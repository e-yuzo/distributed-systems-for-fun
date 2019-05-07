
package ds.tcp.client_server;

import java.net.*;
import java.io.*;
import java.util.Scanner;

/**
 *
 * @author yuzo
 * Description: fazer um código para o Cliente e Servidor se comunicarem. O
 * cliente envia e recebe mensagens. O servidor envia e recebe mensagens. Quando
 * algum dos dois enviar 'SAIR', a comunicação entre eles deve ser finalizada.
 * Use o TCP.
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
            new Thread(new TCPClientSender(clientSocket, input, output)).start();
            new Thread(new TCPClientReceiver(clientSocket, input, output)).start();
        } catch (UnknownHostException ex) {
            System.out.println("UHE: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("IOE: " + ex.getMessage());
        }
    }
}

class TCPClientSender implements Runnable{

    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;

    public TCPClientSender(Socket clientSocket, DataInputStream in
            , DataOutputStream out) {
        this.clientSocket = clientSocket;
        this.in = in;
        this.out = out;
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
            System.out.println("IOE: " + ex.getMessage());
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

class TCPClientReceiver implements Runnable {

    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;

    public TCPClientReceiver(Socket clientSocket, DataInputStream in
            , DataOutputStream out) {
        this.clientSocket = clientSocket;
        this.in = in;
        this.out = out;
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
            System.out.println("IOE: " + ex.getMessage());
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
