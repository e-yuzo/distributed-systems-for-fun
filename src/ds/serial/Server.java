package ds.serial;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    public static void main(String[] args) {
        SerialBook sb;
        Socket s = null;
        List<SerialBook> sbList = new ArrayList<>();
        try {
            ServerSocket ss = new ServerSocket(6666);
            System.out.println("Server running...");
            s = ss.accept();
            ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
            while (true) {
                sb = (SerialBook) ois.readObject();
                System.out.println("Command: " + sb.getId());
                switch (sb.getId()) {
                    case 1:
                        sbList.add(sb);
                        break;
                    case 2:
                        List<SerialBook> toRemove = new ArrayList<>();
                        for (SerialBook book : sbList) {
                            if (book.getTitle().equals(sb.getTitle())) {
                                toRemove.add(book);
                                //System.out.println(book.getTitle());
                            }
                        }
                        sbList.removeAll(toRemove);
                        //System.out.println(sbList.size());
                        break;
                    case 3:
                        oos.writeInt(sbList.size());
                        oos.flush();
//                        oos.writeObject(sbList);
                        for (int i = 0; i < sbList.size(); i++) {
                            oos.writeObject(sbList.get(i));
                            oos.flush();
                        }
                        break;
                    default:
                        System.out.println("Nothing interesting happens.");
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                s.close();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
