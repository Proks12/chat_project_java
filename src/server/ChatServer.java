package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A multi-threaded chat server that accepts client connections, broadcasts messages,
 * logs chat activity, monitors client inactivity and filters banned words.
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

    /** Filter for banned words */
    private WordFilter wordFilter;

    /**
     * Application entry point.
     */
    public static void main(String[] args) {
        loadConfig();
        new ChatServer().start();
    }

    /**
     * Loads server configuration from "config/server.properties".
     */
    private static void loadConfig() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("config/server.properties")) {
            props.load(fis);

            PORT = Integer.parseInt(props.getProperty("port", "12345").trim());
            INACTIVITY_LIMIT_MS =
                    Long.parseLong(props.getProperty("inactivity_limit_ms", "300000").trim());
            LOG_MAX_SIZE =
                    Long.parseLong(props.getProperty("log_max_size", "5000000").trim());

        } catch (IOException e) {
            System.out.println("Cannot load config file, using defaults.");
            PORT = 12345;
            INACTIVITY_LIMIT_MS = 5 * 60 * 1000;
            LOG_MAX_SIZE = 5_000_000;
        }
    }

    /**
     * Starts the chat server.
     */
    public void start() {
        System.out.println("Chat server started on port " + PORT);

        // Load banned words
        try {
            wordFilter = new WordFilter(Path.of("config/banwords.txt"));
            System.out.println("Loaded banned words: " +
                    wordFilter.getBannedWords().size());
        } catch (IOException e) {
            System.err.println("Failed to load banwords file!");
            e.printStackTrace();
            return;
        }

        // Start worker threads
        new Thread(new BroadcastWorker(clients, broadcastQueue), "BroadcastWorker").start();
        new Thread(new LoggerWorker(logQueue), "LoggerWorker").start();
        new Thread(new TimeoutWatcher(clients, broadcastQueue, logQueue), "TimeoutWatcher").start();

        // Accept clients
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();

                ClientHandler handler =
                        new ClientHandler(clientSocket, this, clients, broadcastQueue, logQueue);

                clients.add(handler);
                new Thread(handler, "ClientHandler-" + clientSocket.getPort()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the word filter.
     */
    public WordFilter getWordFilter() {
        return wordFilter;
    }
}
