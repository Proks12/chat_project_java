package client;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

/**
 * A simple Swing-based chat client that connects to a TCP chat server.
 * The client displays incoming messages in a GUI window and allows the user
 * to send messages via a text field and a button.
 */
public class ChatClientGUI {

    /** Main application window. */
    private JFrame frame;

    /** Text area that displays received chat messages. */
    private JTextArea chatArea;

    /** Text field where a user types outgoing messages. */
    private JTextField inputField;

    /** Button used to send a message. */
    private JButton sendButton;

    /** Writer for sending data to the server. */
    private PrintWriter writer;

    /** The socket used to communicate with the server. */
    private Socket socket;

    /**
     * Application entry point.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClientGUI().start());
    }

    /**
     * Initializes GUI, connects to the server, starts the listening thread,
     * and configures sending of messages.
     */
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

            // Send username to server
            writer.println(name);

            /**
             * Thread that continuously reads messages from the server.
             * Handles server commands such as /disconnect.
             */
            new Thread(() -> {
                String msg;
                try {
                    while ((msg = reader.readLine()) != null) {

                        // Server requests that the client disconnects
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

            // Attach send listeners
            sendButton.addActionListener(e -> sendMessage());
            inputField.addActionListener(e -> sendMessage());

        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Cannot connect to server: " + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Builds the Swing GUI layout including the chat display area,
     * input field, and send button.
     */
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

    /**
     * Sends the text currently typed in the input field to the server.
     * If the user types "/quit", the command is also forwarded to the server
     * and the client begins shutdown.
     */
    private void sendMessage() {
        String msg = inputField.getText().trim();
        if (!msg.isEmpty()) {
            writer.println(msg);
            inputField.setText("");

            // Client requests to close connection
            if (msg.equalsIgnoreCase("/quit")) {
                writer.println("/quit");
            }
        }
    }
}
