package ds.multicast;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
 * @author yuzo Description: faça uma interface gráfica para os clientes de chat
 * e que possibilite vários clientes enviarem e visualizarem mensagens de os
 * outros clientes conectados. Use o TCP. TODO: Synchronized methods. Fix ports
 * in class atributes. dest ports. Join many times without joinGroup method
 * invo. Just for peers to receive name
 */
public class ClientGUI {

    public String appName;
    public ClientGUI mainGUI;
    public JFrame newFrame;
    public JButton sendMessage;
    public JTextField messageField;
    public JTextArea chatBox;
    public JTextField usernameField;
    public JFrame preFrame;
    public JLabel usernameLabel;
    public String username = Long.toString(Thread.currentThread().getId());

//    public DataOutputStream dos;
    public MulticastSocket ms;
    public DatagramSocket ds;
    public InetAddress addr; //multicast addr
    public int msPort; //multicast msPort
    public int fileSharePort = 7777;
    public List<User> onlineUsers;
    public int destDatagramPort = 6799;
    public int destMsPort;

    public void setIO(MulticastSocket ms, DatagramSocket ds, InetAddress addr,
            int mcPort, int fsPort, int dPort, String username, int destMsPort) { //this is important
        this.ms = ms;
        this.ds = ds;
        this.addr = addr;
        this.msPort = mcPort;
        this.fileSharePort = fsPort;
        onlineUsers = new ArrayList<>();
        this.destDatagramPort = dPort;
        this.username = username;
        this.appName = "hey, " + username;
        this.newFrame = new JFrame(appName);
        this.destMsPort = destMsPort;
    }

    public void initializeThreads() { //this is important
        listenForDirectMessages();
        //listenForMulticast(); //listen for it after sending join
    }

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
            String message = messageField.getText();
            if (message.length() < 1) {
                // do nothing
            } else if (message.equals("..")) {
                messageField.setText("");
            } else if (message.equals("::user")) {
                String users = "";
                for (User u : onlineUsers) {
                    users += u.getUsername() + "\n";
                }
                chatBox.append("<" + "System" + ">:  " + users);
                messageField.setText("");
            } else {
//                chatBox.append("<" + username + ">:  " + message + "\n");
                messageField.setText("");
                try {
                    commandsToBeSent(message);
                } catch (Exception ex) {
                    Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            messageField.requestFocusInWindow();
        }
    }

    public User findUserByName(String name) {
        for (User user : onlineUsers) {
            if (user.username.equals(name)) {
                return user;
            }
        }
        return null;
    }

    /**
     * This method listens for incoming multicast messages.
     */
    private void listenForMulticast() { //thread for listening multicast messages
        Runnable runnable = () -> {
            try {
                while (true) {
                    byte[] buffer = new byte[2048];
                    DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
                    ms.receive(messageIn);
                    String message = new String(messageIn.getData(), 0, messageIn.getLength());
                    System.out.println("/**RECEIVED MULTICAST: " + message + "**/");
                    receivedCommands(message.getBytes(), messageIn.getAddress(), messageIn.getPort());
                    //chatBox.append("<" + "Anonymous" + ">:  " + message + "\n");
                }
            } catch (IOException e) {
                System.out.println(e);
            } catch (Exception ex) {
                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        };
        Thread t = new Thread(runnable);
        t.start();

//        SwingUtilities.invokeLater(() -> {
//            try {
//                while (true) {
//                    byte[] buffer = new byte[2048];
//                    DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
//                    ms.receive(messageIn);
//                    String message = new String(messageIn.getData(), 0, messageIn.getLength());
//                    System.out.println("Received: " + message);
//                    receivedCommands(message.getBytes(), messageIn.getAddress(), messageIn.getPort());
//                    chatBox.append("<" + "Anonymous" + ">:  " + message + "\n");
//                }
//            } catch (IOException e) {
//                System.out.println(e);
//            } catch (Exception ex) {
//                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        });
    }

    /**
     * This method listens for incoming UDP messages, this one is using msPort
     * 6799.
     */
    private void listenForDirectMessages() {
        Runnable runnable = () -> {
            try {
                //DatagramSocket ds = new DatagramSocket(destDatagramPort); // msPort 6799
                while (true) {
                    byte[] buffer = new byte[2048];
                    DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
                    ds.receive(messageIn);
                    String message = new String(messageIn.getData(), 0, messageIn.getLength());
                    System.out.println("/**RECEIVED UDP MESSAGE: " + message + "**/");
                    receivedCommands(message.getBytes(), messageIn.getAddress(), messageIn.getPort());
                    //chatBox.append("<" + "Whoever" + ">:  " + message + "\n");
                }
            } catch (IOException e) {
                System.out.println(e);
            } catch (Exception ex) {
                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        };
        Thread t = new Thread(runnable);
        t.start();
//        SwingUtilities.invokeLater(() -> {
//            try {
//                //DatagramSocket ds = new DatagramSocket(destDatagramPort); // msPort 6799
//                while (true) {
//                    byte[] buffer = new byte[2048];
//                    DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
//                    ds.receive(messageIn);
//                    String message = new String(messageIn.getData(), 0, messageIn.getLength());
//                    System.out.println("Received: " + message);
//                    receivedCommands(message.getBytes(), messageIn.getAddress(), messageIn.getPort());
//                    chatBox.append("<" + "Whatever" + ">:  " + message + "\n");
//                }
//            } catch (IOException e) {
//                System.out.println(e);
//            } catch (Exception ex) {
//                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        });
    }

    /**
     * This method sends the response to the destination, given the received
     * 'userInput'.
     *
     * @param userInput
     * @param addr
     * @param port
     * @throws Exception
     */
    public void receivedCommands(byte[] userInput, InetAddress addr, int port) throws Exception {
        String toString = new String(userInput, StandardCharsets.UTF_8);
        String[] splitUserInput = toString.split(" ");
        byte[] toBeSent;
        String response;
        User user;
        System.out.println("sadasd" + toString);
        switch (splitUserInput[0].toLowerCase()) {
            case "join": // send the joinack by receiving join
                if (splitUserInput.length == 2) {
                    response = "JOINACK " + username;
                    toBeSent = response.getBytes(StandardCharsets.UTF_8);
                    user = new User(addr, splitUserInput[1], destDatagramPort); //using fixed port instead of received from user
                    onlineUsers.add(user); //maintain online user list
                    DatagramPacket ack = new DatagramPacket(toBeSent, toBeSent.length, addr, destDatagramPort); //using fixed port instead of received from user
                    chatBox.append("<" + "System" + ">:  " + toString + "\n");
                    ds.send(ack);
                    System.out.println("/**JOIN RECEIVED");
                    System.out.println("JOINACK sent with UDP.**/");
                }
                break;
            case "joinack":
                if (splitUserInput.length == 2) {
                    user = new User(addr, splitUserInput[1], port);
                    onlineUsers.add(user);
                    System.out.println("/**JOIN ACK RECEIVED");
                    System.out.println("USER " + user.getUsername() + " ADDED**/");
                }
                break;
            case "msgidv":
                if (splitUserInput.length >= 6) {
                    System.out.println("/**DIRECT MESSAGE RECEIVED**/");
                    String[] msg = new String[splitUserInput.length - 5];
                    System.arraycopy(splitUserInput, 5, msg, 0, splitUserInput.length - 5);
                    String info = String.join(" ", msg);
                    chatBox.append("<" + splitUserInput[2] + ">:  " + info + "\n");
                }
                break;
            case "msg": // MSG [apelido] "texto"
                if (splitUserInput.length >= 3) {
                    String[] msg = new String[splitUserInput.length - 2];
                    System.arraycopy(splitUserInput, 2, msg, 0, splitUserInput.length - 2);
                    String info = String.join(" ", msg);
                    chatBox.append("<" + splitUserInput[1] + " [MS]" + ">:  " + info + "\n");
                }
            case "listfiles":
                if (splitUserInput.length == 2) {
                    String concatFiles = "FILES [";
                    File folder = new File("/home/yuzo/NetBeansProjects/pictures/");
                    for (File entry : folder.listFiles()) {
                        if (entry.isFile()) {
                            concatFiles += entry.getName() + ", ";
                        }
                    }

                    concatFiles += "]";
                    System.out.println(concatFiles);
                    byte[] fileNamesBytes = concatFiles.getBytes(StandardCharsets.UTF_8);
                    DatagramPacket sendFileNames = new DatagramPacket(
                            fileNamesBytes, fileNamesBytes.length, addr, port);
                    ds.send(sendFileNames);
                }
                break;
            case "files":
                //System.out.println(toString);
                String[] listOfNames = toString.split("\\[")[1].split("\\]")[0].split(", ");
                System.out.println(Arrays.toString(listOfNames));
                String names = "";
                for (String name : listOfNames) {
                    names += name + "\n";
                }
                chatBox.append("<" + "System" + ">:  " + names + "\n");
                break;
            case "downfile": // enviar downinfo e abrir conexão de acordo com a info, enviar file do 'pictures'
                // [lista_alunos_visitar.txt, 50000, 192.168.10.2, 7777]
                File file = new File("/home/yuzo/NetBeansProjects/pictures/" + splitUserInput[2]);
                if (file.exists()) {
                    String info = "DOWNINFO " + "[" + file.getName() + ", " + file.length() + ", " + "127.0.0.1" + ", " + fileSharePort + "]";
                    chatBox.append("<" + "System" + ">:  " + "DOWNLOAD REQUESTED" + "\n");
                    byte[] infoBytes = info.getBytes(StandardCharsets.UTF_8);
                    DatagramPacket infoPacket = new DatagramPacket(
                            infoBytes, infoBytes.length, addr, port);
                    ds.send(infoPacket);
                    //send file
                    chatBox.append("<" + "System" + ">:  " + "FILE SENT" + "\n");
                    FileInputStream fis;
                    int BUFFER = 4096;
                    byte[] byteArray = new byte[BUFFER];
                    fis = new FileInputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    DataInputStream dis = new DataInputStream(bis);
                    ServerSocket ss = new ServerSocket(fileSharePort); // create tcp connection
                    Socket s = ss.accept();
                    DataOutputStream output = new DataOutputStream(s.getOutputStream());
                    int read;
                    while ((read = dis.read(byteArray)) != -1) {
                        output.write(byteArray, 0, read);
                    }
                }
                break;
            case "downinfo": // abrir conexão de acordo com as info e colocar no 'downloaded_pictures'
                SwingUtilities.invokeLater(() -> {
                    try {
                        String[] splittedMessage = toString.split("\\[")[1].split("\\]")[0].split(", "); // separate info section to list
                        File downFile = new File("/home/yuzo/NetBeansProjects/downloaded_pictures/" + splittedMessage[0].trim());
                        System.out.println("DOWNINFO RECEIVED: " + Arrays.toString(splittedMessage));
                        Socket s = new Socket(addr, Integer.valueOf(splittedMessage[3]));

                        DataInputStream dis = new DataInputStream(s.getInputStream());

                        FileOutputStream fos;
                        //chatBox.append("<" + "Anonymous" + ">:  " + splittedMessage[1] + "\n");
                        fos = new FileOutputStream(downFile);
                        int bufferSize = 4096;
                        byte[] buffer = new byte[bufferSize];
                        int read;
                        while ((read = dis.read(buffer)) != -1) {
                            fos.write(buffer, 0, read);
                            //System.out.println("received" + read);
                            if (read != bufferSize) {
                                break;
                            }
                        }
                        fos.close();
                        chatBox.append("<" + "System" + ">:  " + "DOWNLOAD FINISHED" + "\n");
                        System.out.println("DONE DOWNLOADING FILE");
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
                break;
            case "leave":
                user = findUserByName(splitUserInput[1]);
                if (user != null) {
                    deleteUser(user);
                }
                break;

            default:
                System.out.println("it's nothing");
                break;
        }
    }

    public void deleteUser(User user) {
        List<User> toRemove = new ArrayList<>();
        for (User u : onlineUsers) {
            if (u == user) {
                toRemove.add(u);
            }
        }
        onlineUsers.removeAll(toRemove);
    }
    
    public boolean listeningForMulticast = false;

    public void setMulticastGroup() {
        try {
            //System.out.println(addr);
            ms.joinGroup(addr); // join the group
            listeningForMulticast = true;
            listenForMulticast();
            //System.out.println("JOINED GROUP BEFOREHAND: " + ms.);
        } catch (IOException ex) {
            Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void commandsToBeSent(String userInput) throws Exception {
        String[] splitUserInput = userInput.split(" ");
        User destUser;
        String commandString;
        byte[] commandBytes;
        DatagramPacket dp;
        switch (splitUserInput[0].toLowerCase()) {
            case "join":
                String command = splitUserInput[0].toUpperCase() + " " + username;
                byte[] toBeSent = command.getBytes(StandardCharsets.UTF_8);
                DatagramPacket join = new DatagramPacket(toBeSent, toBeSent.length, addr, destMsPort);
                if (!listeningForMulticast) {
                    ms.joinGroup(addr); // join the group
                    listeningForMulticast = true;
                    ms.send(join);
                    listenForMulticast();
                    chatBox.append("<" + "System" + ">:  " + "JOINED GROUP: " + addr + "\n");
                    System.out.println("/**JOINED GROUP: " + addr + "**/");
                } else {
                    chatBox.append("<" + "System" + ">:  " + "ALREADY JOINED GROUP: " + addr + "\n");
                }
                //listenForMulticast();
                break;
            case "msgidv": // MSGIDV FROM [apelido] TO [apelido] "texto"
                destUser = findUserByName(splitUserInput[4]);
                if (destUser != null) {
                    String[] msg = new String[splitUserInput.length - 5];
                    System.arraycopy(splitUserInput, 5, msg, 0, splitUserInput.length - 5);
                    String info = String.join(" ", msg);
                    chatBox.append("<" + username + ">:  " + info + "\n");
                    commandString = splitUserInput[0].toUpperCase() + " "
                            + splitUserInput[1].toUpperCase() + " "
                            + username + " "
                            + splitUserInput[3].toUpperCase() + " "
                            + splitUserInput[4] + " " //destino
                            + info; //texto
                    commandBytes = commandString.getBytes(StandardCharsets.UTF_8);
                    dp = new DatagramPacket(commandBytes, commandBytes.length, destUser.addr, destUser.getPort());
                    ds.send(dp);
                } else {
                    chatBox.append("<" + "System" + ">:  " + "Username not found" + "\n");
                    System.out.println("NULL USER");
                }
                break;
            case "msg":
                if (splitUserInput.length >= 3) {
                    String[] msg = new String[splitUserInput.length - 2];
                    System.arraycopy(splitUserInput, 2, msg, 0, splitUserInput.length - 2);
                    String info = String.join(" ", msg);
                    chatBox.append("<" + username + " [MS]" + ">:  " + info + "\n");
                    commandString = splitUserInput[0].toUpperCase() + " "
                            + username + " "
                            + info;
                    commandBytes = commandString.getBytes(StandardCharsets.UTF_8);
                    dp = new DatagramPacket(commandBytes, commandBytes.length, addr, destMsPort);
                    ms.send(dp);
                }
                break;
            case "listfiles":
                destUser = findUserByName(splitUserInput[1]);
                if (destUser != null) {
                    commandString = splitUserInput[0].toUpperCase() + " " + splitUserInput[1];
                    commandBytes = commandString.getBytes(StandardCharsets.UTF_8);
                    dp = new DatagramPacket(commandBytes, commandBytes.length, destUser.addr, destUser.getPort());
                    ds.send(dp);
                } else {
                    chatBox.append("<" + "System" + ">:  " + "Username not found" + "\n");
                    System.out.println("NULL USER");
                }
                break;
            case "files": //not needed
                break;
            case "downfile": //DOWNFILE [apelido] filename UDP
                destUser = findUserByName(splitUserInput[1]);
                if (destUser != null) {
                    String c = splitUserInput[0].toUpperCase() + " " + splitUserInput[1]
                            + " " + splitUserInput[2];
                    byte[] cBytes = c.getBytes(StandardCharsets.UTF_8);
                    DatagramPacket go = new DatagramPacket(cBytes, cBytes.length, destUser.addr, destUser.getPort());
                    chatBox.append("<" + "System" + ">:  " + "GETTING DOWNLOAD INFO" + "\n");
                    ds.send(go);
                } else {
                    chatBox.append("<" + "System" + ">:  " + "Username not found" + "\n");
                    System.out.println("NULL USER");
                }
                break;
            case "downinfo": //not needed
                break;
            case "leave":
                String leave = splitUserInput[0].toUpperCase() + " " + username;
                byte[] leaveBytes = leave.getBytes(StandardCharsets.UTF_8);
                DatagramPacket leavePacket = new DatagramPacket(leaveBytes, leaveBytes.length, addr, destMsPort);
                ms.send(leavePacket);
                ms.leaveGroup(addr);
                System.exit(0);
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
