/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ds.multicast;

import java.net.InetAddress;

/**
 *
 * @author yuzo
 */
public class User {

    public User(InetAddress addr, String username, int port) {
        this.addr = addr;
        this.username = username;
        this.port = port;
    }
    
    InetAddress addr;
    String username;
    int port;

    public InetAddress getAddr() {
        return addr;
    }

    public void setAddr(InetAddress addr) {
        this.addr = addr;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
