package server;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class TimeoutWatcher implements Runnable {
    private final List<ClientHandler> clients;
    private final LinkedBlockingQueue<String> broadcastQueue;
    private final LinkedBlockingQueue<String> logQueue;
    private static final long CHECK_INTERVAL = 10_000;

    public TimeoutWatcher(List<ClientHandler> clients, LinkedBlockingQueue<String> broadcastQueue, LinkedBlockingQueue<String> logQueue) {
        this.clients = clients;
        this.broadcastQueue = broadcastQueue;
        this.logQueue = logQueue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                long now = System.currentTimeMillis();
                for (ClientHandler c : clients) {
                    if (now - c.getLastActivityTime() > ChatServer.INACTIVITY_LIMIT_MS) {
                        broadcastQueue.put(c.getName() + " was removed due to inactivity.");
                        logQueue.put(c.getName() + " disconnected due to inactivity.");
                        c.sendMessage("You were disconnected due to inactivity.");
                        clients.remove(c);
                    }
                }
                Thread.sleep(CHECK_INTERVAL);
            } catch (Exception ignored) {}
        }
    }
}
