package server;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class BroadcastWorker implements Runnable {
    private final List<ClientHandler> clients;
    private final LinkedBlockingQueue<String> broadcastQueue;

    public BroadcastWorker(List<ClientHandler> clients, LinkedBlockingQueue<String> broadcastQueue) {
        this.clients = clients;
        this.broadcastQueue = broadcastQueue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String msg = broadcastQueue.take();
                for (ClientHandler client : clients) {
                    client.sendMessage(msg);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
