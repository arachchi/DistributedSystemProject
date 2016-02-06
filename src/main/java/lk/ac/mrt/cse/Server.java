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
    String port="9878";
    final static int size=1024;

    public Server(Node node,String port,ArrayList<String> fileList){
        this.node=node;
        this.fileList = fileList;
        this.port=port;
        neighbourFileList = new Hashtable<String, ArrayList<Connection>>();
        consoleMsg="";

    }


    public void run(){
        try {
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
                    node.addConnections(connection);
                    //Send response to node
                    String packet = "0013 JOINOK 0";
                    Node.sendRequest(packet, message[2], message[3]);
                }
                catch(Exception ex){
                    String packet = "0016 JOINOK 9999";
                    Node.sendRequest(packet, message[2], message[3]);
                }

                //Get the file list of connection for Updating neighbour file list
                getFileList(connection);
            }
            else if(message[1].equals("SER")){
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
            Node.sendRequest(packet, message[2], message[3]);
        }
    }

    private void sendFileList(String[] message) { //response to GETFILES : FILES <file1> <file2> ....
        String RequesterIPAddress = message[2];
        String port = message[3];
        updateNeighbourFileList(message);

        //Get IP of localhost
        InetAddress IPAddress = null;
        try {
            IPAddress = Node.getIp();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String packet = " FILES " +IPAddress.getHostName() + " " +this.port+" ";
        String filename;
        for(int i=0;i<fileList.size();++i){
            filename= fileList.get(i);
            if(filename.contains(" ")){
                filename=filename.replaceAll(" ","_");
            }
            if(i!=fileList.size()-1){
                packet=packet.concat(filename+",");
            }else{
                packet=packet.concat(filename);
            }
        }

        String userCommand = Node.getUniversalCommand(packet);
        System.out.println("Trying to send a file list "+userCommand);
        Node.sendRequest(userCommand,RequesterIPAddress,port);
        System.out.println("successfully sent a file list "+userCommand);
    }

    private void getFileList(Connection connection){//GETFILES Query is GETFILES requester's_ip requester's_port requester's_files
        //Request connection file list
        String packet = " GETFILES";
        //Get IP of localhost
        InetAddress IPAddress = null;
        try {
            IPAddress = Node.getIp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        packet=packet.concat(" "+Node.getHostAddress()+" "+port+" ");
        //Append my list at the end
        String filename;
        for(int i=0;i<fileList.size();++i){
            filename= fileList.get(i);
            if(filename.contains(" ")){
                filename=filename.replaceAll(" ","_");
            }
            if(i!=fileList.size()-1){
                packet=packet.concat(filename+",");
            }else{
                packet=packet.concat(filename);
            }
        }
        String userCommand = Node.getUniversalCommand(packet);

        Node.sendRequest(userCommand,connection.getIp(),connection.getPort());
    }

    private void updateNeighbourFileList(String[] message){
        String SenderIPAddress = message[2];
        String port = message[3];
        String filelist=message[4];
        Connection connection=null;

        for(Connection con: node.getConnections()){
            if(con.getIp().equals(SenderIPAddress) && con.getPort().equals(port)){
                connection=con;
            }
        }
        ArrayList<Connection> existingConnections;
        String[] files=filelist.split(",");

        for(int i=0;i<files.length;++i){
            if(neighbourFileList.containsKey(files[i]) && connection!=null){
                existingConnections = neighbourFileList.get(files[i]);
            }else{
                existingConnections=new ArrayList<Connection>();
            }
            existingConnections.add(connection);
            neighbourFileList.put(files[i],existingConnections);
            System.out.println("Added to neighbor"+files[i]);
        }
    }

    public void search(String[] message){//Search Query is len SER filename no_of_hops searcher's_ip searcher's_port
        //search query runs here
        String keyword = message[2];
        System.out.println("searching for key "+message[2]);

        int hops = Integer.parseInt(message[3]);
        String SearcherIPAddress = message[4];
        String searcherport = message[5];

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

        if(SearcherIPAddress.equals(IPAddress.getHostAddress()) && searcherport.equals(this.port)){
            fromLocalClient=true;
        }if(!fromLocalClient){
            hops=hops-1;
            //Search in my file list
            if (keyword.contains("_")) {//Convert keyword with multiple words
                keyword = keyword.replaceAll("_", " ");
            }
            System.out.println("searching "+keyword);
            for (String file : fileList) {
                //If the keyword is contained in the file name as a word or set of words
                if (file.matches(".*\\b"+keyword+"\\b.*")) {
                    hasFile = true;
                    searchResults += " " + file ;
                    no_files++;
                }
            }
        }
        if(hasFile){
            //send search ok
            packet = " SEROK " + no_files + " " + IPAddress.getHostAddress() + " " + port + " " + hops + searchResults;
            String userCommand = Node.getUniversalCommand(packet);
            Node.sendRequest(userCommand,SearcherIPAddress,searcherport);
        }
        if(fromLocalClient | !hasFile){
            System.out.println("Should be forwarded");
            //Send to neighbours if they have
            //else send to the connections
            if (keyword.contains(" ")) {//Convert keyword with multiple words
                keyword = keyword.replaceAll(" ", "_");
            }
            System.out.println(keyword);
            //check if neighbours have
            files= containsKeyWord(keyword);

            //If neighbour has send req to that node
            if(!files.isEmpty()){
                for(String file: files){
                    ArrayList<Connection> connections = neighbourFileList.get(file);
                    System.out.println("node has "+connections.size());
                    if(!connections.isEmpty()){
                        for(Connection connection:connections ){
                            //Only sends search query to one mapping neighbour if there are many
                            packet = " SER " + keyword + " " + hops + " " + SearcherIPAddress + " " + searcherport ;
                            String userCommand = Node.getUniversalCommand(packet);
                            Node.sendRequest(userCommand,connection.getIp(),connection.getPort());
                            break;
                        }
                    }
                }
            }else{
                //else send to connections
                if (hops > 1) {
                    ArrayList<Connection> connections=node.getConnections();
                    Collections.sort(connections,new CustomComparator());
                    for (Connection connection : connections) {
                        String IP = connection.getIp();
                        String connectionPort = connection.getPort();
                        packet = " SER " + keyword + " " + hops + " " + SearcherIPAddress + " " +  port;
                        String userCommand = Node.getUniversalCommand(packet);
                        Node.sendRequest(userCommand,IP,connectionPort);
                    }
                }else{
                    System.out.println("Number of hops has expired for search. Aborting due to that!");
                }
            }


        }


    }

    private ArrayList<String> containsKeyWord(String keyword){
        System.out.println("checking if neighbors contain key");
        ArrayList<String> files=new ArrayList<String>();
        Set<String> keys = neighbourFileList.keySet();
        for(String key :keys){
            if(key.matches(".*\\b"+keyword+"\\b.*")){
                files.add(key);
                System.out.println("there is a match in neighbors");
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
