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
import java.util.Arrays;

/**
 *
 * @author yuzo
 */
public class Server {

    public static void main(String args[]) {
        try {
            int serverPort = 6666;
            ServerSocket serverSocket = new ServerSocket(serverPort);
            //EstablishedSockets establishedSockets = new EstablishedSockets();
            while (true) {
                System.out.println("Waiting for connections...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client Accepted...");
                new Thread(new ThreadServerManager(clientSocket)).start();
                //establishedSockets.addSocket(clientSocket);
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
    //EstablishedSockets socketList;
    //List<DataOutputStream> outputStreams = new ArrayList<>();

    public ThreadServerManager(Socket serverSocket) {
        try {
            this.serverSocket = serverSocket;
            this.input = new DataInputStream(serverSocket.getInputStream());
            this.output = new DataOutputStream(serverSocket.getOutputStream());
            //this.socketList = establishedSockets;
            //initOutputStreams();
        } catch (IOException ioe) {
            System.out.println("IOE: " + ioe.getMessage());
        }
    }

    public void requestHandler(String buffer) throws IOException {
        byte[] requestBytes = buffer.getBytes(StandardCharsets.UTF_8);
        int[] splittedMessage = new int[3];
        splittedMessage[0] = (int) requestBytes[0];  //request or response?
        splittedMessage[1] = (int) requestBytes[1];  //command
        //splittedMessage[2] = (int) requestBytes[2];  //file size
        //String fileName = new String(Arrays.copyOfRange(
        //        intoBytes, 3, splittedMessage[2] + 3), StandardCharsets.UTF_8); //filename

        //String response = null;
        byte[] responseBytes = new byte[3];
        responseBytes[0] = (byte) 2;
        responseBytes[1] = requestBytes[1];
        byte[] extraBytes;

        switch (splittedMessage[1]) {
            case 1: //addfile \\not needed lol
                break;
            case 2: //delete
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
                //String response = new String(actualResponse, StandardCharsets.UTF_8);
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
                //System.out.println("njomokmomknjhi");
                File file = new File("/home/yuzo/NetBeansProjects/pictures/" + fileName);
                if (file.exists()) { //after status: filename, filesize, filecontent.
                    //send confirmation
                    responseBytes[2] = 1; //1 means file exists
                    long fileSize = file.length();
                    //System.out.println("asdass"+ fileSize);
                    extraBytes[0] = (byte) (fileSize & 0xFF);
                    extraBytes[1] = (byte) ((fileSize >> 8) & 0xFF);
                    extraBytes[2] = (byte) ((fileSize >> 16) & 0xFF);
                    extraBytes[3] = (byte) ((fileSize >> 24) & 0xFF);
//                    ByteBuffer fileSize = ByteBuffer.allocate(4); //always big endian.
//                    fileSize.putInt((int) file.length());
                    byte[] confirmationBytes = ByteUtils.combine(responseBytes, extraBytes);
                    //System.out.println("confirmLength: " + Arrays.toString(confirmationBytes));
                    //String confirmation = new String(confirmationBytes, );
                    //byte[] asd = confirmation.getBytes(StandardCharsets.UTF_8);
                    //System.out.println("asdLength: " + Arrays.toString(asd));
                    //output.writeBytes(confirmation);
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
                        //System.out.println("sent" + read);
                    }

                    //byte[] tempFileSize = fileSize.array();
                    //System.arraycopy(tempFileSize, 0, responseBytes, 3, 6);
//                    responseBytes[3] = tempFileSize[0];
//                    responseBytes[4] = tempFileSize[1];
//                    responseBytes[5] = tempFileSize[2];
//                    responseBytes[6] = tempFileSize[3];
//                    bis.read(mybytearray, 0, mybytearray.length);
//                    System.arraycopy(mybytearray, 0, responseBytes, 7, (int) file.length());
                } else {
                    responseBytes[2] = 2; //file doesnt exists
                }
                break;
            default:
                responseBytes[2] = 2;
        }
        //try {
        //response = new String(responseBytes, StandardCharsets.UTF_8);
        //output.writeBytes(response);
        //} catch (IOException ex) {
        //}
    }

    @Override
    public void run() {
        try {
            String buffer;
            while (true) {
                buffer = this.input.readUTF();
                //System.out.println(buffer);
                requestHandler(buffer);
                //output.writeUTF(response);
                //updateOutputStreams();
//                for (DataOutputStream dos : this.outputStreams) {
//                    dos.writeUTF(buffer);
//                }
            }
        } catch (EOFException eofe) {
            System.out.println("EOF: " + eofe.getMessage());
        } catch (IOException ioe) {
            System.out.println("IOE: " + ioe.getMessage());
        }
        System.out.println("ThreadServerReceiver terminated.");
    }
}
