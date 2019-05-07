/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ds.tcp.request_process;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author yuzo TODO: end thread after completing transaction
 */
public class FileReceiver {

    private DataInputStream input;
    private BufferedOutputStream bos;
    private String fileName;
    static int BUFFER = 4096;

    public FileReceiver(DataInputStream input, String fileName) {
        this.input = input;
        this.fileName = fileName;
    }
    
    public void receiveFile() {
        //FileOutputStream fos = null;
        try {
            byte[] contents = new byte[BUFFER];
            //fos = new FileOutputStream("/home/yuzo/NetBeansProjects/downloaded_pictures/" + fileName);
            int fileSize = input.readInt();
            //System.out.println(fileSize); //recebe o tamanho do arquivo em inteiro
            
            //BufferedOutputStream bos = new BufferedOutputStream(fos);
            int bytesRead = 0;
//            for (int i = 0; i < fileSize; i += BUFFER) {
//                int receivedBytes = input.read(contents);
//                System.out.println(receivedBytes);bos.write(contents, 0, receivedBytes);
//                bytesRead += BUFFER;
//            }
//            if (bytesRead < fileSize) {
//                int receivedBytes = input.read(contents);
//                bos.write(contents, 0, receivedBytes);
//            }
            while ((bytesRead = input.read(contents)) != -1) {
                bos.write(contents, 0, bytesRead);
            }
            bos.flush();
            System.out.println("File saved successfully!");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileReceiver.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FileReceiver.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
//            try {
//            } catch (IOException ex) {
//                Logger.getLogger(FileReceiver.class.getName()).log(Level.SEVERE, null, ex);
//            }
        }
    }
}
