import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class ChatClientGUI {

    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private PrintWriter writer;

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
            Socket socket = new Socket(host, port);
            writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Poslat jméno serveru
            writer.println(name);

            // Vlákno pro příjem zpráv
            new Thread(() -> {
                String msg;
                try {
                    while ((msg = reader.readLine()) != null) {
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
            JOptionPane.showMessageDialog(frame, "Cannot connect to server: " + e.getMessage());
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
        }
    }
}
