package Test;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;


class BroadcastWorkerTest {

    @Test
    void testBroadcast() throws InterruptedException {
        LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
        List<server.ClientHandler> clients = new CopyOnWriteArrayList<>();
        TestClient client1 = new TestClient();
        TestClient client2 = new TestClient();
        clients.add(client1);
        clients.add(client2);


        server.BroadcastWorker worker = new server.BroadcastWorker(clients, queue);
        Thread thread = new Thread(worker);
        thread.setDaemon(true);
        thread.start();

        queue.put("Hello all!");
        Thread.sleep(100); // počkej na zpracování

        assertTrue(client1.received.contains("Hello all!"));
        assertTrue(client2.received.contains("Hello all!"));
    }

    static class TestClient extends server.ClientHandler {
        public List<String> received = new CopyOnWriteArrayList<>();

        public TestClient() {
            super(null, null, null, null);
        }

        @Override
        public void sendMessage(String msg) {
            received.add(msg);
        }
    }
}
