package Test;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for the BroadcastWorker class.
 * Ensures that messages placed in the broadcast queue are sent to all clients.
 */
class BroadcastWorkerTest {

    /**
     * Tests that messages put into the broadcast queue are received by all clients.
     */
    @Test
    void testBroadcast() throws InterruptedException {
        LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
        List<server.ClientHandler> clients = new CopyOnWriteArrayList<>();

        // Test clients that record received messages
        TestClient client1 = new TestClient();
        TestClient client2 = new TestClient();
        clients.add(client1);
        clients.add(client2);

        // Start the BroadcastWorker in a separate daemon thread
        server.BroadcastWorker worker = new server.BroadcastWorker(clients, queue);
        Thread thread = new Thread(worker);
        thread.setDaemon(true);
        thread.start();

        // Put a message in the queue and wait briefly for processing
        queue.put("Hello all!");
        Thread.sleep(100); // krátká pauza na zpracování

        // Verify that both clients received the message
        assertTrue(client1.received.contains("Hello all!"));
        assertTrue(client2.received.contains("Hello all!"));
    }

    /**
     * Test subclass of ClientHandler that captures messages instead of sending over a socket.
     */
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
