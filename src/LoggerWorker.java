import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

public class LoggerWorker implements Runnable {
    private final LinkedBlockingQueue<String> logQueue;
    private static final String LOG_FILE = "logs/chat.log";

    public LoggerWorker(LinkedBlockingQueue<String> logQueue) {
        this.logQueue = logQueue;
        new File("logs").mkdirs();
    }

    @Override
    public void run() {
        try {
            while (true) {
                String msg = logQueue.take();
                appendToLog(msg);
            }
        } catch (InterruptedException ignored) {}
    }

    private void appendToLog(String msg) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            fw.write("[" + timeStamp + "] " + msg + System.lineSeparator());
        } catch (IOException e) {
            System.err.println("Logger error: " + e.getMessage());
        }
    }
}
