package lk.ac.mrt.cse;

/**
 * @author nuran
 * @version 1.0.
 * @since 1/8/16
 */
public class Connection {
    private String ip;
    private String userName;
    private String port;

    private int noOfConnections;

    public Connection(String ip, String port){
        this.ip = ip;
        this.port = port;
        this.noOfConnections = 0;
    }

    public Connection(String ip, String port, String username){
        this.ip = ip;
        this.port = port;
        this.userName = username;
        this.noOfConnections = 0;
    }

    public Connection(String ip, String port , String username, String noOfConnections){
        this.ip = ip;
        this.port = port;
        this.userName = username;
        this.noOfConnections = Integer.parseInt(noOfConnections);
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public int getNoOfConnections() {
        return noOfConnections;
    }

    public void setNoOfConnections(int noOfConnections) {
        this.noOfConnections = noOfConnections;
    }

    @Override
    public String toString() {
        return "Connection{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}
