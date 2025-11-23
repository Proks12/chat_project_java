package server;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Worker thread responsible for broadcasting messages to all connected clients.
 * Messages are taken from a shared queue and sent to each client in the list.
 */
public class BroadcastWorker implements Runnable {

    /** List of currently connected clients. */
    private final List<ClientHandler> clients;

    /** Queue holding messages to be broadcast to all clients. */
    private final LinkedBlockingQueue<String> broadcastQueue;

    /**
     * Constructs a BroadcastWorker.
     *
     * @param clients        list of connected clients to broadcast messages to
     * @param broadcastQueue queue from which messages will be taken for broadcasting
     */
    public BroadcastWorker(List<ClientHandler> clients, LinkedBlockingQueue<String> broadcastQueue) {
        this.clients = clients;
        this.broadcastQueue = broadcastQueue;
    }

    /**
     * Continuously takes messages from the broadcast queue and sends them
     * to all connected clients. If the thread is interrupted, it prints the stack trace.
     */
    @Override
    public void run() {
        try {
            while (true) {
                // Wait for a message to be available in the queue
                String msg = broadcastQueue.take();

                // Send the message to all clients
                for (ClientHandler client : clients) {
                    client.sendMessage(msg);
                }
            }
        } catch (InterruptedException e) {
            // Thread was interrupted; handle or log as needed
            e.printStackTrace();
        }
    }
}
