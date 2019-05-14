package ds.udp.files_names;

import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author yuzo
 */
public class Server {

    public static void main(String args[]) {
        try {
            DatagramSocket socket = new DatagramSocket(5555);
            InetAddress serverAddr = InetAddress.getByName("127.0.0.1");
            String filePath = "/home/yuzo/NetBeansProjects/downloaded_pictures/";
            Thread rec = new Thread(new Receiver(socket, serverAddr, 6666, filePath));
            Thread sen = new Thread(new Sender(socket, serverAddr, 6666, filePath));
            rec.start();
            sen.start();
        } catch (SocketException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
