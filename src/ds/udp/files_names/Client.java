package ds.udp.files_names;

import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author yuzo
 */
public class Client {

    public static void main(String args[]) {
        try {
            DatagramSocket socket = new DatagramSocket(6666);
            InetAddress serverAddr = InetAddress.getByName("127.0.0.1");
            String filePath = "/home/yuzo/NetBeansProjects/pictures/";
            Thread rec = new Thread(new Receiver(socket, serverAddr, 5555, filePath));
            Thread sen = new Thread(new Sender(socket, serverAddr, 5555, filePath));
            rec.start();
            sen.start();
        } catch (SocketException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
