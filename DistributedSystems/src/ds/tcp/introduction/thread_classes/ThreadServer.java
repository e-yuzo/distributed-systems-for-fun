/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ds.tcp.introduction.thread_classes;

import java.net.*;
import java.io.*;
import java.util.Scanner;

/**
 *
 * @author a1354698
 */
public class ThreadServer {

    public static void main(String args[]) {
        try {
            int serverPort = 6666;
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while (true) {
                System.out.println("Waiting for connections...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client Accepted...");
                ThreadServerReceiver threadReceiver = new ThreadServerReceiver(clientSocket);
                ThreadServerSender threadSender = new ThreadServerSender(clientSocket);
                threadReceiver.start();
                threadSender.start();
            }
        } catch (IOException e) {
            System.out.println("Server socket: " + e.getMessage());
        }
    }
}

class ThreadServerReceiver extends Thread {

    DataInputStream input;
    DataOutputStream output;
    Socket serverSocket;

    public ThreadServerReceiver(Socket serverSocket) {
        try {
            this.serverSocket = serverSocket;
            this.input = new DataInputStream(serverSocket.getInputStream());
            this.output = new DataOutputStream(serverSocket.getOutputStream());
        } catch (IOException ioe) {
            System.out.println("IOE: " + ioe.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            String buffer;
            while (true) {
                buffer = this.input.readUTF();
                System.out.println(buffer);
                if (buffer.equals("SAIR")) {
                    break;
                }
            }
        } catch (EOFException eofe) {
            System.out.println("EOF: " + eofe.getMessage());
        } catch (IOException ioe) {
            System.out.println("IOE: " + ioe.getMessage());
        } finally {
            try {
                this.input.close();
                this.output.close();
                this.serverSocket.close();
                System.exit(0);
            } catch (IOException ioe) {
                System.err.println("IOE: " + ioe);
            }
        }
        System.out.println("ThreadServerReceiver terminated.");
    }
}

class ThreadServerSender extends Thread {

    DataInputStream input;
    DataOutputStream output;
    Socket serverSocket;

    public ThreadServerSender(Socket serverSocket) {
        try {
            this.serverSocket = serverSocket;
            input = new DataInputStream(serverSocket.getInputStream());
            output = new DataOutputStream(serverSocket.getOutputStream());
        } catch (IOException ioe) {
            System.out.println("IOE: " + ioe.getMessage());
        }
    }

    @Override
    public void run() {
        Scanner reader = new Scanner(System.in);
        try {
            String buffer;
            while (true) {
                buffer = reader.nextLine();
                this.output.writeUTF(buffer);
                if (buffer.equals("SAIR")) {
                    break;
                }
            }
        } catch (EOFException eofe) {
            System.out.println("EOF: " + eofe.getMessage());
        } catch (IOException ioe) {
            System.out.println("IOE: " + ioe.getMessage());
        } finally {
            try {
                this.input.close();
                this.output.close();
                this.serverSocket.close();
                System.exit(0);
            } catch (IOException ioe) {
                System.err.println("IOE: " + ioe);
            }
        }
        System.out.println("ThreadServerSender terminated.");
    }
}
