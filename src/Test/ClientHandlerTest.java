package Test;

import org.junit.jupiter.api.Test;
import server.ClientHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientHandlerTest {

    @Test
    void testChangeName() throws InterruptedException {
        LinkedBlockingQueue<String> broadcastQueue = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
        List<ClientHandler> clients = new CopyOnWriteArrayList<>();

        // Vytvoříme ClientHandler instanci s name "Anonymous"
        ClientHandler handler = new ClientHandlerMock(clients, broadcastQueue, logQueue, "Anonymous");

        // Zavoláme novou veřejnou metodu
        handler.changeName("NewName");

        // Zkontrolujeme, že broadcastQueue obsahuje zprávu
        String msg = broadcastQueue.take();
        assertEquals("Anonymous is now known as NewName", msg);

        // Zkontrolujeme, že se jméno skutečně změnilo
        assertEquals("NewName", handler.getName());
    }

    // Mock třída, jen pro test
    static class ClientHandlerMock extends ClientHandler {
        public ClientHandlerMock(List<ClientHandler> clients, LinkedBlockingQueue<String> broadcastQueue,
                                 LinkedBlockingQueue<String> logQueue, String initialName) {
            super(null, clients, broadcastQueue, logQueue);
            super.name = initialName;
        }

        @Override
        public void run() {

        }
    }
}
