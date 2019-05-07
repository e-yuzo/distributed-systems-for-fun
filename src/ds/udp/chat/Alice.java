/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ds.udp.chat;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.io.*;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author yuzo
 */
class Alice {

    static Vector<User> blockedUsers = new Vector<User>(); //blocked users
    static Vector<User> userList = new Vector<User>(); //every single user
    static Vector<User> acceptedUsers = new Vector<User>(); //accepted users
    static String username; //this is unique
    static GUIPeer gui;
    //static GUIPeer gui;

    public static void main(String[] args) {
        DatagramSocket datagramSocket = null;

        try {
            datagramSocket = new DatagramSocket(5555);
        } catch (SocketException ex) {
            Logger.getLogger(PeerA.class.getName()).log(Level.SEVERE, null, ex);
        }
        username = JOptionPane.showInputDialog("Whats your username now?");
        gui = new GUIPeer("User A");
        PeerThreadOut peerThreadOut = new PeerThreadOut(datagramSocket);
        PeerThreadIn peerThreadIn = new PeerThreadIn(datagramSocket);
        peerThreadIn.start();
        peerThreadOut.start();
        gui.updateDisplay(blockedUsers, acceptedUsers, userList);
    }

    static String messageFormatter(String user, String command, String data) {
        String comm = "";
        if (command.equals("Request Friendship")) {
            comm = "add";
        } else if (command.equals("Just a Message")) {
            comm = "msg";
        }

        String message = user + "," + comm + "," + data;
        return message;
    }

    static void messageHandler(String buffer, InetAddress address) {
        String[] splittedMessage = buffer.split(",");
        System.out.println(buffer);
        System.out.println(splittedMessage[0] + splittedMessage[1] + splittedMessage[2]);
        String command = splittedMessage[1];
        String nickname = splittedMessage[0];
        String data = splittedMessage[2];
        User userOrigin = getUserByNickname(nickname);
        switch (command.toLowerCase()) {
            case "add":
                System.out.println("hey");
                userListInsert(nickname, address);
                gui.appendChatArea("[" + nickname
                        + "] is online. Type 'accept:[nickname]' to accept.");
                gui.appendErrorsLog("Trying to send add request.");
                break;

            case "msg":
                if (userOrigin != null) {
                    if (acceptedUsers.contains(userOrigin)) {
                        gui.appendChatArea(data);
                    }
                }
                gui.appendErrorsLog("Trying to send a message.");
                break;

        }
        gui.updateDisplay(blockedUsers, acceptedUsers, userList);
    }

    static boolean localCommandResolver(String command) {
        String[] splittedCommand = command.split(":");
        if (splittedCommand.length == 2) {
            User user = getUserByNickname(splittedCommand[1]);
            if (user != null) {
                switch (splittedCommand[0].toLowerCase()) {
                    case "block":
                        if (!blockedUsers.contains(user)) {
                            blockedUsers.add(user);
                        }
                        gui.appendErrorsLog("Trying to block a user.");
                        gui.updateDisplay(blockedUsers, acceptedUsers, userList);
                        return true;

                    case "unblock":
                        if (blockedUsers.contains(user)) {
                            blockedUsers.remove(user);
                        }
                        gui.appendErrorsLog("Trying to unblock a user.");
                        gui.updateDisplay(blockedUsers, acceptedUsers, userList);
                        return true;

                    case "accept":
                        if (userList.contains(user)) {
                            if (!blockedUsers.contains(user)) {
                                if (!acceptedUsers.contains(user)) {
                                    acceptedUsers.add(user);
                                }
                            }
                        }
                        gui.appendErrorsLog("Trying to accept user.");
                        gui.updateDisplay(blockedUsers, acceptedUsers, userList);
                        return true;
                }
            }
        }
        return false;
    }

    static User getUserByNickname(String nickname) {
        for (User u : userList) {
            if (u.nickname.equals(nickname)) {
                return u;
            }
        }
        return null;
    }

    static void userListInsert(String nickname, InetAddress address) {
        if (getUserByNickname(nickname) == null) {
            User user = new User();
            user.setIp(address);
            user.setNickname(nickname);
            userList.add(user);
        }
    }
}

class PeerThreadIn extends Thread {

    DatagramSocket peerSocket;

    public PeerThreadIn(DatagramSocket peerSocket) {
        this.peerSocket = peerSocket;
    }

    @Override
    public void run() {
        PeerA.gui.btSender.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String data = PeerA.gui.getTextChatField();
                boolean localCommand = PeerA.localCommandResolver(data);
                if (!localCommand) {
                    try {
                        String name = PeerA.gui.getSelectedUser();
                        String command = PeerA.gui.getSelectedCommand();
                        InetAddress serverAddr = PeerA.getUserByNickname(name).getIp();
                        String buffer = PeerA.messageFormatter(PeerA.username, command, data);
                        byte[] m = buffer.getBytes();
                        DatagramPacket request = new DatagramPacket(m, m.length, serverAddr, 6666);
                        peerSocket.send(request);
                    } catch (IOException ex) {
                        Logger.getLogger(PeerThreadIn.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }
}

class PeerThreadOut extends Thread {

    DatagramSocket peerSocket;

    public PeerThreadOut(DatagramSocket peerSocket) {
        this.peerSocket = peerSocket;
    }

    @Override
    public void run() {
        try {
            while (true) {
                byte[] buffer = new byte[1000];
                DatagramPacket dgramPacket = new DatagramPacket(buffer, buffer.length);
                peerSocket.receive(dgramPacket);
                String receivedMessage = new String(dgramPacket.getData(), 0, dgramPacket.getLength());
                System.out.println(receivedMessage);
                PeerA.messageHandler(receivedMessage, dgramPacket.getAddress());
            }
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            peerSocket.close();
        }
    }
}
