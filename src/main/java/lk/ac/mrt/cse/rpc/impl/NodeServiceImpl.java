package lk.ac.mrt.cse.rpc.impl;

import lk.ac.mrt.cse.rpc.NodeService;
import lk.ac.mrt.cse.rpc.RPCServer;
import lk.ac.mrt.cse.system.Client;
import lk.ac.mrt.cse.system.Server;
import lk.ac.mrt.cse.system.model.Connection;
import lk.ac.mrt.cse.util.ConnectionTable;
import lk.ac.mrt.cse.util.Utility;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.net.InetAddress;
import java.util.*;

/**
 * Created by sabra on 2/6/16.
 */
public class NodeServiceImpl extends  Observable implements NodeService.Iface,Server {
    ArrayList<String> fileList;
    ArrayList<Connection> connections;// Routing Table
    Hashtable<String, ArrayList<Connection>> neighbourFileList;
    String consoleMsg;
    String searchMsg;
    Client client;
    ConnectionTable routingTable;

    private static int BS_Port;
    private static String BS_IP;
    private static String port;
    private static String nodeIp;
    private static String userName;
    final static int size=1024;

    public NodeServiceImpl(ConnectionTable routingTable,ArrayList<String> fileList){
        this.routingTable=routingTable;
        this.fileList = fileList;
        //connections = new ArrayList<Connection>();
        neighbourFileList = new Hashtable<String, ArrayList<Connection>>();
        consoleMsg="";
    }

    @Override
    public String join(String myIp, int myPort) throws TException {
        //Connection will be established; Ip and port will be saved
        Connection connection = new Connection(myIp,""+myPort);//ip , port
        String packet;
        try {
            routingTable.addConnections(connection);
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
    public void search(String keyWord, String requestorIP, String requestorPort, int hops) throws TException {

        hops--;
        if(hops<1) hops=0;

        boolean hasFile = false;//Flags whether this node contains the file
        boolean fromLocalClient=false; //Flags if the request is from the local client
        ArrayList<String> files=new ArrayList<>();

        //Get IP of localhost
        String localIPAddress=Utility.getHostAddress();
        //Check if from local client
        if(requestorIP.equals(localIPAddress) && requestorPort.equals(port)){
            fromLocalClient=true;
        }
        //if not from local client,check in local files
        if(!fromLocalClient){
            //check in local files and send searchOK if present
            hasFile=checkIfKeyInLocalFiles(keyWord, requestorIP, requestorPort, hops);
        }
        //if not in local files
        if(!hasFile){
            ArrayList<Connection> connections=routingTable.getConnections();
            files= containsKeyWord(neighbourFileList,keyWord);
            if (!files.isEmpty()){
                sendToNeighbours(files,keyWord,requestorIP,requestorPort,hops);
            }else{
                sendToAllConnections(connections,keyWord,requestorIP,requestorPort,hops);
            }
        }
    }

    private boolean checkIfKeyInLocalFiles(String keyword,String searcherIPAddress,String searcherPort,int hops){
        boolean hasFile=false;
        String searchResults = "";//search results
        int no_files = 0;//no of search results
        for (String file : fileList) {
            //If the keyword is contained in the file name as a word or set of words
            if (file.matches(".*\\b"+keyword+"\\b.*")) {
                hasFile = true;
                searchResults += " " + file ;
                no_files++;
            }
        }
        if(hasFile){

            String localIPAddress=Utility.getHostAddress();
            String result = " SEROK " + no_files + " " + localIPAddress + " " + port + " " + hops + searchResults;
            String userCommand = Utility.getUniversalCommand(result);

            TTransport transport;
            try {
                //Open RPC Connection
                transport = new TSocket(searcherIPAddress, Integer.parseInt(searcherPort));
                TProtocol protocol = new TBinaryProtocol(transport);
                NodeService.Client client = new NodeService.Client(protocol);
                transport.open();

                //Send the success result (SEROK) to the requestor
                client.handleResult(userCommand);

                transport.close();
            } catch (TTransportException e) {
                e.printStackTrace();
            } catch (TException e) {
                e.printStackTrace();
            }

        }
        return hasFile;
    }

    private void sendToNeighbours(ArrayList<String> files,String keyword,String searcherIPAddress,String searcherPort,int hops){
        for(String file: files){
            ArrayList<Connection> connectionsHavingFile = neighbourFileList.get(file);
            if(!connectionsHavingFile.isEmpty()){
                Connection connection = connectionsHavingFile.get(0);
                String IP = connection.getIp();
                String connectionPort = connection.getPort();
                hops=hops-1;
                if(connectionPort.equals(searcherPort) && IP.equals(searcherIPAddress)){
                    System.out.println("Ignoring because it's the searcher");
                }else{
                    //Only sends search query to one mapping neighbour if there are many
                    TTransport transport;
                    try {
                        //Open RPC Connection
                        transport = new TSocket(IP, Integer.parseInt(connectionPort));
                        TProtocol protocol = new TBinaryProtocol(transport);
                        NodeService.Client client = new NodeService.Client(protocol);
                        transport.open();

                        client.search(keyword,searcherIPAddress,searcherPort,hops);

                        transport.close();
                    } catch (TTransportException e) {
                        e.printStackTrace();
                    } catch (TException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void sendToAllConnections(ArrayList<Connection> connections,String keyword,String searcherIPAddress,String searcherPort,int hops){
        if (hops > 1) {
            if(connections.isEmpty()){ //has no connections
                System.out.println("I don't have the file and no more connections. Aborting search.");
                setConsoleMsg("I don't have the file and no more connections. Aborting search.");
            }else{
                Collections.sort(connections,new CustomComparator());
                for (Connection connection : connections) {
                    String IP = connection.getIp();
                    String connectionPort = connection.getPort();
                    if(connectionPort.equals(searcherPort) && IP.equals(searcherIPAddress)){
                        System.out.println("Ignoring because it's the searcher");
                    }else{
                        TTransport transport;
                        try {
                            //Open RPC Connection
                            transport = new TSocket(IP, Integer.parseInt(connectionPort));
                            TProtocol protocol = new TBinaryProtocol(transport);
                            NodeService.Client client = new NodeService.Client(protocol);
                            transport.open();

                            client.search(keyword,searcherIPAddress,searcherPort,hops);

                            transport.close();
                        } catch (TTransportException e) {
                            e.printStackTrace();
                        } catch (TException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @Override
    public String handleResult(String result) throws TException {

        String[] message = result.split(" ");

        setConsoleMsg("Successfully Connected to you: ip- " + message[3] + " port- " + message[4]);

        //Connect to IP and Port to get the file - No Implementation needed
        return "Successfully Connected to you: ip- " + message[3] + " port- " + message[4];
    }

    @Override
    public void handleFileList(List<String> fileList, String joinedNodeIp, String joinedNodePort) throws TException {
        Connection connection = new Connection(joinedNodeIp, joinedNodePort);
        //Update neighbour file list with joined nodes file list
        updateNeighbourFileList((ArrayList<String>)fileList,connection);
    }

    @Override
    public String leave(String leaverIp, int leaverPort) throws TException {
        String result;
        //Connection will be established; Ip and port will be saved
        Connection connection = new Connection(leaverIp,Integer.toString(leaverPort));//ip , port

        try {
            routingTable.removeConnection(connection);
            result = "0014 LEAVEOK 0";
        }
        catch(Exception ex){
            result = "0017 LEAVEOK 9999";
        }

        return result;
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

    private void getFileList(Connection connection){//Of joining node
        ArrayList otherNode_FileList;
        TTransport transport;
        try {
            //Open RPC Connection
            transport = new TSocket(connection.getIp(), Integer.parseInt(connection.getPort()));
            TProtocol protocol = new TBinaryProtocol(transport);
            NodeService.Client client = new NodeService.Client(protocol);
            transport.open();

            //Get files of connecting node
            otherNode_FileList = (ArrayList<String>) client.getFiles();

            //Update my neighbour list with other nodes file list
            updateNeighbourFileList(otherNode_FileList, connection);

            //Send my file list to connecting node
            client.handleFileList(fileList, nodeIp, port);

            transport.close();
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
    }

    private void updateNeighbourFileList(ArrayList<String> fileList, Connection connection){
        ArrayList<Connection> existingConnections;
        for(String fileName: fileList){
            if(neighbourFileList.containsKey(fileName) && connection!=null){
                existingConnections = neighbourFileList.get(fileName);
            }else{
                existingConnections=new ArrayList<>();
            }
            existingConnections.add(connection);
            neighbourFileList.put(fileName,existingConnections);
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
            //Extract params from search message returned from client
            //When the client doesn't have the requested file
            String[] mes = packet.substring(9).split(" ");
            String keyword = mes[0];
            String requestorIp = mes[2];
            String requestorPort = mes[3];
            int hops = Integer.parseInt(mes[1]);

            try {
                search(keyword, requestorIp, requestorPort, hops);
            } catch (TException e) {
                e.printStackTrace();
            }
        }

        return "Search is forwarded to network";
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
    public String getSearchMsg() {
        return searchMsg;
    }

    @Override
    public void setSearchMsg(String serachMsg) {
        this.searchMsg = searchMsg;
    }

    @Override
    public String getClientStatus() {
        return getClient().getStatus();
    }

    @Override
    public boolean registerToServer() {
        return getClient().registerToServer();
    }

    public void setConsoleMsg(String consoleMsg) {
        this.consoleMsg = consoleMsg;
        setChanged();
        notifyObservers();
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

        String nodes = "Joining to : \n";
        for(Connection con: connections){
            System.out.println(con);
            nodes += con+"";
        }
        setConsoleMsg(nodes);
    }

    public ConnectionTable getRoutingTable() {
        return this.routingTable;
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
        RPCServer rpcServer = new RPCServer(this.routingTable);
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

