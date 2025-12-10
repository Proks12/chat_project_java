package Test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.ChatServer;
import server.ClientHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientHandlerTest {

    private ClientHandler handler;
    private List<ClientHandler> clients;
    private LinkedBlockingQueue<String> broadcastQueue;
    private LinkedBlockingQueue<String> logQueue;

    @BeforeEach
    void setup() {
        clients = new ArrayList<>();
        broadcastQueue = new LinkedBlockingQueue<>();
        logQueue = new LinkedBlockingQueue<>();

        ChatServer server = new ChatServer();

        handler = new ClientHandler(
                null,              // socket not needed for unit test
                server,            // fake server instance
                clients,
                broadcastQueue,
                logQueue
        );


    }

    @Test
    void testChangeName() throws InterruptedException {
        handler.changeName("NewName");

        assertEquals("NewName", handler.getName());
        assertEquals("OldName is now known as NewName", broadcastQueue.take());
    }
}
