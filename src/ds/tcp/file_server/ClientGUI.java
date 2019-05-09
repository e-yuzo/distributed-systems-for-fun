package ds.tcp.file_server;

import ds.tcp.request_process.FileReceiver;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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

    public void setDataStream(DataOutputStream outputStream, DataInputStream inputStream) {
        this.outputStream = outputStream;
        this.inputStream = inputStream;
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
            if (message.length() < 1) {
                // do nothing
            } else if (message.equals("..")) {
                chatBox.setText("Cleared all messages\n");
                messageField.setText("");
            } else {
                try {
                    chatBox.append("<" + username + ">:  " + message
                            + "\n");
                    String[] bananaSplit = message.split(" ");
                    if ("DELETE".equals(bananaSplit[0])
                            || "GETFILE".equals(bananaSplit[0])
                            || "ADDFILE".equals(bananaSplit[0])) {
                        fileName = bananaSplit[1];
                    }
                    String formattedRequest = requestFormatter(message);
                    outputStream.writeUTF(formattedRequest);
                    messageField.setText("");
                    handleExpectedResponse();
                } catch (IOException ex) {
                    System.out.println("IOE: " + ex.getMessage());
                }
            }
            messageField.requestFocusInWindow();
        }
    }

    String fileNameList;

    /*
    The requestFormatter returns the formatted request.
    The handleExpectedResponse handles the response for a given type of request.
     */
    public void handleExpectedResponse() {
        try {
            byte[] flagBytes = new byte[3];
            int size = inputStream.read(flagBytes);
            //byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8); //change this partttttttttttttttt
            System.out.println("responseLength: " + size);
            int[] splittedResponse = new int[3];
            splittedResponse[0] = (int) flagBytes[0]; //request or response?
            splittedResponse[1] = (int) flagBytes[1]; //command
            splittedResponse[2] = (int) flagBytes[2]; //status
            if (splittedResponse[2] == 1) { //successful
                switch (splittedResponse[1]) { //commands
                    case 1: //addfile //dont need
                        System.out.println("This command leads nowhere.");
                        break;
                    case 2: //delete
                        System.out.println("File deleted successfuly.");
                        break;
                    case 3: //getfileslist // more data
                        SwingUtilities.invokeLater(() -> {
                            try {
                                byte[] finalBytes = new byte[2];
                                inputStream.read(finalBytes);
                                int numberOfFiles = ((finalBytes[1] & 0xff) << 8) | (finalBytes[0] & 0xff);
                                System.out.println(numberOfFiles);
                                String file;
                                byte[] stream;
                                String fileList = "";
                                int nameSize;
                                for (int i = 0; i < numberOfFiles; i++) {
                                    try {
                                        //receive all file names
                                        file = inputStream.readUTF();
                                        //System.out.println(file.length());
                                        stream = file.getBytes(StandardCharsets.UTF_8);
                                        //System.out.println(stream.length);
                                        nameSize = (int) stream[0];
                                        //System.out.println(nameSize);
                                        fileList += "\n" + new String(Arrays.copyOfRange(stream, 1,
                                                nameSize + 1), StandardCharsets.UTF_8);
                                        //System.out.println(fileList);
                                    } catch (IOException ex) {
                                        Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                chatBox.append("<" + "System" + ">:  " + fileList + "\n");
                            } catch (IOException ex) {
                                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        });
                        break;
                    case 4: //getfile // more data
                        SwingUtilities.invokeLater(() -> {
                            try {
                                byte[] valueBytes = new byte[4];
                                inputStream.read(valueBytes);
                                File downloadedFile = new File("/home/yuzo/NetBeansProjects/downloaded_pictures/" + fileName);
                                //String sizeOfFile = inputStream.readUTF();
                                long sizeOfFile = ((valueBytes[3] & 0xff) << 24)
                                        | ((valueBytes[2] & 0xff) << 16)
                                        | ((valueBytes[1] & 0xff) << 8)
                                        | (valueBytes[0] & 0xff);
                                System.out.println(sizeOfFile);
                                FileOutputStream fos;
                                try {
                                    chatBox.append("<" + "System" + ">:  " + sizeOfFile
                                            + "\n");
                                    fos = new FileOutputStream(downloadedFile);
                                    int bufferSize = 4096;
                                    byte[] buffer = new byte[bufferSize];
                                    int read;
                                    while ((read = inputStream.read(buffer)) != -1) {
                                        fos.write(buffer, 0, read);
                                        //System.out.println("received" + read);
                                        if (read != bufferSize) {
                                            break;
                                        }
                                    }
                                    fos.close();
                                    System.out.println("done");
                                } catch (IOException ex) {
                                    Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        });
                        break;
                    default:
                        System.out.println("tfuk jus happend");
                }
            } else {
                System.out.println("unsuccesful tHel happnded");
            }
            //String fileName = new String(Arrays.copyOfRange(
            //        responseBytes, 3, (int) splittedResponse[2] + 3), StandardCharsets.UTF_8); //filename
        } catch (IOException ex) {
            Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String requestFormatter(String buffer) {
        String[] message = buffer.split(" ");
        //String request;
        byte[] requestBytes = new byte[2];
        requestBytes[0] = 1;
        switch (message[0]) {
            case "ADDFILE": //addfile //dont need
                requestBytes[1] = (byte) (1);
                break;
            case "DELETE": //delete //dont need
                requestBytes[1] = (byte) (2);
                int fileLength = message[1].getBytes().length;
                byte[] fileStuff = new byte[1 + fileLength];
                fileStuff[0] = (byte) (fileLength); //fileAttributes -> fileNameSize in bytes + fileNameItself
                System.arraycopy(message[1].getBytes(), 0, fileStuff, 1, fileLength);
                requestBytes = ByteUtils.combine(requestBytes, fileStuff);
                
                break;
            case "GETFILESLIST": //getfileslist // more data
                requestBytes[1] = (byte) (3);
                break;
            case "GETFILE": //getfile // more data ////after status: filesize, filecontent.
                int fileSize = message[1].getBytes().length;
                //requestedFile = message[1];
                requestBytes[1] = (byte) (4);

                byte[] fileAttributes = new byte[1 + fileSize];
                fileAttributes[0] = (byte) (fileSize); //fileAttributes -> fileNameSize in bytes + fileNameItself

                System.arraycopy(message[1].getBytes(), 0, fileAttributes, 1, fileSize);
                requestBytes = ByteUtils.combine(requestBytes, fileAttributes);
                break;
            default:
                System.out.println("I can't understand your command.");
            //request = "";
        }
        String requestString = new String(requestBytes, StandardCharsets.UTF_8);
        return requestString;
    }

    class enterServerButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            username = usernameField.getText();
            if (username.length() < 1) {
                System.out.println("Seriously, we'll just call you Bot.");
                username = "Bot";
            }
            preFrame.setVisible(false);
            display();
        }
    }
}
