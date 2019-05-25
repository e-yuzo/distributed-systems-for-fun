package ds.tcp.pipes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.net.ServerSocket;
import java.net.Socket;
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
//            EstablishedSockets establishedSockets = new EstablishedSockets();

            PipedWriter pipeO1 = new PipedWriter();
            PipedWriter pipeO2 = new PipedWriter();
            PipedReader pipeI1 = new PipedReader(pipeO2);
            PipedReader pipeI2 = new PipedReader(pipeO1);
            //first client
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client 1 Accepted...");
            ThreadServerManager c1 = new ThreadServerManager(clientSocket);
            c1.setPipes(pipeO1, pipeI1);
            new Thread(c1).start();

            //second client
            Socket clientSocket2 = serverSocket.accept();
            System.out.println("Client 2 Accepted...");
            ThreadServerManager c2 = new ThreadServerManager(clientSocket2);
            c2.setPipes(pipeO2, pipeI2);
            new Thread(c2).start();
//                establishedSockets.addSocket(clientSocket);
        } catch (IOException e) {
            System.out.println("Server socket: " + e.getMessage());
        } finally {

        }
    }
}

class InputPipe implements Runnable {

    PipedReader inputPipe;
    DataOutputStream outputData;

    public InputPipe(PipedReader inputPipe, DataOutputStream outputData) {
        this.inputPipe = inputPipe;
        this.outputData = outputData;
    }

    @Override
    public void run() {
        String buffer;
        try {
            while (true) {
                buffer = "";
                int i;
                char[] cbuf = new char[1024];
                while ((i = inputPipe.read(cbuf)) != -1) {
                    System.out.println("Size of i: " + i);
                    buffer = new String(cbuf, 0, i / 2);
                    if (i < 1024) {
                        break;
                    }
                }
                System.out.println("Reading: " + buffer);
                outputData.writeUTF(buffer);
                if (buffer.equals("SAIR")) {
                    break;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(OutputPipe.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

class OutputPipe implements Runnable {

    PipedWriter outputPipe;
    DataInputStream outputData;

    public OutputPipe(PipedWriter outputPipe, DataInputStream outputData) {
        this.outputPipe = outputPipe;
        this.outputData = outputData;
    }

    @Override
    public void run() {
        String buffer = "";
        try {
            while (true) {
                buffer = this.outputData.readUTF();
                for (int i = 0; i < buffer.length(); i++) {
                    outputPipe.write(buffer.charAt(i));
                }
                System.out.println("Writing: " + buffer);
                outputPipe.write(buffer);
                outputPipe.flush();
                //System.out.println(buffer);
                if (buffer.equals("SAIR")) {
                    break;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(OutputPipe.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

class ThreadServerManager implements Runnable {

    DataInputStream input;
    DataOutputStream output;
    PipedReader inputPipe;
    PipedWriter outputPipe;
    Socket serverSocket;

    public void setPipes(PipedWriter outputPipe, PipedReader inputPipe) {
        this.inputPipe = inputPipe;
        this.outputPipe = outputPipe;
    }

    public void initThreadedPipes() {
        new Thread(new InputPipe(inputPipe, output)).start();
        new Thread(new OutputPipe(outputPipe, input)).start();
    }

    public ThreadServerManager(Socket serverSocket) {
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
        initThreadedPipes();
    }
}
