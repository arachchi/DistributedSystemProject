package lk.ac.mrt.cse;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

/**
 * @author nuran
 * @version 1.0.
 * @since 1/4/16
 */
class Node implements Serializable {
    ArrayList<String> fileList;
    ArrayList<Connection> connections;
    InetAddress ipAddress;
    final static int port=9878;
    final static int size=1024;

    public Node(){
        fileList = new ArrayList<String>();
        connections = new ArrayList<Connection>();
    }


    public void server() throws Exception{
        DatagramSocket serverSocket = new DatagramSocket(port);
        byte[] receiveData = new byte[size];
        byte[] sendData = new byte[size];

        while(true)
        {
            System.out.println("Server is waiting:");
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            String sentence = new String( receivePacket.getData(),0,receivePacket.getLength());

            InetAddress IPAddress = receivePacket.getAddress();

            int port = receivePacket.getPort();
            String capitalizedSentence = sentence.toUpperCase();
            sendData = capitalizedSentence.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
            serverSocket.send(sendPacket);
        }
    }

    public void requestProcess(String sentence){
        String[] message = sentence.split(" ");

        if(message[0].equals("INIT")){
            //Connection will be established Ip and port will be saved
            Connection connection = new Connection(message[1],message[2]);//ip , port
            connections.add(connection);;
        }
        else if(message[0].equals("SEARCH")){
            search(message);
        }
        System.out.println("RECEIVED: " + sentence);
    }


    public void search(String[] message){//Search Query is SEARCH filename no_of_hops searcher's_ip searcher's_port
        //search query runs here



    }

    public void client() throws Exception{

        DatagramSocket clientSocket = new DatagramSocket();

        InetAddress IPAddress = InetAddress.getByName("localhost");

        byte[] sendData = new byte[size];
        byte[] receiveData = new byte[size];

        String sentence;

        sentence = "INIT "+IPAddress.getHostAddress()+" "+ port;
        sendData = sentence.getBytes();

        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
        clientSocket.send(sendPacket);
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
        String modifiedSentence = new String(receivePacket.getData(),0,receivePacket.getLength());
        System.out.println("FROM SERVER:" + modifiedSentence);
        clientSocket.close();

    }

}
