package ds.tcp.request_process;

import static ds.tcp.request_process.FileSender.BUFFER;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public String requisitionHandler(String requisition) throws FileNotFoundException {
        String[] message = requisition.split(" ");
        String response = "";
        switch (message[0]) {
            case "TIME":
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                Date hour = Calendar.getInstance().getTime();
                response = sdf.format(hour);
                break;
            case "DATE":
                SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
                Date date = Calendar.getInstance().getTime();
                response = fmt.format(date);
                break;
            case "FILES":
                File folder = new File("/home/yuzo/NetBeansProjects/pictures/");
                File[] listFiles = folder.listFiles();
                for (File file : listFiles) {
                    if (file.isFile()) {
                        response += "\nfile " + file.getName();
                    } else if (file.isDirectory()) {
                        response += "\ndir " + file.getName();
                    }
                }
                if (response.length() > 0) {
                    //response = response.substring(0, response.length() - 1);
                } else {
                    response = "no files or directories found.";
                }
                //response += listFiles.length + " files/directories.";
                break;
            case "DOWN":
                File file = new File("/home/yuzo/NetBeansProjects/pictures/" + message[1]);
                if (file.exists()) {
                    //response = file.length() + " bytes";
                    //response = file.length();
                    //output.writeInt((int)file.length());
                    sendFile(file, output);
                    response = "file sent";
                } else {
                    try {
                        output.writeLong(0);
                    } catch (IOException ex) {
                        Logger.getLogger(ThreadServerManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
            case "EXIT":
                try {
                    this.input.close();
                    this.output.close();
                    this.serverSocket.close();
                    //System.exit(0);
                } catch (IOException ioe) {
                    System.err.println("IOE: " + ioe);
                }

            default:
                response = "unknown command <" + requisition + ">";
        }
        return response;
    }

    public void sendFile(File file, DataOutputStream output) {
        new FileSender(file, output).sendFile();
    }

    @Override
    public void run() {
        try {
            String buffer;
            while (true) {
                buffer = this.input.readUTF();
                System.out.println(buffer);
                String response = requisitionHandler(buffer);
                if (!response.equals("")) {
                    output.writeUTF(response);
                }
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
