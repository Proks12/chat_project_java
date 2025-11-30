package server;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Handles communication with a single chat client.
 * <p>
 * Each client is handled in its own thread. The handler reads messages from the client,
 * updates the last activity time, supports commands like /quit, /who, /name, and
 * broadcasts messages to all connected clients.
 * </p>
 */
public class ClientHandler implements Runnable {

    /** The socket associated with this client. */
    private final Socket socket;

    /** Shared list of all connected clients. */
    private final List<ClientHandler> clients;

    /** Queue used to broadcast messages to all clients. */
    protected final LinkedBlockingQueue<String> broadcastQueue;

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
     *
     * @param socket          the socket connected to the client
     * @param clients         shared list of clients
     * @param broadcastQueue  shared queue for broadcasting messages
     * @param logQueue        shared queue for logging messages
     */
    public ClientHandler(Socket socket, List<ClientHandler> clients,
                         LinkedBlockingQueue<String> broadcastQueue,
                         LinkedBlockingQueue<String> logQueue) {
        this.socket = socket;
        this.clients = clients;
        this.broadcastQueue = broadcastQueue;
        this.logQueue = logQueue;
        this.lastActivityTime = System.currentTimeMillis();
    }

    /** @return the name of the client */
    public String getName() { return name; }

    /** @return the timestamp of the last activity from this client */
    public long getLastActivityTime() { return lastActivityTime; }

    /**
     * Main loop for reading messages from the client.
     * Handles special commands:
     * <ul>
     *     <li>/quit - disconnects the client</li>
     *     <li>/who - lists the number of users online</li>
     *     <li>/name NEWNAME - changes the client's name</li>
     * </ul>
     */
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

    /**
     * Disconnects the client, removes it from the clients list, broadcasts the disconnection,
     * logs it, and closes the socket.
     */
    public  void disconnect() {
        try {
            clients.remove(this);
            if (name != null) {
                broadcastQueue.put(name + " left the chat.");
                logQueue.put(name + " disconnected.");
            }
            if (socket != null) socket.close();
        } catch (IOException | InterruptedException ignored) {}
    }

    /**
     * Sends a message directly to this client.
     *
     * @param msg the message to send
     */
    public void sendMessage(String msg) { writer.println(msg); }

    /**
     * Changes the client's name and broadcasts the change.
     *
     * @param newName the new name for the client
     * @throws InterruptedException if the broadcast queue operation is interrupted
     */
    public void changeName(String newName) throws InterruptedException {
        broadcastQueue.put(name + " is now known as " + newName);
        name = newName;
    }
}
