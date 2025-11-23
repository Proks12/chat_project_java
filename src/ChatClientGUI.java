import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class ChatClientGUI {

    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private PrintWriter writer;
    private Socket socket;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClientGUI().start());
    }

    public void start() {
        setupGUI();

        String host = "127.0.0.1";
        int port = 12345;

        String name = JOptionPane.showInputDialog(frame, "Enter your name:");
        if (name == null || name.trim().isEmpty()) {
            name = "Anonymous";
        }

        try {
            socket = new Socket(host, port);
            writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Poslat jméno serveru
            writer.println(name);

            // Vlákno pro příjem zpráv od serveru
            new Thread(() -> {
                String msg;
                try {
                    while ((msg = reader.readLine()) != null) {
                        // Pokud server poslal /disconnect → ukončit klienta
                        if (msg.equals("/disconnect")) {
                            JOptionPane.showMessageDialog(frame,
                                    "You have been disconnected from the server.",
                                    "Disconnected",
                                    JOptionPane.INFORMATION_MESSAGE);
                            socket.close();
                            frame.dispose();
                            break;
                        }

                        chatArea.append(msg + "\n");
                        chatArea.setCaretPosition(chatArea.getDocument().getLength());
                    }
                } catch (IOException e) {
                    chatArea.append("Disconnected from server.\n");
                }
            }).start();

            // Odesílání zpráv
            sendButton.addActionListener(e -> sendMessage());
            inputField.addActionListener(e -> sendMessage());

        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Cannot connect to server: " + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupGUI() {
        frame = new JFrame("Chat Client GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel panel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("Send");
        panel.add(inputField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);
        frame.add(panel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void sendMessage() {
        String msg = inputField.getText().trim();
        if (!msg.isEmpty()) {
            writer.println(msg);
            inputField.setText("");

            // Pokud klient sám chce ukončit spojení
            if (msg.equalsIgnoreCase("/quit")) {
                writer.println("/quit"); // poslat serveru
            }
        }
    }
}
