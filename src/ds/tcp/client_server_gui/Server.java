
package ds.tcp.client_server_gui;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author yuzo
 */
public class Server {

    public static void main(String args[]) {
        try {
            int serverPort = 6666;
            ServerSocket serverSocket = new ServerSocket(serverPort);
            EstablishedSockets establishedSockets = new EstablishedSockets();
            while (true) {
                System.out.println("Waiting for connections...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client Accepted...");
                new Thread(new ThreadServerManager(clientSocket, establishedSockets)).start();
                establishedSockets.addSocket(clientSocket);
                
            }
        } catch (IOException e) {
            System.out.println("Server socket: " + e.getMessage());
        } finally {

        }
    }
}

class EstablishedSockets {

    private List<Socket> socket;

    public EstablishedSockets() {
        socket = new ArrayList<>();
    }

    public void addSocket(Socket socket) {
        this.socket.add(socket);
    }

    public List<Socket> getSocket() {
        return this.socket;
    }

    public void removeSocket(Socket socket) {//modificar para remover se um socket foi closed
        //verificar com o método isClosed();
        for (Socket skt : this.socket) {
            if (socket == skt) {
                this.socket.remove(skt);
            }
        }
    }
}

class ThreadServerManager implements Runnable {

    DataInputStream input;
    DataOutputStream output;
    Socket serverSocket;
    EstablishedSockets socketList;
    List<DataOutputStream> outputStreams = new ArrayList<>();

    public ThreadServerManager(Socket serverSocket,
             EstablishedSockets establishedSockets) {
        try {
            this.serverSocket = serverSocket;
            this.input = new DataInputStream(serverSocket.getInputStream());
            this.output = new DataOutputStream(serverSocket.getOutputStream());
            this.socketList = establishedSockets;
            initOutputStreams();
        } catch (IOException ioe) {
            System.out.println("IOE: " + ioe.getMessage());
        }
    }

    private void initOutputStreams() {
        List<Socket> sockets = this.socketList.getSocket();
        sockets.forEach((socket) -> {
            try {
                this.outputStreams.add(new DataOutputStream(socket
                        .getOutputStream()));
            } catch (IOException ex) {
                System.out.println("IOE: " + ex.getMessage());
            }
        });
    }

    private void updateOutputStreams() { // "otimizar" esse método sem precisar reinicializar a lista.
        List<Socket> sockets = this.socketList.getSocket();
        int socketListSize = sockets.size(); //6
        int outputStreamSize = this.outputStreams.size(); //4
        if (socketListSize - outputStreamSize > 1) {
            for (int i = outputStreamSize + 1; i < socketListSize; i++) {
                try {
                    this.outputStreams.add(new DataOutputStream(sockets.get(i)
                            .getOutputStream()));
                } catch (IOException ex) {
                    System.out.println("IOE: " + ex.getMessage());
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            String buffer;
            while (true) {
                buffer = this.input.readUTF();
                System.out.println(buffer);
                updateOutputStreams();
                for (DataOutputStream dos : this.outputStreams) {
                    dos.writeUTF(buffer);
                }
                if (buffer.equals("SAIR")) {
                    break;
                }
            }
        } catch (EOFException eofe) {
            System.out.println("EOF: " + eofe.getMessage());
        } catch (IOException ioe) {
            System.out.println("IOE: " + ioe.getMessage());
        } finally {
            try {
                this.input.close();
                this.output.close();
                this.serverSocket.close();
                System.exit(0);
            } catch (IOException ioe) {
                System.err.println("IOE: " + ioe);
            }
        }
        System.out.println("ThreadServerReceiver terminated.");
    }
}