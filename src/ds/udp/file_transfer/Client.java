
package ds.udp.file_transfer;

import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

                String filePath = "/home/yuzo/NetBeansProjects/pictures/";
                System.out.print("File name: ");
                String fileName = reader.nextLine().trim();

                //SEND file name
                byte[] saveFileAs = fileName.getBytes(StandardCharsets.UTF_8);
                DatagramPacket fileStatPacket = new DatagramPacket(
                        saveFileAs, saveFileAs.length, serverAddr,
                        serverPort);
                socket.send(fileStatPacket);

                File file = new File(filePath + fileName);

                //SEND file size
                byte[] fileSize = ByteBuffer.allocate(1024)
                        .putInt((int) file.length()).array();
                DatagramPacket fileSizePacket = new DatagramPacket(
                        fileSize, fileSize.length, serverAddr,
                        serverPort);
                socket.send(fileSizePacket);

                //SEND file content
                byte[] fileByteArray = Files.readAllBytes(file.toPath());
                sendFile(socket, fileByteArray, serverAddr, serverPort);

                System.out.println("File sent.");

                //SEND md5 hash
                FileIntegrityVerification integrity = new FileIntegrityVerification(
                        filePath + fileName);
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

    //confirmation: if third byte is -1, then adjust index i, if it's 0 then it's all good
    public static void sendFile(DatagramSocket socket,
            byte[] fileByteArray,
            InetAddress serverAddr, int serverPort) throws IOException {
        int sequenceNumber = 0;
        boolean flag;
        DatagramPacket sendPacket;
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
            
            sendPacket = new DatagramPacket(message, message.length,
                     serverAddr, serverPort);
            socket.send(sendPacket);
            
            System.out.println("Sending Packet Number: " + sequenceNumber);
            
            //await confirmation
            byte[] data = new byte[3];
            DatagramPacket receivedPacket = new DatagramPacket(
                    data, data.length);
            socket.receive(receivedPacket);
            int confirmation = (int) data[2];
            int sequenceNumberRequired;
            if (confirmation == 0) {
                continue;
            } else { //adjusting variables
                sequenceNumberRequired = ((data[0] & 0xff) << 8) + (data[1] & 0xff);
                sequenceNumber = sequenceNumberRequired - 1; //sequnceNumber will be incremented again
                i = (sequenceNumber * i) - 1021;
            }
        }
    }
    
    
}