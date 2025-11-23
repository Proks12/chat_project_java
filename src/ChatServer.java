import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class ChatServer {
    public static int PORT;
    public static long INACTIVITY_LIMIT_MS;
    public static long LOG_MAX_SIZE;

    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final LinkedBlockingQueue<String> broadcastQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<String> logQueue = new LinkedBlockingQueue<>();

    public static void main(String[] args) {
        loadConfig();
        new ChatServer().start();
    }

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
            INACTIVITY_LIMIT_MS = 5*60*1000;
            LOG_MAX_SIZE = 5_000_000;
        }
    }

    public void start() {
        System.out.println("Chat server started on port " + PORT);

        Thread broadcaster = new Thread(new BroadcastWorker(clients, broadcastQueue));
        Thread logger = new Thread(new LoggerWorker(logQueue));
        Thread timeoutWatcher = new Thread(new TimeoutWatcher(clients, broadcastQueue, logQueue));


        broadcaster.start();
        logger.start();
        timeoutWatcher.start();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, clients, broadcastQueue, logQueue);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
