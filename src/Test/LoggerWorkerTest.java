package Test;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertTrue;
import server.LoggerWorker;
class LoggerWorkerTest {

    @Test
    void testLogging() throws Exception {
        LinkedBlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
        LoggerWorker logger = new LoggerWorker(logQueue);

        Thread thread = new Thread(logger);
        thread.setDaemon(true);
        thread.start();

        String msg = "Test log message";
        logQueue.put(msg);

        Thread.sleep(100); // počkej na zapsání

        File logFile = new File("logs/chat.log");
        String content = new String(Files.readAllBytes(logFile.toPath()));
        assertTrue(content.contains(msg));
    }
}
