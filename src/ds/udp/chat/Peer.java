
package ds.udp.chat;

import java.net.InetAddress;

/**
 *
 * @author yuzo
 */
public class Peer {
    int id;
    String nickname;
    InetAddress ip;


    public Peer(int id, String nickname, InetAddress ip) {
        this.id = id;
        this.nickname = nickname;
        this.ip = ip;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
