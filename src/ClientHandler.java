import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final List<ClientHandler> clients;
    private final LinkedBlockingQueue<String> broadcastQueue;
    private final LinkedBlockingQueue<String> logQueue;
    private BufferedReader reader;
    private PrintWriter writer;
    private String name;
    private volatile long lastActivityTime;

    public ClientHandler(Socket socket, List<ClientHandler> clients,
                         LinkedBlockingQueue<String> broadcastQueue,
                         LinkedBlockingQueue<String> logQueue) {
        this.socket = socket;
        this.clients = clients;
        this.broadcastQueue = broadcastQueue;
        this.logQueue = logQueue;
        this.lastActivityTime = System.currentTimeMillis();
    }

    public String getName() { return name; }
    public long getLastActivityTime() { return lastActivityTime; }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            writer.println("Enter your name:");
            name = reader.readLine();
            broadcastQueue.put(name + " joined the chat.");
            logQueue.put(name + " connected.");

            String message;
            while ((message = reader.readLine()) != null) {
                lastActivityTime = System.currentTimeMillis();

                if (message.equalsIgnoreCase("/quit")) {
                    writer.println("/disconnect");
                    break;
                }

                if (message.equals("/who")) {
                    writer.println("Users online: " + clients.size());
                    continue;
                }
                if (message.startsWith("/name ")) {
                    String newName = message.substring(6);
                    broadcastQueue.put(name + " is now known as " + newName);
                    name = newName;
                    continue;
                }

                broadcastQueue.put(name + ": " + message);
                logQueue.put(name + ": " + message);
            }
        } catch (IOException | InterruptedException ignored) {}
        finally { disconnect(); }
    }

    private void disconnect() {
        try {
            clients.remove(this);
            if (name != null) {
                broadcastQueue.put(name + " left the chat.");
                logQueue.put(name + " disconnected.");
            }
            if (socket != null) socket.close();
        } catch (IOException | InterruptedException ignored) {}
    }

    public void sendMessage(String msg) { writer.println(msg); }
}
