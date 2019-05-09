package ds.tcp.file_server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author yuzo
 */
public class Server {

    public static void main(String args[]) {
        try {
            int serverPort = 6666;
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while (true) {
                System.out.println("Waiting for connections...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client Accepted...");
                new Thread(new ThreadServerManager(clientSocket)).start();
            }
        } catch (IOException e) {
            System.out.println("Server socket: " + e.getMessage());
        } finally {
            System.exit(0);
        }
    }
}

class ThreadServerManager implements Runnable {

    DataInputStream input;
    DataOutputStream output;
    Socket serverSocket;

    public ThreadServerManager(Socket serverSocket) {
        try {
            this.serverSocket = serverSocket;
            this.input = new DataInputStream(serverSocket.getInputStream());
            this.output = new DataOutputStream(serverSocket.getOutputStream());
        } catch (IOException ioe) {
            System.out.println("IOE: " + ioe.getMessage());
        }
    }

    public void requestHandler(String buffer) throws IOException {
        byte[] requestBytes = buffer.getBytes(StandardCharsets.UTF_8);
        int[] splittedMessage = new int[3];
        splittedMessage[0] = (int) requestBytes[0];  //request or response?
        splittedMessage[1] = (int) requestBytes[1];  //command
        byte[] responseBytes = new byte[3];
        responseBytes[0] = (byte) 2;
        responseBytes[1] = requestBytes[1];
        byte[] extraBytes;
        switch (splittedMessage[1]) {
            case 1: //addfile \\not needed lol
                break;
            case 2: //delete
                int size = (int) requestBytes[2];
                byte[] nameBytes = new byte[size];
                System.arraycopy(requestBytes, 3, nameBytes, 0, size);
                String name = new String(nameBytes, StandardCharsets.UTF_8);
                System.out.println(name);
                File dir = new File("/home/yuzo/NetBeansProjects/pictures/" + name);
                if (dir.exists()) {
                    responseBytes[2] = (byte) 1;
                    dir.delete();
                } else {
                    responseBytes[2] = (byte) 2;
                }
                output.write(responseBytes);
                break;
            case 3: //getfileslist // more data
                //send confirmation (and number of files)
                extraBytes = new byte[2];
                File folder = new File("/home/yuzo/NetBeansProjects/pictures/");
                int numberOfFiles = folder.list().length;
                extraBytes[0] = (byte) (numberOfFiles & 0xFF);
                extraBytes[1] = (byte) ((numberOfFiles >> 8) & 0xFF);
                responseBytes[2] = (byte) 1;
                byte[] actualResponse = ByteUtils.combine(responseBytes, extraBytes);
                output.write(actualResponse);
                String response;
                //send files and directories
                File[] listOfFiles = folder.listFiles();
                for (File file : listOfFiles) {
                    if (file.isFile()) {
                        System.out.println(file.getName());
                        byte[] fileSize = new byte[1];
                        fileSize[0] = (byte) file.getName().length();
                        byte[] fileNameBytes = file.getName().getBytes(StandardCharsets.UTF_8);

                        byte[] fileResponse = ByteUtils.combine(fileSize, fileNameBytes);
                        response = new String(fileResponse, StandardCharsets.UTF_8);
                        output.writeUTF(response);
                    }
                }
                break;
            case 4: //getfile // more data
                int fileNameSize = (int) requestBytes[2];
                byte[] fileNameBytes = new byte[fileNameSize];
                System.arraycopy(requestBytes, 3, fileNameBytes, 0, fileNameSize);
                String fileName = new String(fileNameBytes, StandardCharsets.UTF_8);
                System.out.println(fileName);
                extraBytes = new byte[4]; //tamanho do arquivo
                File file = new File("/home/yuzo/NetBeansProjects/pictures/" + fileName);
                if (file.exists()) { //after status: filename, filesize, filecontent.
                    //send confirmation
                    responseBytes[2] = 1; //1 means file exists
                    long fileSize = file.length();
                    extraBytes[0] = (byte) (fileSize & 0xFF);
                    extraBytes[1] = (byte) ((fileSize >> 8) & 0xFF);
                    extraBytes[2] = (byte) ((fileSize >> 16) & 0xFF);
                    extraBytes[3] = (byte) ((fileSize >> 24) & 0xFF);
//                    ByteBuffer fileSize = ByteBuffer.allocate(4); //always big endian.
//                    fileSize.putInt((int) file.length());
                    byte[] confirmationBytes = ByteUtils.combine(responseBytes, extraBytes);
                    output.write(confirmationBytes);
                    //send file
                    FileInputStream fis;
                    int BUFFER = 4096;
                    byte[] mybytearray = new byte[BUFFER];
                    fis = new FileInputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    DataInputStream dis = new DataInputStream(bis);
                    int read;
                    while ((read = dis.read(mybytearray)) != -1) {
                        output.write(mybytearray, 0, read);
                    }
                } else {
                    responseBytes[2] = 2; //file doesn't exists
                }
                break;
            default:
                responseBytes[2] = 2;
        }
    }

    @Override
    public void run() {
        try {
            String buffer;
            while (true) {
                buffer = this.input.readUTF();
                requestHandler(buffer);
            }
        } catch (EOFException eofe) {
            System.out.println("EOF: " + eofe.getMessage());
        } catch (IOException ioe) {
            System.out.println("IOE: " + ioe.getMessage());
        }
        System.out.println("ThreadServerReceiver terminated.");
    }
}
