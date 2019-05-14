package ds.udp.files_names;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.ByteUtils;

/**
 *
 * @author yuzo
 */
public class Receiver implements Runnable {

    private DatagramSocket socket;
    private InetAddress serverAddr;
    private int serverPort;
    private String path;

    public Receiver(DatagramSocket socket, InetAddress serverAddr, int serverPort,
            String path) {
        this.socket = socket;
        this.serverAddr = serverAddr;
        this.serverPort = serverPort;
        this.path = path;
    }

    public void handleRequest(boolean file, boolean dir) throws Exception {
        File folder = new File(path);
        byte[] response;
        int namelength;
        byte[] name;
        for (File entry : folder.listFiles()) {
            response = new byte[2];
            response[0] = (byte) 2;
            namelength = entry.getName().length();
            name = entry.getName().getBytes(StandardCharsets.UTF_8);
            if ((entry.isDirectory() && dir) || (entry.isFile() && file)) {
                response[1] = (byte) namelength;
                response = ByteUtils.combine(response, name);
                DatagramPacket sendPacket = new DatagramPacket(
                        response, response.length, serverAddr, serverPort);
                socket.send(sendPacket);
            }
        }
        response = new byte[2];
        response[0] = (byte) 2;
        response[1] = (byte) 0;
        DatagramPacket sendPacket = new DatagramPacket(
                response, response.length, serverAddr, serverPort);
        socket.send(sendPacket);
    }

    @Override
    public void run() {
        while (true) {
            String name;
            try {
                byte[] bytes = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(
                        bytes, bytes.length);
                socket.receive(receivePacket);
                //System.out.println("i received somthing");
                if ((int) bytes[0] == 1) {//request
                    boolean file = (int) bytes[1] == 1 ? true : false;
                    boolean dir = (int) bytes[2] == 1 ? true : false;
                    handleRequest(file, dir);
                } else if ((int) bytes[0] == 2) {//response
                    int length = (int) bytes[1];
                    if (length == 0) {
                        System.out.println("a packet has no name because it is the end");
                    } else {
                        name = new String(ByteUtils.subArray(bytes, 2, length + 2), StandardCharsets.UTF_8);
                        System.out.println(name);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
