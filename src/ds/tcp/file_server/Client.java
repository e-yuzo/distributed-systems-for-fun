package ds.tcp.file_server;

import utils.ByteUtils;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author yuzo
 */
public class Client {

    public static void main(String args[]) {

        Socket clientSocket;
        int serverPort = 6666;
        InetAddress serverAddr;
        DataOutputStream output;
        DataInputStream input;

        try {
            serverAddr = InetAddress.getByName("127.0.0.1");
            clientSocket = new Socket(serverAddr, serverPort);
            output = new DataOutputStream(clientSocket.getOutputStream());
            input = new DataInputStream(clientSocket.getInputStream());

            //setLookAndFeel();
            SwingUtilities.invokeLater(() -> {
                try {
                    UIManager.setLookAndFeel(UIManager
                            .getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException
                        | InstantiationException
                        | IllegalAccessException
                        | UnsupportedLookAndFeelException ex) {
                    System.out.println("LAF: " + ex.getMessage());
                }
                ClientGUI clientGUI = new ClientGUI();
                clientGUI.display();
                new Thread(new TCPClientSenderGUI(clientSocket, input, output,
                        clientGUI)).start();
            });
        } catch (UnknownHostException ex) {
            System.out.println("UHE: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("IOE: " + ex.getMessage());
        }
    }
}

class TCPClientSenderGUI implements Runnable {

    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;
    ClientGUI gui;

    public TCPClientSenderGUI(Socket clientSocket, DataInputStream in,
            DataOutputStream out, ClientGUI gui) {
        this.clientSocket = clientSocket;
        this.in = in;
        this.out = out;
        this.gui = gui;
    }

    @Override
    public void run() {
//        Scanner reader = new Scanner(System.in);
        gui.setDataStream(out, in);
//        try {
//            String buffer;
//            while (true) {
//                buffer = in.readUTF();
//                gui.chatBox.append("<" + "System" + ">:  " + buffer + "\n");
//            }
//        } catch (IOException ex) {
//            System.out.println("IOE: " + ex.getMessage());
//        } finally {
//            try {
//                in.close();
//                out.close();
//                clientSocket.close();
//                System.exit(0);
//            } catch (IOException ioe) {
//                System.err.println("IOE: " + ioe);
//            }
//        }
    }
    
    /*
    -> ADDFILE (1): adiciona um arquivo novo. (OPCIONAL)
-> DELETE (2):  remove um arquivo existente.
-> GETFILESLIST (3): retorna uma lista com o nome dos arquivos.
-> GETFILE (4): faz download de um arquivo.
    
    1 byte: requisição(1)
1 byte: código do comando
1 byte: tamanho do nome do arquivo
variável: nome do arquivo (0-255 bytes)
    
    byte[] c = new byte[a.length + b.length];
System.arraycopy(a, 0, c, 0, a.length);
System.arraycopy(b, 0, c, a.length, b.length);
    */

    public String requestFormatter(String buffer) {
        String[] message = buffer.split(" ");
        //String request;
        byte[] requestBytes = new byte[2];
        requestBytes[0] = 1;
        switch (message[0]) {
            case "ADDFILE": //addfile //dont need
                requestBytes[1] = (byte)(1);
                break;
            case "DELETE": //delete //dont need
                requestBytes[1] = (byte)(2);
                break;
            case "GETFILESLIST": //getfileslist // more data
                requestBytes[1] = (byte)(3);
                break;
            case "GETFILE": //getfile // more data ////after status: filesize, filecontent.
                int fileSize = message[1].getBytes().length;
                //requestedFile = message[1];
                requestBytes[1] = (byte)(4);
                
                byte[] fileAttributes = new byte[1 + fileSize];
                fileAttributes[0] = (byte)(fileSize); //fileAttributes -> fileNameSize in bytes + fileNameItself
                
                System.arraycopy(message[1].getBytes(), 0, fileAttributes, 1, message[1].getBytes().length);
                requestBytes = ByteUtils.combine(requestBytes, fileAttributes);
                break;
            default:
                System.out.println("I can't understand your command.");
                //request = "";
        }
        String requestString = new String(requestBytes, StandardCharsets.UTF_8);
        return requestString;
    }

    public void responseHandler(String buffer) throws IOException {
        byte[] responseBytes = buffer.getBytes(StandardCharsets.UTF_8);
        int[] splittedMessage = new int[3];
        splittedMessage[0] = (int) responseBytes[0]; //request or response?
        splittedMessage[1] = (int) responseBytes[1]; //command
        splittedMessage[2] = (int) responseBytes[2]; //status
        String fileName = new String(Arrays.copyOfRange(
                responseBytes, 3, (int) splittedMessage[2] + 3), StandardCharsets.UTF_8); //filename
        if (splittedMessage[2] == 1) { //successful
            switch (splittedMessage[1]) { //commands
                case 1: //addfile //dont need
                    break;
                case 2: //delete //dont need
                    break;
                case 3: //getfileslist // more data
                    
                    break;
                case 4: //getfile // more data
                    File downloadedFile = new File("/home/yuzo/NetBeansProjects/downloaded_pictures/" + fileName);
                    //PrintWriter writer = new PrintWriter("C:\\Users\\Yuso\\Documents\\"
                    //      + "NetBeansProjects\\TCPCommEx5\\ClientDirectory\\" + TCPClient.requestedFile, "UTF-8");
                    //System.out.print("Recebendo arquivo " + msg + "\n");
                    FileOutputStream output = new FileOutputStream(downloadedFile);
                    byte[] mybytearray = new byte[in.readInt()];
                    in.read(mybytearray, 0, mybytearray.length);
                    output.write(mybytearray);
                    break;
                default:
                    System.out.println("tfuk jus happend");
            }
        } else {
            System.out.println("unsuccesful tHel happnded");
        }
    }
}
