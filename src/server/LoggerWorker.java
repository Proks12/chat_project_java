package server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Worker thread responsible for logging chat messages to a file.
 * <p>
 * Messages are taken from a shared queue and appended to "logs/chat.log" with a timestamp.
 * If the logs directory does not exist, it is created automatically.
 * </p>
 */
public class LoggerWorker implements Runnable {

    /** Queue from which log messages are consumed. */
    private final LinkedBlockingQueue<String> logQueue;

    /** Path to the log file. */
    private static final String LOG_FILE = "logs/chat.log";

    /**
     * Constructs a LoggerWorker with the given log queue.
     *
     * @param logQueue the queue holding messages to log
     */
    public LoggerWorker(LinkedBlockingQueue<String> logQueue) {
        this.logQueue = logQueue;
        // Ensure the logs directory exists
        new File("logs").mkdirs();
    }

    /**
     * Main loop for consuming messages from the log queue and writing them to the file.
     */
    @Override
    public void run() {
        try {
            while (true) {
                // Wait for a message to log
                String msg = logQueue.take();
                appendToLog(msg);
            }
        } catch (InterruptedException ignored) {
            // Thread interrupted, exit gracefully
        }
    }

    /**
     * Appends a message to the log file with a timestamp.
     *
     * @param msg the message to append
     */
    private void appendToLog(String msg) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            fw.write("[" + timeStamp + "] " + msg + System.lineSeparator());
        } catch (IOException e) {
            System.err.println("Logger error: " + e.getMessage());
        }
    }
}
