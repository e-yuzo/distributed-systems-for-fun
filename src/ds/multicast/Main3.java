package ds.multicast;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author yuzo
 */
public class Main3 {

    public static void main(String args[]) {
        SwingUtilities.invokeLater(() -> {
            try {
                try {
                    UIManager.setLookAndFeel(UIManager
                            .getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException
                        | InstantiationException
                        | IllegalAccessException
                        | UnsupportedLookAndFeelException ex) {
                    System.out.println("LAF: " + ex.getMessage());
                }
                // setting up sockets
                InetAddress group = InetAddress.getByName("225.1.2.10");
                int msPort = 6789;
                MulticastSocket ms = new MulticastSocket(msPort);
//                    ms.joinGroup(group); this will happen after communication is happenning
                int dPort = 6799;
                int fsPort = 7777;
                DatagramSocket ds = new DatagramSocket(dPort);
//                DataOutputStream output;
//                DataInputStream input;

                //InetAddress peerAddr = InetAddress.getByName("127.0.0.1");
                //Socket peerSocket = new Socket(peerAddr, ); //for file transfer
                // those will be created after
//                output = new DataOutputStream(peerSocket.getOutputStream());
//                input = new DataInputStream(peerSocket.getInputStream());
//                cg.setStuff
                ClientGUI cg = new ClientGUI();
                cg.setIO(ms, ds, group, msPort, fsPort, 6799, "burgerking", 6789);

//                cg.set cg // set variables in GUI, must set on other thread too (for UDP/TCP ?)
                //cg.setMulticastGroup();
                cg.initializeThreads();
                cg.display();
//                ms = new MulticastSocket(6789);
//                ClientGUI clientGUI = new ClientGUI();
//                clientGUI.display();
//                new Thread(new TCPClientSenderGUI(clientSocket, input, output,
//                        clientGUI)).start();
            } catch (UnknownHostException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        /* cria um grupo multicast */
        //group = InetAddress.getByName(args[0]);
        /* cria um socket multicast */
        //ms = new MulticastSocket(6789);
        /* adiciona o host ao grupo */
//            ms.joinGroup(group);

        /* cria a thread para receber */
//            ReceiveThread receiveThread = new ReceiveThread(ms);
//            receiveThread.start();
//            do {
        /* cria um datagrama com a msg */
//                String msg = JOptionPane.showInputDialog("Mensagem?");
//                byte[] m = msg.getBytes();
        /* retira-se do grupo */
    }
}
