package lk.ac.mrt.cse;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
//import java.util.Hashtable;
/**
 * @author nuran
 * @version 1.0.
 * @since 1/8/16
 */
public class Server extends Thread {
    ArrayList<String> fileList;
    ArrayList<Connection> connections;// Routing Table
	//neighbourFileList = new Hashtable<String, String>();
	
    int port=9878;
    final static int size=1024;

    public Server(){
        fileList = new ArrayList<String>();
        connections = new ArrayList<Connection>();
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

		
        String book = message[1];
        int hops = Integer.parseInt(message[2])-1;
        String IP = message[3];
        String port = message[4];
              
        hops--;
        
        /*
        String packet = "";
        if (fileList.contains(book)) {//this node has the book
            String searchResults = "";//search results
            int no_files = 0;//no of search results
            for (String file : fileList) {
                if (file.contains(book)) {
                    searchResults += file + " ";
                    no_files++;
                }
            }

            packet = "SEARCHOK " + no_files + " " + IP + " " + port + " " + hops + " " + searchResults;

        }else if (neighbourFileList.containsKey(book)) {//neighbour nodes have the book
      
         
        }else{
            if(hops>1){
                //forward to local client
            }else{
                //send not fount message
            }
        }
        */



    }

}
