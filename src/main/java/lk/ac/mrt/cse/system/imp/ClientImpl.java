package lk.ac.mrt.cse.system.imp;


import lk.ac.mrt.cse.system.Client;
import lk.ac.mrt.cse.system.model.Connection;
import lk.ac.mrt.cse.util.ConnectionTable;
import lk.ac.mrt.cse.util.Utility;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Random;

/**
 * @author nuran
 * @version 1.0.
 * @since 1/8/16
 */
public class ClientImpl extends Observable implements Client {
    int hops=5;
    private ArrayList<String> fileList;
    private int connectingNodeCount = 2;
    private ArrayList<Connection> connectingNodesList = new ArrayList<Connection>(); //Nodes connected by this node
    private ArrayList<Connection> nodeListbyBS = new ArrayList<Connection>();
    private String consoleMsg;
    ConnectionTable routingTable;
    boolean isConsole;
    boolean isStatus;



    private static int BS_Port;
    private static String BS_IP;
    private static String port;
    private static String nodeIp;
    private static String userName;
    private String status;

    public ClientImpl(ConnectionTable routingTable,ArrayList<String> fileList,String port,String BS_IP,int BS_Port, String userName){
        this.routingTable=routingTable;
        this.fileList = fileList;
        this.port = port;
        this.BS_IP = BS_IP;
        this.BS_Port = BS_Port;
        this.userName = userName;
        consoleMsg="";
        isConsole=false;
        isStatus = false;
    }


    public void init(){
        System.out.println("In init");
        setConsoleMessage("In init..");
        try {

            //Select two nodes to connect
            int min, max = nodeListbyBS.size();

            if(max >0) {

                //Connect to only two selected random nodes
                if (nodeListbyBS.size() > 3) {
                    min = 1;

                    //Get random numbers
                    Random rand = new Random();
                    int randomNum;

                    for (int i = 0; i < connectingNodeCount; i++) {
                        randomNum = rand.nextInt(max);
                        if(randomNum>=max)
                            randomNum= max-1;
                        connectingNodesList.add(nodeListbyBS.get(randomNum));
                    }

                    //Connect to the selected nodes
                    for (int i = 0; i < connectingNodesList.size(); i++) {
                        connectToNode(connectingNodesList.get(i));
                    }

                } else { //Connect to all nodes in the list
                    for( int i =0; i < nodeListbyBS.size(); i++){
                        connectToNode(nodeListbyBS.get(i));
                    }
                }
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void connectToNode(Connection con){
        this.routingTable.addConnections(con);
        //Generating packet to send
        String command = " JOIN " + Utility.getHostAddress() + " " + port;
        String packet = Utility.getUniversalCommand(command);
        Utility.sendRequest(packet, con.getIp(), "" + con.getPort());
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
            setConsoleMessage("The searched keyword is present in my list of files.");
            return "The searched keyword is present in my list of files.";
        }
        else{
            String packet = " SER " + keyword + " " + hops + " " + Utility.getHostAddress() + " " + port;
            String userCommand = Utility.getUniversalCommand(packet);

            setConsoleMessage("Search request is forwarded to the network");
            //return Node.sendRequest(userCommand, Node.getHostAddress(),""+port);
            return userCommand;
        }
    }

    public String getConsoleMsg() {
        return consoleMsg;
    }

    public boolean registerToServer(){
        //Connect to the bootstrap server and get the list of nodes
        //send these list of nodes to the server instance
        //if you cannot connect to the server using the user name and the password, ask a port and other details again
        //then connect to the bootstrap server again

        boolean registered = false;

        try{
            //BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
            // System.out.println("Enter Username to Register with BS");
            //String userName = inFromUser.readLine();

            InetAddress IP = Utility.getMyIp();
            String ipAddress = IP.getHostAddress();
            nodeIp = ipAddress;

            String command = " REG " + ipAddress + " " + port + " " + userName;

            String userCommand = Utility.getUniversalCommand(command);
            System.out.println("BS_IP "+BS_IP+" port "+BS_Port);
            setStatus("BS_IP "+BS_IP+" port "+BS_Port);
            Socket clientSocket = new Socket(BS_IP, BS_Port);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            outToServer.write(userCommand.getBytes());

            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String serverResponse = inFromServer.readLine();
            String[] serverResponseParts = serverResponse.split(" ");

            if(serverResponseParts.length==2){
                System.out.println("Error Message:" + serverResponseParts[1]);
                setStatus("Error Message:" + serverResponseParts[1]);
            }
            else{
                if("REGOK".equals(serverResponseParts[1])){

                    int noOfNodes = Integer.parseInt(serverResponseParts[2]);
                    int count=0;
                    int index = 3;
                    String ip, port, username;

                    if(noOfNodes == 9999){
                        System.out.println("failed, there is some error in the command");
                        setStatus("failed, there is some error in the command");
                    } else if(noOfNodes == 9998){
                        System.out.println("failed, already registered to you, unregister first");
                        setStatus("failed, already registered to you, unregister first");
                    } else if(noOfNodes == 9997){
                        System.out.println("failed, registered to another user, try a different IP and port");
                        setStatus("failed, registered to another user, try a different IP and port");
                    } else if(noOfNodes == 9996){
                        System.out.println("failed, can’t register. BS full");
                        setStatus("failed, can’t register. BS full");
                    } else{
                        System.out.println("Successfully Registered ");
                        setStatus("Successfully Registered");
                        while(count<noOfNodes){
                            ip = serverResponseParts[index];
                            port = serverResponseParts[++index];
                            username = serverResponseParts[++index];

                            nodeListbyBS.add(new Connection(ip, port, username));

                            index++;
                            count++;
                        }

                        registered = true;
                    }
                }
            }
            clientSocket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(registered){


            for(Connection con : nodeListbyBS){
                System.out.println(con.getIp() + " " + con.getPort() + " " + con.getUserName());
                setStatus( con.getIp() + " " + con.getPort() + " " + con.getUserName());
            }
        }
        else{
            System.out.println("Registration Failure, Check Again");
            setStatus("Registration Failure, Check Again");
        }

        return registered;
    }

    public boolean unRegisterToServer(){
        //Connect to the bootstrap server and unregister from the BS
        //send these list of nodes to the server instance
        //if you cannot connect to the server using the user name and the password, ask a port and other details again
        //then connect to the bootstrap server again

        boolean registered = false;

        //Send leave requests to all connections
        ArrayList<Connection> connections=routingTable.getConnections();
        for (Connection connection : connections) {
            String IP = connection.getIp();
            String connectionPort = connection.getPort();
            String packet = " LEAVE "+ Utility.getHostAddress()+" "+port;;
            String userCommand = Utility.getUniversalCommand(packet);
            Utility.sendRequest(userCommand, IP, connectionPort);
        }

        try{
            //BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
            // System.out.println("Enter Username to Register with BS");
            //String userName = inFromUser.readLine();

            InetAddress IP = Utility.getMyIp();
            String ipAddress = IP.getHostAddress();
            nodeIp = ipAddress;

            String command = " UNREG " + ipAddress + " " + port + " " + userName;
            int fullLength = command.length() + 4;

            String fullLengthStr = "";
            for(int i=0; i < 4 - Integer.toString(fullLength).length() ; i++){
                fullLengthStr +=  "0";
            }
            fullLengthStr += Integer.toString(fullLength);

            String userCommand = fullLengthStr + command;

            Socket clientSocket = new Socket(BS_IP, BS_Port);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            outToServer.write(userCommand.getBytes());

            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String serverResponse = inFromServer.readLine();
            String[] serverResponseParts = serverResponse.split(" ");

            if(serverResponseParts.length==2){
                System.out.println("Error Message:" + serverResponseParts[1]);
                setStatus("Error Message:" + serverResponseParts[1]);
            }
            else{
                if("UNROK".equals(serverResponseParts[1])){

                    int noOfNodes = Integer.parseInt(serverResponseParts[2]);
                    int count=0;
                    int index = 3;
                    String ip, port, username;

                    if(noOfNodes == 9999){
                        System.out.println("failed, there is some error in the command");
                        setStatus("failed, there is some error in the command");
                    } else{
                        System.out.println("Successfully Unregistered");
                        setStatus("Successfully Unregistered");
                        //TODO:unregister from the connected nodes
//                        while(count<noOfNodes){
//                            ip = serverResponseParts[index];
//                            port = serverResponseParts[++index];
//                            username = serverResponseParts[++index];
//
//                            nodeListbyBS.add(new Connection(ip, port, username));
//
//                            index++;
//                            count++;
//                        }

                        registered = true;
                    }
                }
            }
            clientSocket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(registered){


            for(Connection con : connectingNodesList){
                System.out.println(con.getIp() + " " + con.getPort() + " " + con.getUserName());
               // setStatus(con.getIp() + " " + con.getPort() + " " + con.getUserName());
            }
        }
        else{
            System.out.println("Unregistration Failure, Check Again");
            setStatus("Unregistration Failure, Check Again");
        }

        return registered;
    }


    private void setConsoleMessage(String consoleMsg){
        isConsole = true;
        this.consoleMsg = consoleMsg;
        setChanged();
        notifyObservers(isConsole);
        isConsole = false;
    }

    private void setStatus(String status){
        isStatus = true;
        this.status = status;
        setChanged();
        notifyObservers(isStatus);
        isStatus = false;
    }

    public String getStatus(){return status;}

    @Override
    public ArrayList<Connection> getConnectedNodes() {
        return connectingNodesList;
    }

}
