/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ds.udp.files_names;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author yuzo
 */
public class Sender implements Runnable {

    private DatagramSocket socket;
    private InetAddress serverAddr;
    private int serverPort;
    private String path;

    public Sender(DatagramSocket socket, InetAddress serverAddr, int serverPort,
            String path) {
        this.socket = socket;
        this.serverAddr = serverAddr;
        this.serverPort = serverPort;
        this.path = path;
    }

    public void sendRequest(boolean file, boolean dir) throws Exception {
        DatagramPacket sendPacket;
        byte[] choice = {(byte) 1, (byte) 0, (byte) 0}; //first byte: request or response
        if (file) {
            choice[1] = (byte) 1;
        }
        if (dir) {
            choice[2] = (byte) 1;
        }
        sendPacket = new DatagramPacket(choice, choice.length, serverAddr,
                serverPort);
        socket.send(sendPacket);
        System.out.println("requestSent");
    }

    @Override
    public void run() {
        while (true) {
            Scanner reader = new Scanner(System.in);
            try {
                /**
                 * 1 1: files and dirs 1 0: only files 0 1: only dirs
                 */
                System.out.print("Request: ");
                String choices = reader.nextLine().trim();
                String[] split = choices.split(" ");
                if (split.length == 2) {
                    boolean file = split[0].equals("1") ? true : false;
                    boolean dir = split[1].equals("1") ? true : false;
                    if (!file && !dir) {
                        System.out.println("command not allowed");
                    } else {
                        sendRequest(file, dir);
                    }
                } else {
                    System.out.println("command not allowed");
                }
            } catch (IOException ex) {
                Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
