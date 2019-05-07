/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ds.tcp.file_server;

/**
 *
 * @author yuzo
 */
public class ByteUtils {
    
    public static byte[] combine(byte[] a, byte[] b) {
        int length = a.length + b.length;
        byte[] result = new byte[length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
