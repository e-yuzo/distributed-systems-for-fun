
package ds.tcp.introduction.thread_classes;

import ds.tcp.introduction.gui_classes.GUIClientEvolved;
import java.net.*;
import java.io.*;
import java.util.Scanner;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author yuzo
 */
public class ThreadClientGUI {

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
                GUIClientEvolved clientGUI = new GUIClientEvolved();
                clientGUI.display();
                new Thread(new TCPClientSenderGUI(clientSocket, input, output,
                        clientGUI)).start();
                new Thread(new TCPClientReceiverGUI(clientSocket, input, output,
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
    GUIClientEvolved gui;

    public TCPClientSenderGUI(Socket clientSocket, DataInputStream in,
            DataOutputStream out, GUIClientEvolved gui) {
        this.clientSocket = clientSocket;
        this.in = in;
        this.out = out;
        this.gui = gui;
    }

    @Override
    public void run() {
        Scanner reader = new Scanner(System.in);
        String buffer;
        try {
            while (true) {
                buffer = reader.nextLine();
                out.writeUTF(buffer);
                if (buffer.equals("SAIR")) {
                    break;
                }
            }
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(TCPClientSenderGUI.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
                System.exit(0);
            } catch (IOException ioe) {
                System.err.println("IOE: " + ioe);
            }
        }
    }
}

class TCPClientReceiverGUI implements Runnable {

    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;
    GUIClientEvolved gui;

    public TCPClientReceiverGUI(Socket clientSocket, DataInputStream in,
            DataOutputStream out, GUIClientEvolved gui) {
        this.clientSocket = clientSocket;
        this.in = in;
        this.out = out;
        this.gui = gui;
    }

    @Override
    public void run() {
        try {
            String buffer;
            while (true) {
                buffer = in.readUTF();
                System.out.println(buffer);
                if (buffer.equals("SAIR")) {
                    break;
                }
            }
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(TCPClientReceiver.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
                System.exit(0);
            } catch (IOException ioe) {
                System.err.println("IOE: " + ioe);
            }
        }
    }
}
