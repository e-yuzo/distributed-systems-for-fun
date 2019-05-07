/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ds.tcp.request_process;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author yuzo Description: faça um servidor para processar as seguintes
 * mensagens dos clientes. O servidor deve suportar mensagens de múltiplos
 * clientes. Use o TCP. As mensagens estão no formato String UTF.
 */
public class ClientGUI {

    public String appName = "I Did Not Steal This Interface, I Think I Borrowed It.";
    public ClientGUI mainGUI;
    public JFrame newFrame = new JFrame(appName);
    public JButton sendMessage;
    public JTextField messageField;
    public JTextArea chatBox;
    public JTextField usernameField;
    public JFrame preFrame;
    public JLabel usernameLabel;
    public String username = Long.toString(Thread.currentThread().getId());
    public DataOutputStream outputStream;
    public DataInputStream inputStream;
    public Socket clientSocket;

    public void preDisplay() {
        newFrame.setVisible(false);
        preFrame = new JFrame(appName);
        usernameField = new JTextField(15);
        usernameLabel = new JLabel("Username:");
        JButton enterServer = new JButton("Uncover Universe");
        enterServer.addActionListener(new enterServerButtonListener());
        JPanel prePanel = new JPanel(new GridBagLayout());

        GridBagConstraints preRight = new GridBagConstraints();
        preRight.insets = new Insets(0, 0, 0, 10);
        preRight.anchor = GridBagConstraints.EAST;
        GridBagConstraints preLeft = new GridBagConstraints();
        preLeft.anchor = GridBagConstraints.WEST;
        preLeft.insets = new Insets(0, 10, 0, 10);
        //preRight.weightx = 2.0;
        preRight.fill = GridBagConstraints.HORIZONTAL;
        preRight.gridwidth = GridBagConstraints.REMAINDER;

        prePanel.add(usernameLabel, preLeft);
        prePanel.add(usernameField, preRight);
        preFrame.add(BorderLayout.CENTER, prePanel);
        preFrame.add(BorderLayout.SOUTH, enterServer);
        preFrame.setSize(300, 300);
        preFrame.setLocationRelativeTo(null);
        preFrame.setVisible(true);
    }

    public void setDataStream(Socket clientSocket, DataOutputStream outputStream, DataInputStream inputStream) {
        this.outputStream = outputStream;
        this.inputStream = inputStream;
        this.clientSocket = clientSocket;
    }

    public void display() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel southPanel = new JPanel();
        southPanel.setBackground(Color.BLUE);
        southPanel.setLayout(new GridBagLayout());

        messageField = new JTextField(30);
        messageField.requestFocusInWindow();

        sendMessage = new JButton("Send Message");
        sendMessage.addActionListener(new sendMessageButtonListener());

        chatBox = new JTextArea();
        chatBox.setEditable(false);
        chatBox.setFont(new Font("Serif", Font.PLAIN, 15));
        chatBox.setLineWrap(true);

        mainPanel.add(new JScrollPane(chatBox), BorderLayout.CENTER);

        GridBagConstraints left = new GridBagConstraints();
        left.anchor = GridBagConstraints.LINE_START;
        left.fill = GridBagConstraints.HORIZONTAL;
        left.weightx = 512.0D;
        left.weighty = 1.0D;

        GridBagConstraints right = new GridBagConstraints();
        right.insets = new Insets(0, 10, 0, 0);
        right.anchor = GridBagConstraints.LINE_END;
        right.fill = GridBagConstraints.NONE;
        right.weightx = 1.0D;
        right.weighty = 1.0D;

        southPanel.add(messageField, left);
        southPanel.add(sendMessage, right);

        mainPanel.add(BorderLayout.SOUTH, southPanel);

        newFrame.add(mainPanel);
        newFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        newFrame.setSize(470, 300);
        newFrame.setLocationRelativeTo(null);
        newFrame.setVisible(true);
    }

    String fileName;

    class sendMessageButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            String message = messageField.getText();
            messageField.setText("");
            if (message.length() < 1) {
                // do nothing
            } else if (message.equals("..")) {
                chatBox.setText("Cleared all messages\n");
                messageField.setText("");
            } else {
                try {
                    chatBox.append("<" + username + ">:  " + message
                            + "\n");
                    outputStream.writeUTF(message);
                    requestResponseHandler(message);
                } catch (IOException ex) {
                    Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            messageField.requestFocusInWindow();
        }
    }

    public void requestResponseHandler(String message) {
        try {
            String[] messageArray = message.split(" ");
            //String request;
            String response;
            switch (messageArray[0]) {
                case "DOWN": //addfile //dont need
                    SwingUtilities.invokeLater(
                            new Runnable() {
                        public void run() {
                            FileOutputStream fos = null;
                            try {
                                fileName = messageArray[1];
                                int fileSize = (int) inputStream.readLong();
                                chatBox.append("<" + "System" + ">:  " + fileSize
                                        + "\n");
                                if (fileSize > 0) {
                                    fos = new FileOutputStream("/home/yuzo/NetBeansProjects/downloaded_pictures/" + fileName);
                                    System.out.println(fileSize);
                                    int bufferSize = 4096;
                                    byte[] buffer = new byte[bufferSize];
                                    int read;
                                    while ((read = inputStream.read(buffer)) != -1) {
                                        fos.write(buffer, 0, read);
                                        System.out.println("received" + read);
                                        if (read != bufferSize) {
                                            break;
                                        }
                                    }
                                    fos.close();
                                }
                            } catch (FileNotFoundException ex) {
                                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IOException ex) {
                                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    });

                    break;
                case "FILES":
                    response = inputStream.readUTF();
                    chatBox.append("<" + "System" + ">:  " + response + "\n");
                    break;
                case "DATE":
                    response = inputStream.readUTF();
                    chatBox.append("<" + "System" + ">:  " + response + "\n");
                    break;
                case "TIME":
                    response = inputStream.readUTF();
                    chatBox.append("<" + "System" + ">:  " + response + "\n");
                    break;
                case "EXIT":
                    inputStream.close();
                    outputStream.close();
                    clientSocket.close();
                    System.exit(0);
                default:
                    response = inputStream.readUTF();
                    chatBox.append("<" + "System" + ">:  " + response + "\n");
                    System.out.println("I can't understand your command.");
            }
        } catch (IOException ex) {
            Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    class enterServerButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            username = usernameField.getText();
            if (username.length() < 1) {
                System.out.println("Well, we'll just call you Crash Bandicoot. Have fun.");
                username = "Crash Bandicoot";
            }
            preFrame.setVisible(false);
            display();
        }
    }
}
