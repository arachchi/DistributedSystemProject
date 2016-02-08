package lk.ac.mrt.cse.rpc.impl;

import lk.ac.mrt.cse.rpc.NodeService;
import lk.ac.mrt.cse.rpc.RPCServer;
import lk.ac.mrt.cse.system.Client;
import lk.ac.mrt.cse.system.Server;
import lk.ac.mrt.cse.system.model.Connection;
import lk.ac.mrt.cse.util.Utility;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.net.InetAddress;
import java.util.*;
import java.util.stream.Collectors;

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

    @Override
    public String search(String keyWord, String requestorIP, String requestorPort, int hops) throws TException {

        //connections.addAll(client.getConnectedNodes().stream().collect(Collectors.toList()));

        String response = null;
        if(hops<1) hops = 0;

        boolean hasFile = false;//Flags whether this node contains the file
        boolean fromLocalClient=false; //Flags if the request is from the local client
        ArrayList<String> files=new ArrayList<String>();

        String searchResults = "";//search results
        int no_files = 0;//no of search results

        //Get IP of localhost
        InetAddress IPAddress = null;
        try {
            IPAddress = Utility.getMyIp();
            System.out.println("I am serverImpl 216");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(requestorIP.equals(IPAddress.toString())){
            fromLocalClient=true;
        }
        if(!fromLocalClient){
            for (String file : fileList) {
                //If the keyword is contained in the file name as a word or set of words
                if (file.matches(".*\\b"+keyWord+"\\b.*")) {
                    hasFile = true;
                    searchResults += " " + file ;
                    no_files++;
                }
            }
        }
        if(!hasFile){
            if(connections.isEmpty()){
                response = "I don't have the file and no more connections. Aborting search.";
            }

            files= containsKeyWord(neighbourFileList,keyWord);
        }

        if (hasFile) {//this node has the keyword
            response = " SEROK " + no_files + " " + IPAddress.getHostAddress() + " " + port + " " + hops + searchResults;
        }
        else if (!files.isEmpty()) {//neighbour nodes have the keyword

            for(String file: files){
                ArrayList<Connection> connections = neighbourFileList.get(file);
                if(!connections.isEmpty()){

                    Connection connection = connections.get(0);
                    //Only sends search query to one mapping neighbour if there are many
                    TTransport transport;
                    try {
                        transport = new TSocket(connection.getIp(), Integer.parseInt(connection.getPort()));

                        TProtocol protocol = new TBinaryProtocol(transport);

                        NodeService.Client client = new NodeService.Client(protocol);
                        transport.open();

                        response = client.search(keyWord,requestorIP,requestorPort,hops);

                        transport.close();
                    } catch (TTransportException e) {
                        e.printStackTrace();
                    } catch (TException e) {
                        e.printStackTrace();
                    }

                }

            }
        } else { //otherwise
            if (hops > 1) {
                //number of hops should be checked at the server side by reading search message request
                //and only forward to client if not expired
                //forward the message if not expired

                Collections.sort(connections, new CustomComparator());

                for (Connection connection : connections) {
                    String IP = connection.getIp();
                    String connectionPort = connection.getPort();

                    TTransport transport;
                    try {
                        transport = new TSocket(IP, Integer.parseInt(connectionPort));

                        TProtocol protocol = new TBinaryProtocol(transport);

                        NodeService.Client client = new NodeService.Client(protocol);
                        transport.open();

                        response = client.search(keyWord,requestorIP,requestorPort,hops);

                        transport.close();
                    } catch (TTransportException e) {
                        e.printStackTrace();
                    } catch (TException e) {
                        e.printStackTrace();
                    }
                }


            }
        }

        return response;
    }

    private ArrayList<String> containsKeyWord(Hashtable<String, ArrayList<Connection>> neighbourFileList,String keyword){
        ArrayList<String> files=new ArrayList<String>();
        Set<String> keys = neighbourFileList.keySet();
        for(String key :keys){
            if(key.matches(".*\\b"+keyword+"\\b.*")){
                files.add(key);
            }
        }
        return  files;
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

            System.out.println(fileList.size());

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

        String packet = client.search(message);
        if(packet.equals("The searched keyword is present in my list of files."))
            return "The searched keyword is present in my list of files.";
        else{
            String[] mes = packet.substring(9).split(" ");
            String keyword = mes[0];
            String requestorIp = mes[2];
            String requestorPort = mes[3];
            int hops = Integer.parseInt(mes[1]);

            try {
                return search(keyword, requestorIp, requestorPort, hops);
            } catch (TException e) {
                e.printStackTrace();
            }
        }

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
        return getClient().unRegisterToServer();
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

    @Override
    public void setConnectedNodesList(ArrayList<Connection> firstTwoNodes) {
        connections = firstTwoNodes;

        for(Connection con: connections){
            System.out.println(con);
        }
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

    class CustomComparator implements Comparator<Connection> {
        @Override
        public int compare(Connection o1, Connection o2) {
            return Integer.valueOf(o1.getNoOfConnections()).compareTo(o2.getNoOfConnections());
        }
    }
}

