
package ds.udp.file_transfer;

import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
/**
 * 
 * @author yuzo
 * Description:
 * Update 1: falta implementar a ordenação dos pacotes pelo sequenceNumber.
 */
public class Server {

    public static String fileName = "";
    public static String decodedDataUsingUTF8 = null;
    public static byte[] MD5_Integrity;
    public static int fileSize;
    public static int lastPacketSize;

    public static void main(String args[]) {
        try (DatagramSocket socket = new DatagramSocket(5555)) {
            
            while (true) {
                //get file name
                byte[] receiveFileName = new byte[1024];
                DatagramPacket receiveFileNamePacket = new DatagramPacket(
                        receiveFileName, receiveFileName.length);
                socket.receive(receiveFileNamePacket);
                decodedDataUsingUTF8 = new String(receiveFileName,
                        "UTF-8");
                String savedFileName = decodedDataUsingUTF8.trim();
                fileName = savedFileName;

                File file = new File(fileName);
                FileOutputStream outToFile = new FileOutputStream("/home/yuzo/NetBeansProjects/downloaded_pictures/" + file);
                
                //get file size
                byte[] receiveFileSize = new byte[1024];
                DatagramPacket receiveFileSizePacket = new DatagramPacket(
                        receiveFileSize, receiveFileName.length);
                socket.receive(receiveFileSizePacket);
                int size = ByteBuffer.wrap(receiveFileSize).getInt();
                
                //calculate last packet size
                if(size % 1021 == 0) {
                    lastPacketSize = 1021;
                } else {
                    lastPacketSize = size % 1021;
                }
                
                //get file
                acceptTranferOfFile(outToFile, socket);
                
                //get md5 hash
                byte[] receiveMD5Integrity = new byte[16];
                DatagramPacket receiveFileIntegrity = new DatagramPacket(
                        receiveMD5Integrity, receiveMD5Integrity.length);
                socket.receive(receiveFileIntegrity);
                
                //calculate md5 hash of received file
                String filePath = "/home/yuzo/NetBeansProjects/downloaded_pictures/" + savedFileName;
                FileIntegrityVerification integrity = new FileIntegrityVerification(filePath);
                byte[] createMD5Integrity = integrity.getMD5();
                
                //verify integrity
                System.out.println("File Integrity Verification returned " 
                        + new FileIntegrityVerification().compareHash(
                                createMD5Integrity, receiveMD5Integrity)
                                + ".");
            }
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }
    }

    public static void acceptTranferOfFile(FileOutputStream outToFile,
            DatagramSocket socket) throws IOException {
        boolean flag; //last packet
        int sequenceNumber = 0;
        int last = 0;

        while (true) {
            byte[] data = new byte[1024];
            DatagramPacket receivedPacket = new DatagramPacket(
                    data, data.length);
            socket.receive(receivedPacket);

            sequenceNumber = ((data[0] & 0xff) << 8) + (data[1] & 0xff);
            flag = (data[2] & 0xff) == 1;

            if (sequenceNumber == (last + 1)) {
                last = sequenceNumber;
                byte[] fileData = null;
                if(flag) {
                    fileData = new byte[lastPacketSize]; //dynamic fileData size
                } else {
                    fileData = new byte[1021];
                }
                System.arraycopy(data, 3, fileData, 0, fileData.length);
                System.out.println("'" + fileData.length + "'");
                outToFile.write(fileData);
                System.out.println("Packet Received: " + sequenceNumber);
                decodedDataUsingUTF8 = new String(fileData,
                        "UTF-8");
            } else {
                System.out.println("Error.");
            }

            if (flag) {
                outToFile.close();
                break;
            }
        }
    }
}