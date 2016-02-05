package lk.ac.mrt.cse;

/*import com.sun.org.apache.xpath.internal.SourceTree;

import com.sun.java.accessibility.util.TopLevelWindowMulticaster;

import java.io.PrintStream;*/
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

/**
 * @author nuran
 * @version 1.0.
 * @since 1/8/16
 */
public class Server extends Observable implements Runnable {
    ArrayList<String> fileList;
    Hashtable<String, ArrayList<Connection>> neighbourFileList;
    String consoleMsg;
    Node node;

    int port=9878;
    final static int size=1024;

    public Server(String port,ArrayList<String> fileList){
        this.fileList = fileList;
        this.port=Integer.parseInt(port);
        neighbourFileList = new Hashtable<String, ArrayList<Connection>>();
        consoleMsg="";

    }


    public void run(){
        try {
            DatagramSocket serverSocket = new DatagramSocket(port);
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
                    Node.connections.add(connection);
                    //Send response to node
                    String packet = "0013 JOINOK 0";
                    Node.sendRequest(packet, message[2], message[3]);
                }
                catch(Exception ex){
                    String packet = "0016 JOINOK 9999";
                    Node.sendRequest(packet, message[2], message[3]);
                }
            }
            else if(message[1].equals("SER")){
                search(message);
            }
            consoleMsg = "RECEIVED: " + query;
            setChanged();
            notifyObservers();
        }else{
            //Send response of failure to node
            String packet = "0010 ERROR";
            Node.sendRequest(packet, message[2], message[3]);
        }
    }

    private void sendFileList(String[] message) { //response to GETFILES : FILES <file1> <file2> ....
        String RequesterIPAddress = message[2];
        String port = message[3];

        //Get IP of localhost
        InetAddress IPAddress = null;
        try {
            IPAddress = Node.getIp();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String packet = " FILES " +Node.getHostAddress() + " " +port+" ";
        for(int i=0;i<fileList.size();++i){
            if(i!=fileList.size()-1){
                packet=packet.concat(fileList.get(i)+",");
            }else{
                packet=packet.concat(fileList.get(i));
            }
        }

        String userCommand = Node.getUniversalCommand(packet);
        System.out.println("Trying to send a file list "+userCommand);
        Node.sendRequest(userCommand,RequesterIPAddress,port);
        System.out.println("successfully sent a file list "+userCommand);
    }

    private void getFileList(Connection connection){//GETFILES Query is GETFILES requester's_ip requester's_port
        //Request connection file list
        String packet = " GETFILES";
        //Get IP of localhost
        InetAddress IPAddress = null;
        try {
            IPAddress = Node.getIp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        packet=packet.concat(" "+Node.getHostAddress()+" "+port);
        String userCommand = Node.getUniversalCommand(packet);

        Node.sendRequest(userCommand,connection.getIp(),connection.getPort());
    }

    private void updateNeighbourFileList(String[] message){
        String SenderIPAddress = message[2];
        System.out.println("Sender ip is"+message[2]);
        String port = message[3];
        Connection connection=null;
        for(Connection con: Node.connections){
            if(con.getIp().equals(SenderIPAddress) && con.getPort().equals(port)){
                connection=con;
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

    public void search(String[] message){//Search Query is len SEARCH filename no_of_hops searcher's_ip searcher's_port
        //search query runs here

        String keyword = message[2];
        System.out.println("searching for key "+message[2]);

        int hops = Integer.parseInt(message[3])-1;
        String SearcherIPAddress = message[4];
        String port = message[5];

        String packet = "";
        boolean hasFile = false;//Flags whether this node contains the file
        boolean fromLocalClient=false; //Flags if the request is from the local client

        String searchResults = "";//search results
        int no_files = 0;//no of search results

        //Get IP of localhost
        InetAddress IPAddress = null;
        try {
            IPAddress = Node.getIp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(SearcherIPAddress.equals(IPAddress.getHostName())){
            fromLocalClient=true;
        }
        if(fromLocalClient){
            if (hops > 1) {
                //number of hops should be checked at the server side by reading search message request
                //and only forward to client if not expired
                //forward the message if not expired

                //Collections.sort(connections,new CustomComparator());
                System.out.println("trying to send request to connections");

                for (Connection connection : Node.connections) {
                    String IP = connection.getIp();
                    String connectionPort = connection.getPort();

                    System.out.println("connections's ip "+IP);
                    System.out.println("connections's port "+port);
                    packet = " SER " + keyword + " " + hops + " " + SearcherIPAddress + " " +  port;

                    String userCommand = Node.getUniversalCommand(packet);
                    System.out.println("Forwarding ser request to connections");
                    Node.sendRequest(userCommand,IP,connectionPort);
                }
            }else{
                System.out.println("The maximum hop count is reached. Aborting search.");
            }
        }else{
            if(keyword.contains("_")){//Convert keyword with multiple words
                keyword=keyword.replaceAll("_"," ");
            }
            for (String file : fileList) {
                //If the keyword is contained in the file name as a word or set of words
                if (file.matches(".*\\b"+keyword+"\\b.*")) {
                    hasFile = true;
                    searchResults += " " + file ;
                    no_files++;
                }
            }
            if (hasFile) {//this node has the keyword

                packet = " SEROK " + no_files + " " + IPAddress.getHostAddress() + " " + port + " " + hops + searchResults;
                String userCommand = Node.getUniversalCommand(packet);
                Node.sendRequest(userCommand,SearcherIPAddress,port);
            }else{
                if(Node.connections.isEmpty()){
                    System.out.println("I don't have the file and no more connections. Aborting search.");
                    return;
                }else{
                    if (hops > 1) {
                        //number of hops should be checked at the server side by reading search message request
                        //and only forward to client if not expired
                        //forward the message if not expired
                        if(keyword.contains(" ")){//Allows searching for key words with multiple words
                            keyword=keyword.replaceAll(" ","_");
                        }
                        //Collections.sort(connections,new CustomComparator());
                        for (Connection connection : Node.connections) {
                            String IP = connection.getIp();
                            String connectionPort = connection.getPort();
                            packet = " SER " + keyword + " " + hops + " " + SearcherIPAddress + " " +  port;

                            String userCommand = Node.getUniversalCommand(packet);
                            Node.sendRequest(userCommand,IP,connectionPort);
                        }
                    }else{
                        System.out.println("The maximum hop count is reached. Aborting search.");
                    }
                }
            }
        }
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
}

class CustomComparator implements Comparator<Connection> {
    @Override
    public int compare(Connection o1, Connection o2) {
        return Integer.valueOf(o1.getNoOfConnections()).compareTo(o2.getNoOfConnections());
    }
}
