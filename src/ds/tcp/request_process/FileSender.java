/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ds.tcp.request_process;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author yuzo TODO: do with 4096 max buffer end thread after sending file
 */
public class FileSender {

    private File file;
    private DataOutputStream output;
    static int BUFFER = 4096;

    public FileSender(File file, DataOutputStream output) {
        this.file = file;
        this.output = output;
    }

    public void sendFile() {
        FileInputStream fis = null;
        try {
            byte[] mybytearray = new byte[BUFFER];
            fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);
            try {
                output.writeLong(file.length());
                int read;
                while ((read = dis.read(mybytearray)) != -1) {
                    output.write(mybytearray, 0, read);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileSender.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(FileSender.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
