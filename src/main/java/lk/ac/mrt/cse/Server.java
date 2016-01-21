package lk.ac.mrt.cse;

import com.sun.org.apache.xpath.internal.SourceTree;

import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

/**
 * @author nuran
 * @version 1.0.
 * @since 1/8/16
 */
public class Server extends Thread {
    ArrayList<String> fileList;
    ArrayList<Connection> connections;// Routing Table
    Hashtable<String, ArrayList<Connection>> neighbourFileList;

    PrintStream printStream = new PrintStream(new ServerLogWindow());

    int port=9878;
    final static int size=1024;

    public Server(String port,ArrayList<String> fileList){
        this.fileList = fileList;
        this.port=Integer.parseInt(port);
        connections = new ArrayList<Connection>();
        neighbourFileList = new Hashtable<String, ArrayList<Connection>>();

        //System.setOut(printStream);
    }


    public void run(){
        try {
            DatagramSocket serverSocket = new DatagramSocket(port);
            byte[] receiveData = new byte[size];
            byte[] sendData;

            while (true) {
                System.out.println("Server is waiting:");

                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String query = new String(receivePacket.getData(), 0, receivePacket.getLength());

                requestProcess(query); //processing the request query

                //sending the reply to the client
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();

                String capitalizedSentence = query.toUpperCase();
                sendData = capitalizedSentence.getBytes();//add a reply message

                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                serverSocket.send(sendPacket);

                sleep(1000);

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void requestProcess(String query){

        System.out.println("In requestProcess");

        String[] message = query.split(" ");


        if(message[1].equals("JOIN")){
            //Connection will be established; Ip and port will be saved
            Connection connection = new Connection(message[2],message[3]);//ip , port

            try {
                connections.add(connection);

                //Send response to node
                String packet = "0013 JOINOK 0";
                Node.sendRequest(packet, message[2], message[3]);
            }
            catch(Exception ex){
                String packet = "0016 JOINOK 9999";
                Node.sendRequest(packet, message[2], message[3]);
            }

            //Get the file list of connection for Updating neighbour file list
            //getFileList(connection);
        }
        else if(message[0].equals("SEARCH")){
            search(message);
        }
        else if(message[0].equals("GETFILES")){
            sendFileList(message);
        }
        else if(message[0].equals("FILES")){
            updateNighbourFileList(message);
        }
        System.out.println("RECEIVED: " + query);
    }

    private void sendFileList(String[] message) { //response to GETFILES : FILES <file1> <file2> ....
        String RequesterIPAddress = message[1];
        String port = message[2];

        //Get IP of localhost
        InetAddress IPAddress = null;
        try {
            IPAddress = Node.getIp();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String packet = "FILES " +IPAddress + " " +port ;
        for(String file : fileList){
            packet=packet.concat(" "+ file);
        }
        String length= String.format("%04d", packet.length() + 4); //Length is always represented as 4 digits
        packet = length.concat(" "+ packet);
        Node.sendRequest(packet,RequesterIPAddress,port);
    }

    private void getFileList(Connection connection){//GETFILES Query is GETFILES requester's_ip requester's_port
        //Request connection file list
        String packet = "GETFILES";
        //Get IP of localhost
        InetAddress IPAddress = null;
        try {
            IPAddress = Node.getIp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        packet=packet.concat(" "+IPAddress+" "+port);
        Node.sendRequest(packet,connection.getIp(),connection.getPort());
    }

    private void updateNighbourFileList(String[] message){
        String SenderIPAddress = message[1];
        String port = message[2];
        Connection connection=null;
        for(Connection con: connections){
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

    public void search(String[] message){//Search Query is SEARCH filename no_of_hops searcher's_ip searcher's_port
        //search query runs here

        String keyword = message[1];
        int hops = Integer.parseInt(message[2])-1;
        String SearcherIPAddress = message[3];
        String port = message[4];

        hops--;//should be handled in server side
        String packet = "";
        boolean hasFile = false;//Flags whether this node contains the file
        boolean fromLocalClient=false; //Flags if the request is from the local client
        ArrayList<String> files=new ArrayList<String>();

        String searchResults = "";//search results
        int no_files = 0;//no of search results

        //Get IP of localhost
        InetAddress IPAddress = null;
        try {
            IPAddress = Node.getIp();
        } catch (Exception e) {
            e.printStackTrace();
        }

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
            files= containsKeyWord(neighbourFileList,keyword);
        }

        if (hasFile) {//this node has the keyword

            packet = "SEROK " + no_files + " " + IPAddress.getHostAddress() + " " + port + " " + hops + searchResults;
            String length= String.format("%04d", packet.length() + 4); //Length is always represented as 4 digits
            packet = length.concat(" "+ packet);
            Node.sendRequest(packet,SearcherIPAddress,port);
        }
        else if (!files.isEmpty()) {//neighbour nodes have the keyword

            for(String file: files){
                ArrayList<Connection> connections = neighbourFileList.get(file);
                if(!connections.isEmpty()){
                    for(Connection connection:connections ){
                        //Only sends one search result
                        no_files++;
                        searchResults = " "+ file;
                        packet = "SEROK " + no_files + " " + connection.getIp() + " " + connection.getPort() + " " + hops + searchResults;
                        String length= String.format("%04d", packet.length() + 4); //Length is always represented as 4 digits
                        packet = length.concat(" "+ packet);
                        Node.sendRequest(packet,SearcherIPAddress,port);
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
                    packet = "SER " + keyword + " " + hops + " " + SearcherIPAddress + " " +  port;
                    Node.sendRequest(packet,IP,connectionPort);
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
