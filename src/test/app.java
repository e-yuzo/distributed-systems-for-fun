/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import ds.tcp.file_server.ByteUtils;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author yuzo
 */
public class app {

    public static void main(String[] args) {
        //String s = "qwdqwd";
        //System.out.println(s.substring(0, s.length()));
        //System.out.println(s.getBytes().length);
//        int lawl = 3;
//        byte[] data = new byte[2];
//        data[0] = (byte) (lawl & 0xFF);
//        data[1] = (byte) ((lawl >> 8) & 0xFF);
//        ByteBuffer reg = ByteBuffer.allocate(2);
//        reg.putInt(lawl);
//        int val = ((reg.get(1) & 0xff) << 8) | (reg.get(0) & 0xff);
//        System.out.println(val);
//        String lol = "lolwhatever";
//        byte[] L = lol.getBytes(StandardCharsets.UTF_8);
//        System.out.println(new String(L, StandardCharsets.UTF_8));
//        File folder = new File("/home/yuzo/Desktop/");
//        int numberOfFiles = folder.listFiles().length;
//        System.out.println(numberOfFiles);
        byte[] before = new byte[3];
        before[0] = (byte) 1;
        before[1] = (byte) 2;
        before[2] = (byte) -66;
        System.out.println("beforeLength: " + before.length);
        String after = new String(before, StandardCharsets.UTF_8);
        byte[] afterBytes = after.getBytes(StandardCharsets.UTF_8);
        System.out.println(afterBytes.length);
    }
}
