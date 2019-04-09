/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ds.tcp.introduction.thread_classes;

import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author a1354698
 */
public class ThreadClient {

    public static void main(String args[]) {

        Socket clientSocket = null;
        int serverPort = 6666;
        InetAddress serverAddr = null;
        DataOutputStream output = null;
        DataInputStream input = null;

        try {
            serverAddr = InetAddress.getByName("127.0.0.1");
            clientSocket = new Socket(serverAddr, serverPort);
            output = new DataOutputStream(clientSocket.getOutputStream());
            input = new DataInputStream(clientSocket.getInputStream());
            TCPClientSender clientSender = new TCPClientSender(clientSocket, input, output);
            clientSender.start();
            TCPClientReceiver clientReceiver = new TCPClientReceiver(clientSocket, input, output);
            clientReceiver.start();
        } catch (UnknownHostException ex) {
            Logger.getLogger(ThreadClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ThreadClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

class TCPClientSender extends Thread {

    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;

    public TCPClientSender(Socket clientSocket, DataInputStream in, DataOutputStream out) {
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
            Logger.getLogger(TCPClientSender.class.getName()).log(Level.SEVERE, null, ex);
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

class TCPClientReceiver extends Thread {

    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;

    public TCPClientReceiver(Socket clientSocket, DataInputStream in, DataOutputStream out) {
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
            Logger.getLogger(TCPClientSender.class.getName()).log(Level.SEVERE, null, ex);
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
