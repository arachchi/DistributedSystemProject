package lk.ac.mrt.cse;

/**
 * @author nuran
 * @version 1.0.
 * @since 1/8/16
 */
public class Connection {
    private String ip;
    private int port;
    private int noOfConnections;

    public Connection(String ip, String port){
        this.ip = ip;
        this.port = Integer.parseInt(port);
        this.noOfConnections = 0;
    }

    public Connection(String ip, String port , String noOfConnections){
        this.ip = ip;
        this.port = Integer.parseInt(port);
        this.noOfConnections = Integer.parseInt(noOfConnections);
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "Connection{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}
