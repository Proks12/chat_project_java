package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A multi-threaded chat server that accepts client connections, broadcasts messages,
 * logs chat activity, and monitors client inactivity.
 *
 * <p>The server loads configuration from a properties file or uses defaults if the file is missing.</p>
 */
public class ChatServer {

    /** Port on which the server listens for incoming connections. */
    public static int PORT;

    /** Time limit (in milliseconds) for client inactivity before disconnecting. */
    public static long INACTIVITY_LIMIT_MS;

    /** Maximum size (in bytes) of the log file. */
    public static long LOG_MAX_SIZE;

    /** Thread-safe list of currently connected clients. */
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    /** Queue for broadcasting messages to all clients. */
    private final LinkedBlockingQueue<String> broadcastQueue = new LinkedBlockingQueue<>();

    /** Queue for logging messages. */
    private final LinkedBlockingQueue<String> logQueue = new LinkedBlockingQueue<>();

    /**
     * Application entry point.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        loadConfig();
        new ChatServer().start();
    }

    /**
     * Loads server configuration from "config/server.properties".
     * Uses default values if the file cannot be read.
     */
    private static void loadConfig() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("config/server.properties")) {
            props.load(fis);
            PORT = Integer.parseInt(props.getProperty("port", "12345"));
            INACTIVITY_LIMIT_MS = Long.parseLong(props.getProperty("inactivity_limit_ms", "300000").trim());
            LOG_MAX_SIZE = Long.parseLong(props.getProperty("log_max_size", "5000000").trim());
        } catch (IOException e) {
            System.out.println("Cannot load config file, using defaults.");
            PORT = 12345;
            INACTIVITY_LIMIT_MS = 5 * 60 * 1000; // 5 minutes
            LOG_MAX_SIZE = 5_000_000; // 5 MB
        }
    }

    /**
     * Starts the chat server, initializes worker threads, and accepts incoming client connections.
     */
    public void start() {
        System.out.println("Chat server started on port " + PORT);

        // Start worker threads
        Thread broadcaster = new Thread(new BroadcastWorker(clients, broadcastQueue));
        Thread logger = new Thread(new LoggerWorker(logQueue));
        Thread timeoutWatcher = new Thread(new TimeoutWatcher(clients, broadcastQueue, logQueue));

        broadcaster.start();
        logger.start();
        timeoutWatcher.start();

        // Accept clients
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();

                // Create handler for new client
                ClientHandler handler = new ClientHandler(clientSocket, clients, broadcastQueue, logQueue);
                clients.add(handler);

                // Start client handler thread
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
