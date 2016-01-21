package lk.ac.mrt.cse;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

/**
 * @author nuran
 * @version 1.0.
 * @since 1/8/16
 */
public class Client extends  Observable{
    int size=1024;
    int port=9878;
    int hops=5;
    ArrayList<String> fileList;
    private int connectingNodeCount = 2;
    private ArrayList<Connection> connectingNodesList = new ArrayList<Connection>(); //Nodes connected by this node
    String consoleMsg;

    public Client(String port,ArrayList<String> fileList){
        this.fileList = fileList;
        this.port = Integer.parseInt(port);
        consoleMsg="";
    }


    public void init(){
        System.out.println("In init");
        consoleMsg = "In Init";
        setChanged();
        notifyObservers(consoleMsg);
        try {

            ArrayList<Connection> allNodes = Node.getNodeListbyBS();

            //Select two nodes to connect
            int min, max = allNodes.size();

            if(max >0) {

                //Connect to only two selected random nodes
                if (allNodes.size() > 3) {
                    min = 1;

                    //Get random numbers
                    Random rand = new Random();
                    int randomNum;

                    for (int i = 0; i < connectingNodeCount; i++) {
                        randomNum = rand.nextInt((max - min) + 1) + min;
                        connectingNodesList.add(allNodes.get(randomNum));
                    }

                    //Connect to the selected nodes
                    for (int i = 0; i < connectingNodesList.size(); i++) {
                        connectToNode(connectingNodesList.get(i));
                    }

                } else { //Connect to all nodes in the list
                    for( int i =0; i < allNodes.size(); i++){
                        connectToNode(allNodes.get(i));
                    }
                }

                //Inform BS of the presence
                connectToBS();
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void connectToNode(Connection con){
        String packet = "JOIN " + Node.getIp() + " " + Node.getPort();
        Node.sendRequest(packet,con.getIp(),""+con.getPort());
    }

    public void connectToBS() throws IOException {
            String command = " JOIN " + Node.getNodeIp() + " " + Node.getPort();
            int fullLength = command.length() + 4;

            String fullLengthStr = "";
            for(int i=0; i < 4 - Integer.toString(fullLength).length() ; i++){
                fullLengthStr +=  "0";
            }
            fullLengthStr += Integer.toString(fullLength);

            String userCommand = fullLengthStr + command;
        System.out.println(userCommand);
        consoleMsg = userCommand;
        setChanged();
        notifyObservers(consoleMsg);


            Socket clientSocket = new Socket(Node.getBsIp(), Node.getBS_Port());
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            outToServer.write(userCommand.getBytes());

            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String serverResponse = inFromServer.readLine();
            String[] serverResponseParts = serverResponse.split(" ");

            System.out.println(serverResponse);
        consoleMsg = serverResponse;
        setChanged();
        notifyObservers(consoleMsg);

            if(serverResponseParts.length==2){
                System.out.println("Error Message:" + serverResponseParts[1]);
                consoleMsg = "Error Message:" + serverResponseParts[1];
                setChanged();
                notifyObservers(consoleMsg);
            }
            else{
                if("JOINOK".equals(serverResponseParts[1])){

                    int responseCode = Integer.parseInt(serverResponseParts[2]);

                    if(responseCode == 9999) {
                        System.out.println("error while adding new node to routing table");
                        consoleMsg = "error while adding new node to routing table";
                        setChanged();
                        notifyObservers(consoleMsg);
                    }

                }
            }
            clientSocket.close();
    }

    public String search(String keyword){

        boolean hasBook = false;//Flags whether this node contains the file
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
        if(hasBook){
            return "The searched keyword is present in my list of files.";
        }
        else{
            //Passes the search query to the local server, then the local server handles the query
            InetAddress IPAddress = null;
            try {
                IPAddress = InetAddress.getByName("localhost");
            } catch (Exception e) {
                e.printStackTrace();
            }

            String packet = "SER " + keyword + " " + hops + " " + IPAddress + " " + port;
            String length= String.format("%04d", packet.length() + 4); //Length is always represented as 4 digits
            packet = length.concat(" "+ packet);

            return Node.sendRequest(packet,IPAddress,""+port);


        }
    }

}
