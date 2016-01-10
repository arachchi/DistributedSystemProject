package lk.ac.mrt.cse;

import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

/**
 * @author nuran
 * @version 1.0.
 * @since 1/8/16
 */
public class Client extends Thread {
    int size=1024;
    int port=9878;
    int hops=5;
	
	ArrayList<String> fileList;
    Hashtable<String, ArrayList<String>> neighbourFileList;
    ArrayList<Connection> connections;// Routing Table

    public Client(String port){
        this.port = Integer.parseInt(port);
		
		fileList = new ArrayList<String>();
        connections = new ArrayList<Connection>();
        neighbourFileList = new Hashtable<String, ArrayList<String>>();
    }


    public void sendRequest(String packet){
        try {
            DatagramSocket clientSocket = new DatagramSocket();

            InetAddress IPAddress = InetAddress.getByName("localhost");

            byte[] sendData;
            byte[] receiveData = new byte[size];

            sendData = packet.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
            clientSocket.send(sendPacket);
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);
            String modifiedSentence = new String(receivePacket.getData(), 0, receivePacket.getLength());

            System.out.println("FROM SERVER:" + modifiedSentence);
            clientSocket.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void run(){
        Scanner scanner = new Scanner(System.in);
        String command,fileName;
        while(true){
            System.out.println("Enter your command. eg 1:- INTI eg 2:- SEARCH file_name");
            command = scanner.next();
            if(command.equals("INIT")){
                init();
            }

            else if(command.equals("SEARCH")){
                fileName = scanner.next();
                search(fileName);
            }
        }
    }

    public void init(){
        try {
            InetAddress IPAddress = InetAddress.getByName("localhost");
            String packet = "INIT " + IPAddress.getHostAddress() + " " + port;
            sendRequest(packet);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void search(String keyword){
	
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

        sendRequest(packet);

	
		/*
        try {
            InetAddress IPAddress = InetAddress.getByName("localhost");
            String packet = "SEARCH " +keyword+ " "+hops+" "+IPAddress.getHostAddress() + " " + port;
            sendRequest(packet);
        }catch (Exception e){
            e.printStackTrace();
        }
		*/
    }

    public ArrayList<String> containsKeyWord(Hashtable<String, ArrayList<String>> neighbourFileList,String keyword){
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
