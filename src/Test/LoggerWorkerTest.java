package Test;

import org.junit.jupiter.api.Test;
import server.LoggerWorker;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for the LoggerWorker class.
 * Ensures that messages placed in the log queue are correctly written to the log file.
 */
class LoggerWorkerTest {

    /**
     * Tests that a message put into the log queue is appended to the log file.
     */
    @Test
    void testLogging() throws Exception {
        LinkedBlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
        LoggerWorker logger = new LoggerWorker(logQueue);

        // Start the LoggerWorker in a daemon thread
        Thread thread = new Thread(logger);
        thread.setDaemon(true);
        thread.start();

        String msg = "Test log message";
        logQueue.put(msg);

        // Wait briefly for the logger to write the message
        Thread.sleep(100);

        // Read the log file and verify it contains the test message
        File logFile = new File("logs/chat.log");
        String content = new String(Files.readAllBytes(logFile.toPath()));
        assertTrue(content.contains(msg));
    }
}
