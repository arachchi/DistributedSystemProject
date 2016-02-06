package lk.ac.mrt.cse.system.imp;

/*import com.sun.org.apache.xpath.internal.SourceTree;

import com.sun.java.accessibility.util.TopLevelWindowMulticaster;

import java.io.PrintStream;*/

import lk.ac.mrt.cse.system.Client;
import lk.ac.mrt.cse.system.Server;
import lk.ac.mrt.cse.system.model.Connection;
import lk.ac.mrt.cse.util.Utility;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

/**
 * @author nuran
 * @version 1.0.
 * @since 1/8/16
 */
public class ServerImpl extends Observable implements Runnable,Server {
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

    public ServerImpl(ArrayList<String> fileList){
        this.fileList = fileList;
        connections = new ArrayList<Connection>();
        neighbourFileList = new Hashtable<String, ArrayList<Connection>>();
        consoleMsg="";
    }


    public void run(){
        try {
            System.out.println("port is "+ port);
            //client = new ClientImpl(fileList,port,BS_IP,BS_Port,userName);
            DatagramSocket serverSocket = new DatagramSocket(Integer.parseInt(port));
            byte[] receiveData = new byte[size];
            byte[] sendData;

            while (true) {
                System.out.println("Server is waiting:");
                consoleMsg = "Server is waiting:";
                setChanged();
                notifyObservers();


                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String query = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("RECEIVED: " + query);
                requestProcess(query); //processing the request query

                //sending the reply to the client
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();

                String capitalizedSentence = query.toUpperCase();
                sendData = capitalizedSentence.getBytes();//add a reply message

                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                serverSocket.send(sendPacket);

                Thread.sleep(1000);

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void requestProcess(String query){

        consoleMsg = "In requestProcess";
        setChanged();
        notifyObservers();

        String[] message = query.split(" ");

        int length = Integer.parseInt(message[0]);

        if(query.length() == length){
            if(message[1].equals("JOIN")){
                //Connection will be established; Ip and port will be saved
                Connection connection = new Connection(message[2],message[3]);//ip , port

                try {
                    connections.add(connection);
                    //Send response to node
                    String packet = "0013 JOINOK 0";
                    Utility.sendRequest(packet, message[2], message[3]);
                }
                catch(Exception ex){
                    String packet = "0016 JOINOK 9999";
                    Utility.sendRequest(packet, message[2], message[3]);
                }

                //Get the file list of connection for Updating neighbour file list
                getFileList(connection);
            }
            else if(message[1].equals("SEARCH")){
                search(message);
            }
            else if(message[1].equals("GETFILES")){
                sendFileList(message);
            }
            else if(message[1].equals("FILES")){
                updateNeighbourFileList(message);
            }

            consoleMsg = "RECEIVED: " + query;
            setChanged();
            notifyObservers();
        }else{
            //Send response of failure to node
            String packet = "0010 ERROR";
            Utility.sendRequest(packet, message[2], message[3]);
        }
    }

    private void sendFileList(String[] message) { //response to GETFILES : FILES <file1> <file2> ....
        String RequesterIPAddress = message[2];
        String port = message[3];

        //Get IP of localhost
        InetAddress IPAddress = null;
        try {
            IPAddress = Utility.getMyIp();
            System.out.println("I am serverImpl 140");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String packet = " FILES " + Utility.getHostAddress(IPAddress) + " " +port+" ";
        for(int i=0;i<fileList.size();++i){
            if(i!=fileList.size()-1){
                packet=packet.concat(fileList.get(i)+",");
            }else{
                packet=packet.concat(fileList.get(i));
            }
        }

        String userCommand = Utility.getUniversalCommand(packet);
        System.out.println("Trying to send a file list "+userCommand);
        Utility.sendRequest(userCommand, RequesterIPAddress, port);
        System.out.println("successfully sent a file list " + userCommand);
    }

    private void getFileList(Connection connection){//GETFILES Query is GETFILES requester's_ip requester's_port
        //Request connection file list
        String packet = " GETFILES";
        //Get IP of localhost
        InetAddress IPAddress = null;
        try {
            IPAddress = Utility.getMyIp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        packet=packet.concat(" "+ Utility.getHostAddress(IPAddress)+" "+port);
        String userCommand = Utility.getUniversalCommand(packet);

        Utility.sendRequest(userCommand, connection.getIp(), connection.getPort());
    }

    private void updateNeighbourFileList(String[] message){
        String SenderIPAddress = message[1];
        String port = message[2];
        Connection connection=null;
        for(Connection con: connections){
            if(con.getIp().equals(SenderIPAddress) && con.getPort().equals(port)){
                connection=con;
                break;
            }
        }
        ArrayList<Connection> existingConnections;
        for(int i=3;i<message.length;++i){
            if(neighbourFileList.containsKey(message[i]) && connection!=null){
                existingConnections = neighbourFileList.get(message[i]);
            }else{
                existingConnections=new ArrayList<Connection>();
            }
            existingConnections.add(connection);
            neighbourFileList.put(message[i],existingConnections);
        }
    }

    public void search(String[] message){//Search Query is SEARCH filename no_of_hops searcher's_ip searcher's_port//search query runs here
        String keyword = message[1];
        int hops = Integer.parseInt(message[2])-1;
        String SearcherIPAddress = message[3];
        String port = message[4];

        String packet = "";
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

        System.out.println("search ip " + SearcherIPAddress );
        System.out.println("ip address to str " + IPAddress.toString());

        if(SearcherIPAddress.equals(IPAddress.toString())){
            fromLocalClient=true;
        }
        if(!fromLocalClient){
            for (String file : fileList) {
                //If the keyword is contained in the file name as a word or set of words
                if (file.matches(".*\\b"+keyword+"\\b.*")) {
                    hasFile = true;
                    searchResults += " " + file ;
                    no_files++;
                }
            }
        }
        if(!hasFile){
            if(connections.isEmpty()){
                System.out.println("I don't have the file and no more connections. Aborting search.");
                return;
            }
            files= containsKeyWord(neighbourFileList,keyword);
        }

        if (hasFile) {//this node has the keyword

            packet = " SEROK " + no_files + " " + IPAddress.getHostAddress() + " " + port + " " + hops + searchResults;

            String userCommand = Utility.getUniversalCommand(packet);

            Utility.sendRequest(userCommand, SearcherIPAddress, port);
        }
        else if (!files.isEmpty()) {//neighbour nodes have the keyword

            for(String file: files){
                ArrayList<Connection> connections = neighbourFileList.get(file);
                if(!connections.isEmpty()){
                    for(Connection connection:connections ){
                        //Only sends search query to one mapping neighbour if there are many
                        packet = " SER " + keyword + " " + hops + " " + SearcherIPAddress + " " +  port;
                        String userCommand = Utility.getUniversalCommand(packet);
                        Utility.sendRequest(userCommand, connection.getIp(), connection.getPort());
                        break;
                    }

                }

            }
        } else { //otherwise
            if (hops > 1) {
                //number of hops should be checked at the server side by reading search message request
                //and only forward to client if not expired
                //forward the message if not expired

                    Collections.sort(connections,new CustomComparator());

                    for (Connection connection : connections) {
                        String IP = connection.getIp();
                        String connectionPort = connection.getPort();
                        packet = " SER " + keyword + " " + hops + " " + SearcherIPAddress + " " +  port;

                        String userCommand = Utility.getUniversalCommand(packet);
                        Utility.sendRequest(userCommand, IP, connectionPort);
                    }


            }
        }

    }

    public String search(String message){
        String packet = client.search(message);
        if(packet.equals("The searched keyword is present in my list of files."))
            return "The searched keyword is present in my list of files.";
        else{
            String[] mes = packet.substring(9).split(" ");
//            for(String a: packet)
//                System.out.println(a);
            search(mes);
        }
        return "Search request is forwarded to the network";
    }

    @Override
    public void addClientObserver(Observer observer) {
        getClient().addObserver(observer);
    }

    @Override
    public boolean unRegisterToServer() {
        return getClient().unRegisterToServer();
    }

    @Override
    public void joinNetwork() {
        getClient().init();
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
}

class CustomComparator implements Comparator<Connection> {
    @Override
    public int compare(Connection o1, Connection o2) {
        return Integer.valueOf(o1.getNoOfConnections()).compareTo(o2.getNoOfConnections());
    }
}
