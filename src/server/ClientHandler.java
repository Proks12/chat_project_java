package server;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Handles communication with a single chat client.
 */
public class ClientHandler implements Runnable {

    /** The socket associated with this client. */
    private final Socket socket;

    /** Reference to chat server (shared services, config). */
    private final ChatServer server;

    /** Shared list of all connected clients. */
    private final List<ClientHandler> clients;

    /** Queue used to broadcast messages to all clients. */
    private final LinkedBlockingQueue<String> broadcastQueue;

    /** Queue used for logging messages. */
    private final LinkedBlockingQueue<String> logQueue;

    /** Reader for incoming messages from the client. */
    private BufferedReader reader;

    /** Writer for sending messages to the client. */
    private PrintWriter writer;

    /** Name of the client. */
    protected String name;

    /** Timestamp of the client's last activity (ms since epoch). */
    private volatile long lastActivityTime;

    /**
     * Constructs a new ClientHandler.
     */
    public ClientHandler(
            Socket socket,
            ChatServer server,
            List<ClientHandler> clients,
            LinkedBlockingQueue<String> broadcastQueue,
            LinkedBlockingQueue<String> logQueue) {

        this.socket = socket;
        this.server = server;
        this.clients = clients;
        this.broadcastQueue = broadcastQueue;
        this.logQueue = logQueue;
        this.lastActivityTime = System.currentTimeMillis();
    }

    public String getName() {
        return name;
    }

    public long getLastActivityTime() {
        return lastActivityTime;
    }

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

                // QUIT
                if (message.equalsIgnoreCase("/quit")) {
                    writer.println("/disconnect");
                    break;
                }

                // WHO
                if (message.equals("/who")) {
                    writer.println("Users online: " + clients.size());
                    continue;
                }

                // CHANGE NAME
                if (message.startsWith("/name ")) {
                    changeName(message.substring(6));
                    continue;
                }


                // BANWORDS COMMAND
                if (message.equals("/banwords")) {
                    writer.println("Banned words:");
                    for (String word : server.getWordFilter().getBannedWords()) {
                        writer.println(" - " + word);
                    }
                    continue;
                }
                // HELP
                if (message.equalsIgnoreCase("/help")) {
                    writer.println("===== Available commands =====");
                    writer.println("/help        - show this help");
                    writer.println("/who         - show number of online users");
                    writer.println("/name <name> - change your nickname");
                    writer.println("/banwords    - show banned words");
                    writer.println("/quit        - disconnect from chat");
                    writer.println("==========================");
                    continue;
                }

                // SHOW BANNED WORDS
                if (message.equalsIgnoreCase("/banwords")) {
                    writer.println("=== Banned words ===");

                    if (server.getWordFilter().getBannedWords().isEmpty()) {
                        writer.println("(no banned words configured)");
                    } else {
                        for (String word : server.getWordFilter().getBannedWords()) {
                            writer.println(" - " + word);
                        }
                    }

                    writer.println("====================");
                    continue;
                }

                // FILTER MESSAGE
                if (server.getWordFilter().containsBannedWord(message)) {
                    writer.println("SERVER: Your message contains a banned word.");
                    logQueue.put("BLOCKED message from " + name + ": " + message);
                    continue;
                }

                // NORMAL MESSAGE
                broadcastQueue.put(name + ": " + message);
                logQueue.put(name + ": " + message);
            }
        } catch (IOException | InterruptedException ignored) {
        } finally {
            disconnect();
        }
    }

    /**
     * Disconnects the client.
     */
    public void disconnect() {
        try {
            clients.remove(this);
            if (name != null) {
                broadcastQueue.put(name + " left the chat.");
                logQueue.put(name + " disconnected.");
            }
            if (socket != null) socket.close();
        } catch (IOException | InterruptedException ignored) {
        }
    }

    /**
     * Sends a message directly to this client.
     */
    public void sendMessage(String msg) {
        if (writer != null) {
            writer.println(msg);
        }
    }
    /**
     * Changes the client's name and broadcasts the change.
     *
     * @param newName new client name
     * @throws InterruptedException if interrupted while broadcasting
     */
    public void changeName(String newName) throws InterruptedException {
        broadcastQueue.put(name + " is now known as " + newName);
        name = newName;
    }

}
