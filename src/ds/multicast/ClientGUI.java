package ds.multicast;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author yuzo
 * Description: faça uma interface gráfica para os clientes de chat e que
 * possibilite vários clientes enviarem e visualizarem mensagens de os outros
 * clientes conectados. Use o TCP.
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
    
    public DataOutputStream dos;
    public MulticastSocket ms;
    public DatagramSocket ds;
    public InetAddress addr;
    public int port;
    public List<String> onlineUsers;
    
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
    
    public void setOutputStream(DataOutputStream outputStream) {
        this.dos = outputStream;
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

    class sendMessageButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            if (messageField.getText().length() < 1) {
                // do nothing
            } else if (messageField.getText().equals("..")) {
                messageField.setText("");
            } else {
//                try {
                    chatBox.append("<" + username + ">:  " + messageField.getText()
                            + "\n");
//                    dos.writeUTF(messageField.getText());
                    messageField.setText("");
//                } catch (IOException ex) {
//                    System.out.println("IOE: " + ex.getMessage());
//                }
            }
            messageField.requestFocusInWindow();
        }
    }
    
    
    
    public void receivedCommands(byte[] userInput, InetAddress addr) throws Exception {
        String toString = new String(userInput, StandardCharsets.UTF_8);
        String[] splitUserInput = toString.split(" ");
        byte[] toBeSent;
        String response;
        switch (splitUserInput[0].toLowerCase()) {
            case "join":
                response = "JOINACK" + username;
                toBeSent = response.getBytes(StandardCharsets.UTF_8);
                onlineUsers.add(splitUserInput[1]); //maintain online user list
                DatagramPacket ack = new DatagramPacket(toBeSent, toBeSent.length, addr, port);
                ds.send(ack);
                break;
            case "joinack":
                onlineUsers.add(splitUserInput[1]);
                break;
            case "msgidv":
                break;
            case "listfiles":
                break;
            case "files":
                break;
            case "downfile":
                break;
            case "downinfo":
                break;
            case "leave":
                break;
            
            default:
                System.out.println("it's nothing");
                break;
        }
    }
    
    public void commandsToBeSent(String userInput) throws Exception {
        String[] splitUserInput = userInput.split(" ");
        switch (splitUserInput[0].toLowerCase()) {
            case "join":
                String command = splitUserInput[0].toUpperCase() + username;
                byte[] toBeSent = command.getBytes(StandardCharsets.UTF_8);
                DatagramPacket join = new DatagramPacket(toBeSent, toBeSent.length, addr, port);
                ms.send(join);
                break;
            case "msgidv":
                break;
            case "listfiles":
                break;
            case "files":
                break;
            case "downfile":
                break;
            case "downinfo":
                break;
            case "leave":
                break;
            
            default:
                System.out.println("it's nothing");
                break;
        }
    }

    class enterServerButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            username = usernameField.getText();
            if (username.length() < 1) {
                System.out.println("Seriously, we'll just call you Bot.");
                username = "Crash";
            }
            preFrame.setVisible(false);
            display();
        }
    }
}
