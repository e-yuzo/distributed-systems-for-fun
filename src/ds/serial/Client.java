package ds.serial;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    public static void main(String[] args) {
        Socket s = null;
        Scanner scan = new Scanner(System.in);
        try {
            s = new Socket(InetAddress.getByName("127.0.0.1"), 6666);
            ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
//            oos.writeObject(p);
//            oos.flush();
            while (true) {
                System.out.println("\n\nCommand: [(1:ADD), (2:REMOVE), (3:LIST)]");

                System.out.print("\nCommand: ");
                int command = Integer.valueOf(scan.nextLine());

                switch (command) {
                    case 1:
                        System.out.print("\nTitle: ");
                        String title = scan.nextLine();
                        System.out.print("\nPrice: ");
                        float price = Float.valueOf(scan.nextLine());
                        SerialBook sb = new SerialBook(command, title, price);
                        oos.writeObject(sb);
                        oos.flush();
                        break;
                    case 2:
                        String titleRemove = scan.nextLine();
                        SerialBook sbRemove = new SerialBook(command, titleRemove, -1);
                        oos.writeObject(sbRemove);
                        oos.flush();
                        break;
                    case 3:
                        SerialBook sbListing = new SerialBook(command, "", -1);
                        oos.writeObject(sbListing);
                        oos.flush();
                        int records = ois.readInt();
                        for (int i = 0; i < records; i++) {
                            SerialBook book = (SerialBook) ois.readObject();
                            System.out.println("\nTitle: " + book.getTitle() + "     Price: " + book.getPrice());
                        }
                        break;
                    default:
                        System.out.println("Nothing interesting happens.");
                }
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                s.close();
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
