package ds.multicast;
/**
 * MulticastPeer: Implementa um peer multicast
 * Descricao: Envia mensagens para todos os membros do grupo.
 */

import java.net.*;
import java.io.*;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class MulticastPeer {

    public static void main(String args[]) {
        /* args[0]: ip multicast (entre 224.0.0.0 e 239.255.255.255 */
        MulticastSocket s = null;
        InetAddress group = null;
        int resp = 0;

        try {
            SwingUtilities.invokeLater(() -> {
                try {
                    UIManager.setLookAndFeel(UIManager
                            .getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException
                        | InstantiationException
                        | IllegalAccessException
                        | UnsupportedLookAndFeelException ex) {
                    System.out.println("LAF: " + ex.getMessage());
                }
//                ClientGUI clientGUI = new ClientGUI();
//                clientGUI.display();
//                new Thread(new TCPClientSenderGUI(clientSocket, input, output,
//                        clientGUI)).start();
            });
            /* cria um grupo multicast */
            group = InetAddress.getByName(args[0]);
            /* cria um socket multicast */
            s = new MulticastSocket(6789);
            /* adiciona o host ao grupo */
            s.joinGroup(group);

            /* cria a thread para receber */
            ReceiveThread receiveThread = new ReceiveThread(s);
            receiveThread.start();

            do {
                /* cria um datagrama com a msg */
                String msg = JOptionPane.showInputDialog("Mensagem?");
                byte[] m = msg.getBytes();
                DatagramPacket messageOut = new DatagramPacket(m, m.length, group, 6789);
                /* envia o datagrama como multicast */
                s.send(messageOut);
                resp = JOptionPane.showConfirmDialog(null, "Nova mensagem?",
                        "Continuar", JOptionPane.YES_NO_OPTION);
            } while (resp != JOptionPane.NO_OPTION);

            /* retira-se do grupo */
            s.leaveGroup(group);
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (s != null) {
                s.close(); //fecha o socket
            }
        } //finally
    } //main		      	
}//class

class ReceiveThread extends Thread {

    MulticastSocket multicastSocket = null;

    public ReceiveThread(MulticastSocket multicastSocket) {
        this.multicastSocket = multicastSocket;
    }

    public void run() {
        try {
            while (true) {
                byte[] buffer = new byte[1000];
                DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(messageIn);
                System.out.println("Recebido:" + new String(messageIn.getData(), 0, messageIn.getLength()));
            }
        } catch (Exception e) {
            System.out.println(e);
        }

    }
}
