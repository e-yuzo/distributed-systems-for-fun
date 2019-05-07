
package ds.udp.file_transfer;

import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Scanner;

/**
 *
 * @author yuzo
 */
public class Client {

    public static void main(String args[]) {
        DatagramSocket socket;
        try {
            socket = new DatagramSocket();
            InetAddress serverAddr = InetAddress.getByName("127.0.0.1");
            int serverPort = 5555;
            Scanner reader = new Scanner(System.in);

            while (true) {
//                System.out.println("File Path: ");
//                String filePath = reader.nextLine();
//                System.out.println("Dest. File Name: ");
//                String fileName = reader.nextLine();
//                System.out.println("Sending file:'" + filePath + "'.");

                String filePath = "/home/yuzo/NetBeansProjects/pictures/";
                System.out.println("File name: ");
                String fileName = reader.nextLine();
                
                filePath += fileName;
                //send file name
                byte[] saveFileAs = fileName.getBytes();
                DatagramPacket fileStatPacket = new DatagramPacket(
                        saveFileAs, saveFileAs.length, serverAddr,
                        serverPort);
                socket.send(fileStatPacket); //send file name

                //
                File file = new File(filePath);

                //send file size
                byte[] filesize = ByteBuffer.allocate(1024)
                        .putInt((int) file.length()).array();
                DatagramPacket fileSizePacket = new DatagramPacket(
                        filesize, saveFileAs.length, serverAddr,
                        serverPort);
                socket.send(fileSizePacket);

                //send file
                byte[] fileByteArray = Files.readAllBytes(file.toPath());
                sendFile(socket, fileByteArray, serverAddr, serverPort);

                System.out.println("File sent.");

                //send md5 hash
                FileIntegrityVerification integrity = new FileIntegrityVerification(
                        filePath);
                byte[] md5 = integrity.getMD5(); //16 bytes
                DatagramPacket fileMD5Packet = new DatagramPacket(
                        md5, md5.length, serverAddr,
                        serverPort);
                socket.send(fileMD5Packet); //send hash

                System.out.println("MD5 sent.");
            }
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }
    }

    public static void sendFile(DatagramSocket socket,
            byte[] fileByteArray,
            InetAddress serverAddr, int serverPort) throws IOException {
        int sequenceNumber = 0;
        boolean flag;

        for (int i = 0; i < fileByteArray.length; i = i + 1021) {
            sequenceNumber += 1;
            byte[] message = new byte[1024];
            message[0] = (byte) (sequenceNumber >> 8);
            message[1] = (byte) (sequenceNumber);

            if ((i + 1021) >= fileByteArray.length) {
                flag = true; //last message
                message[2] = (byte) (1);
            } else {
                flag = false;
                message[2] = (byte) (0);
            }

            if (!flag) {
                System.arraycopy(fileByteArray, i, message, 3, 1021);
            } else {
                System.arraycopy(fileByteArray, i, message, 3, fileByteArray.length - i);//last message
            }
            
            DatagramPacket sendPacket = new DatagramPacket(message, message.length,
                     serverAddr, serverPort);
            byte[] newArray = Arrays.copyOfRange(message, 3, 1021);
            //System.out.println("lol?:" + new String(newArray));
            socket.send(sendPacket);
            System.out.println("Sending Packet Number: " + sequenceNumber);
        }
    }
}