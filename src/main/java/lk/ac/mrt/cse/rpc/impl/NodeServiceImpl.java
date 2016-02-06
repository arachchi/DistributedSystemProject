package lk.ac.mrt.cse.rpc.impl;

import lk.ac.mrt.cse.rpc.NodeService;
import lk.ac.mrt.cse.rpc.RPCServer;
import lk.ac.mrt.cse.system.Client;
import lk.ac.mrt.cse.system.Server;
import lk.ac.mrt.cse.system.model.Connection;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Observer;

/**
 * Created by sabra on 2/6/16.
 */
public class NodeServiceImpl implements NodeService.Iface,Server {
    ArrayList<String> fileList;
    ArrayList<Connection> connections;// Routing Table
    Hashtable<String, ArrayList<Connection>> neighbourFileList;
    String consoleMsg;
    Client client;

    private static int BS_Port;
    private static String BS_IP;
    private static String port;
    private static String nodeIp;
    private static String userName;
    final static int size=1024;

    public NodeServiceImpl(ArrayList<String> fileList){
        this.fileList = fileList;
        connections = new ArrayList<Connection>();
        neighbourFileList = new Hashtable<String, ArrayList<Connection>>();
        consoleMsg="";
    }

    @Override
    public String join(String myIp, int myPort) throws TException {
        //Connection will be established; Ip and port will be saved
        Connection connection = new Connection(myIp,""+myPort);//ip , port
        String packet = null;
        try {
            connections.add(connection);
            //Send response to node
            packet = "0013 JOINOK 0";
        }
        catch(Exception ex){
            packet = "0016 JOINOK 9999";
        }

        //Get the file list of connection for Updating neighbour file list
        getFileList(connection);
        return packet;
    }

    @Override
    public List<String> getFiles() throws TException {
        if(fileList == null)
            return new ArrayList<>();
        else
            return fileList;
    }

    private void getFileList(Connection connection){//GETFILES Query is GETFILES requester's_ip requester's_port
        //Request connection file list
        ArrayList fileList;
        TTransport transport;
        try {
            transport = new TSocket(connection.getIp(), Integer.parseInt(connection.getPort()));

            TProtocol protocol = new TBinaryProtocol(transport);

            NodeService.Client client = new NodeService.Client(protocol);
            transport.open();

            fileList = (ArrayList<String>)client.getFiles();

            ArrayList<Connection> existingConnections;
            for(int i=3;i<fileList.size();++i){
                if(neighbourFileList.containsKey(fileList.get(i)) && connection!=null){
                    existingConnections = neighbourFileList.get(fileList.get(i));
                }else{
                    existingConnections=new ArrayList<Connection>();
                }
                existingConnections.add(connection);
                System.out.println(fileList.get(i).toString());
                neighbourFileList.put(fileList.get(i).toString(),existingConnections);
            }

            transport.close();
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void requestProcess(String query) {
        System.out.println("NO RP");
    }

    @Override
    public void search(String[] message) {

    }

    @Override
    public String search(String message) {
        return null;
    }

    @Override
    public void addObserver(Observer observer) {

    }

    @Override
    public void addClientObserver(Observer observer) {

    }

    @Override
    public boolean unRegisterToServer() {
        return false;
    }

    @Override
    public void joinNetwork() {
        client.init();
    }

    public String getConsoleMsg() {
        return consoleMsg;
    }

    public String getClientConsoleMsg(){
        return getClient().getConsoleMsg();
    }

    @Override
    public boolean registerToServer() {
        return getClient().registerToServer();
    }

    public void setConsoleMsg(String consoleMsg) {
        this.consoleMsg = consoleMsg;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public int getBS_Port() {
        return BS_Port;
    }

    public void setBS_Port(int BS_Port) {
        this.BS_Port = BS_Port;
    }

    public String getBsIp() {
        return BS_IP;
    }

    public void setBsIp(String bsIp) {
        BS_IP = bsIp;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getNodeIp() {
        return nodeIp;
    }

    public void setNodeIp(String nodeIp) {
        this.nodeIp = nodeIp;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getSize() {
        return size;
    }

    public ArrayList<String> getFileList() {
        return fileList;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        RPCServer rpcServer = new RPCServer();
        rpcServer.setPort(port);
        rpcServer.start(fileList);
    }
}

