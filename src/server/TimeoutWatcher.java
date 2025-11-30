package server;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Worker thread that monitors client inactivity and disconnects clients
 * who have been idle for longer than the configured inactivity limit.
 */
public class TimeoutWatcher implements Runnable {

    /** List of currently connected clients. */
    private final List<ClientHandler> clients;

    /** Queue for broadcasting messages to all clients. */
    private final LinkedBlockingQueue<String> broadcastQueue;

    /** Queue for logging messages. */
    private final LinkedBlockingQueue<String> logQueue;

    /** Interval (ms) at which client activity is checked. */
    private static final long CHECK_INTERVAL = 10_000; // 10 seconds

    /**
     * Constructs a TimeoutWatcher.
     *
     * @param clients        list of connected clients
     * @param broadcastQueue queue for broadcasting messages
     * @param logQueue       queue for logging messages
     */
    public TimeoutWatcher(List<ClientHandler> clients, LinkedBlockingQueue<String> broadcastQueue,
                          LinkedBlockingQueue<String> logQueue) {
        this.clients = clients;
        this.broadcastQueue = broadcastQueue;
        this.logQueue = logQueue;
    }

    /**
     * Continuously checks all clients for inactivity and disconnects
     * those who exceed the inactivity limit.
     */
    @Override
    public void run() {
        while (true) {
            try {
                long now = System.currentTimeMillis();

                for (ClientHandler c : clients) {
                    if (now - c.getLastActivityTime() > ChatServer.INACTIVITY_LIMIT_MS) {

                        // pošli mu zprávu jako /quit
                        c.sendMessage("You were disconnected due to inactivity.");

                        // odpojí ho stejně jako /quit
                        c.disconnect();
                    }
                }

                Thread.sleep(CHECK_INTERVAL);

            } catch (Exception ignored) {}
        }
    }

}
