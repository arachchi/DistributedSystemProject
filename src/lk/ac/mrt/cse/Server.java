package lk.ac.mrt.cse;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;
//import java.util.Hashtable;
/**
 * @author nuran
 * @version 1.0.
 * @since 1/8/16
 */
public class Server extends Thread {
    ArrayList<String> fileList;
    ArrayList<Connection> connections;// Routing Table
    Hashtable<String, ArrayList<String>> neighbourFileList;

    int port=9878;
    final static int size=1024;

    public Server(){
        fileList = new ArrayList<String>();
        connections = new ArrayList<Connection>();
        neighbourFileList = new Hashtable<String, ArrayList<String>>();
    }

    public Server(String port){
        this.port=Integer.parseInt(port);
        fileList = new ArrayList<String>();
        connections = new ArrayList<Connection>();
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
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void requestProcess(String query){
        String[] message = query.split(" ");

        if(message[0].equals("INIT")){
            //Connection will be established; Ip and port will be saved
            Connection connection = new Connection(message[1],message[2]);//ip , port
            connections.add(connection);;
        }
        else if(message[0].equals("SEARCH")){
            search(message);
        }
        System.out.println("RECEIVED: " + query);
    }

    public void search(String[] message){//Search Query is SEARCH filename no_of_hops searcher's_ip searcher's_port
        //search query runs here


        String keyword = message[1];
        int hops = Integer.parseInt(message[2])-1;
        String SearcherIPAddress = message[3];
        String port = message[4];

        hops--;//should be handled in server side
        String packet = "";
        boolean hasBook = false;//Flags whether this node contains the file
        ArrayList<String> books=new ArrayList<String>();;

        String searchResults = "";//search results
        int no_files = 0;//no of search results
        for (String file : fileList) {
            //If the keyword is contained in the file name as a word or set of words
            if (file.matches(".*\\b"+keyword+"\\b.*")) {
                hasBook = true;
                searchResults += " " + file ;
                no_files++;
            }
        }
        if(!hasBook){
            books= containsKeyWord(neighbourFileList,keyword);
        }

        if (hasBook) {//this node has the keyword
            InetAddress IPAddress = null;
            try {
                IPAddress = InetAddress.getByName("localhost");
            } catch (Exception e) {
                e.printStackTrace();
            }
            packet = "SEARCHOK " + no_files + " " + IPAddress.getHostAddress() + " " + port + " " + hops + searchResults;

        }
        else if (!books.isEmpty()) {//neighbour nodes have the keyword

            for(String book: books){
                ArrayList<String> IPAddresses = neighbourFileList.get(book);
                if(!IPAddresses.isEmpty()){
                    for(String IP:IPAddresses ){
                        packet = "SEARCH " + book + " " + hops + " " + IP + " " + port;
                    }

                }

            }
        } else { //otherwise
            if (hops > 1) {
                //number of hops should be checked at the server side by reading search message request
                //and only forward to client if not expired

                //forward the message if not expired
                for (Connection connection : connections) {
                    String IP = connection.getIp(); //port = connection.getPort();
                    packet = "SEARCH " + keyword + " " + hops + " " + IP + " " + port;
                }

            }
        }

        Node.sendRequest(packet);


    }
    private ArrayList<String> containsKeyWord(Hashtable<String, ArrayList<String>> neighbourFileList,String keyword){
        ArrayList<String> books=new ArrayList<String>();
        Set<String> keys = neighbourFileList.keySet();
        for(String key :keys){
            if(key.matches(".*\\b"+keyword+"\\b.*")){
                books.add(key);
            }
        }
        return  books;
    }
}
