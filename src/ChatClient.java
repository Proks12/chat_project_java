import java.io.*;
import java.net.Socket;

public class ChatClient {
    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 12345;

        try (Socket socket = new Socket(host, port)) {
            System.out.println("Connected to chat server.");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader console = new BufferedReader(
                    new InputStreamReader(System.in)
            );

            // Vlákno pro příjem zpráv od serveru
            new Thread(() -> {
                String fromServer;
                try {
                    while ((fromServer = reader.readLine()) != null) {
                        System.out.println(fromServer);
                    }
                } catch (IOException ignored) {}
            }).start();

            // Hlavní vlákno – odesílání zpráv
            String input;
            while ((input = console.readLine()) != null) {
                writer.println(input);
            }

        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
    }
}
