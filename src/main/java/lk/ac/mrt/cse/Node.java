package lk.ac.mrt.cse;


import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Observable;

/**
 * @author nuran
 * @version 1.0.
 * @since 1/4/16
 */
class Node extends Observable implements Serializable {
    private static InetAddress myIp;
    private static int BSServerPort = 1026;
    private static String ip = "127.0.0.1";
    private static  int size=1024;

    private static int BS_Port;
    private static String BS_IP;
    private static String port;
    private static String nodeIp;
    private static String userName;
    ArrayList<String> fileList;


    public static int getBS_Port() {
        return BS_Port;
    }

    public static String getBsIp() {
        return BS_IP;
    }

    public static String getNodeIp() {
        return nodeIp;
    }

    public static String getPort() {
        return port;
    }

    Node(ArrayList<String> fileList){
        this.fileList = fileList;
        getMyIp();
    }

    private static ArrayList<Connection> nodeListbyBS = new ArrayList<Connection>();

    public static void setBS_Port(int BS_Port) {
        Node.BS_Port = BS_Port;
    }

    public static void setBsIp(String bsIp) {
        BS_IP = bsIp;
    }

    public static void setPort(String port) {
        Node.port = port;
    }

    public static void setNodeListbyBS(ArrayList<Connection> nodeListbyBS) {
        Node.nodeListbyBS = nodeListbyBS;
    }

    public static void setUserName(String userName) {
        Node.userName = userName;
    }

    private static String status;
    public String getStaus(){return status;}
    Server server;
    Client client;

    public boolean execute(){
        // String command;
        boolean registration = registerToServer();
        if(registration){
            server = new Server(port,fileList);
            Thread serverThread = new Thread(server);
            client = new Client(port,fileList);
            serverThread.start();
            return true;
        }else{
            return false;
        }

    }
    public static String sendRequest(String packet,String ip,String port){
        try {
            InetAddress IPAddress = InetAddress.getByName(ip);
            return sendRequest(packet,IPAddress,port);
        }catch (Exception e){
            e.printStackTrace();
            return "Error";
        }
    }
    public static String sendRequest(String packet,InetAddress IPAddress,String port){
        try {
            DatagramSocket clientSocket = new DatagramSocket();

            byte[] sendData;
//            byte[] receiveData = new byte[size];
            sendData = packet.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Integer.parseInt(port));
            clientSocket.send(sendPacket);
//            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
//            clientSocket.receive(receivePacket);
//            String modifiedSentence = new String(receivePacket.getData(), 0, receivePacket.getLength());
            clientSocket.close();
            return "RECEIVED:" ;//+ modifiedSentence;

        }catch (Exception e){
            e.printStackTrace();
            return "Error";
        }
    }

    public static String getUniversalCommand(String command){

        String length= String.format("%04d", command.length() + 4); //Length is always represented as 4 digits
        command = length.concat(command);
        /*int fullLength = command.length() + 4;

        String fullLengthStr = "";
        for(int i=0; i < 4 - Integer.toString(fullLength).length() ; i++){
            fullLengthStr +=  "0";
        }
        fullLengthStr += Integer.toString(fullLength);

        String userCommand = fullLengthStr + command;*/

        return command;
    }

    public static boolean registerToServer(){
        //Connect to the bootstrap server and get the list of nodes
        //send these list of nodes to the server instance
        //if you cannot connect to the server using the user name and the password, ask a port and other details again
        //then connect to the bootstrap server again

        boolean registered = false;

        try{
            //BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
            // System.out.println("Enter Username to Register with BS");
            //String userName = inFromUser.readLine();

            InetAddress IP = Node.getIp();
            String ipAddress = IP.getHostAddress();
            nodeIp = ipAddress;

            String command = " REG " + ipAddress + " " + port + " " + userName;

            String userCommand = getUniversalCommand(command);

            Socket clientSocket = new Socket(BS_IP, BS_Port);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            outToServer.write(userCommand.getBytes());

            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String serverResponse = inFromServer.readLine();
            String[] serverResponseParts = serverResponse.split(" ");

            if(serverResponseParts.length==2){
                System.out.println("Error Message:" + serverResponseParts[1]);
                status = "Error Message:" + serverResponseParts[1];
            }
            else{
                if("REGOK".equals(serverResponseParts[1])){

                    int noOfNodes = Integer.parseInt(serverResponseParts[2]);
                    int count=0;
                    int index = 3;
                    String ip, port, username;

                    if(noOfNodes == 9999){
                        System.out.println("failed, there is some error in the command");
                        status = "failed, there is some error in the command";
                    } else if(noOfNodes == 9998){
                        System.out.println("failed, already registered to you, unregister first");
                        status = "failed, already registered to you, unregister first";
                    } else if(noOfNodes == 9997){
                        System.out.println("failed, registered to another user, try a different IP and port");
                        status = "failed, registered to another user, try a different IP and port";
                    } else if(noOfNodes == 9996){
                        System.out.println("failed, can’t register. BS full");
                        status = "failed, can’t register. BS full";
                    } else{
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
            System.out.println("Successfully Registered");
            status = "Successfully Registered \n";

            for(Connection con : nodeListbyBS){
                System.out.println(con.getIp() + " " + con.getPort() + " " + con.getUserName());
                status+= con.getIp() + " " + con.getPort() + " " + con.getUserName()+"\n";
            }
        }
        else{
            System.out.println("Registration Failure, Check Again");
            status = "Registration Failure, Check Again";
        }

        return registered;
    }

    public static boolean unRegisterToServer(){
        //Connect to the bootstrap server and unregister from the BS
        //send these list of nodes to the server instance
        //if you cannot connect to the server using the user name and the password, ask a port and other details again
        //then connect to the bootstrap server again

        boolean registered = false;

        try{
            //BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
            // System.out.println("Enter Username to Register with BS");
            //String userName = inFromUser.readLine();

            InetAddress IP = Node.getIp();
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
                status = "Error Message:" + serverResponseParts[1];
            }
            else{
                if("UNROK".equals(serverResponseParts[1])){

                    int noOfNodes = Integer.parseInt(serverResponseParts[2]);
                    int count=0;
                    int index = 3;
                    String ip, port, username;

                    if(noOfNodes == 9999){
                        System.out.println("failed, there is some error in the command");
                        status = "failed, there is some error in the command";
                    } else{
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
            System.out.println("Successfully Registered");
            status = "Successfully Registered";

            for(Connection con : nodeListbyBS){
                System.out.println(con.getIp() + " " + con.getPort() + " " + con.getUserName());
            }
        }
        else{
            System.out.println("Registration Failure, Check Again");
            status = "Registration Failure, Check Again";
        }

        return registered;
    }

    public static void getMyIp(){
        try {
            for (final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces( );interfaces.hasMoreElements( ); )
            {
                final NetworkInterface cur = interfaces.nextElement( );
                if ( cur.isLoopback( ) ) continue;
                System.out.println( "interface " + cur.getName( ) );
                for ( final InterfaceAddress addr : cur.getInterfaceAddresses( ) ) {

                    myIp = addr.getAddress( );
                    if ( !( myIp instanceof Inet4Address) ) continue;

                    System.out.println("  address: " + myIp.getHostAddress() +"/" + addr.getNetworkPrefixLength());
                    System.out.println("  broadcast address: " +addr.getBroadcast( ).getHostAddress( ));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if (myIp == null) {
                    myIp = InetAddress.getLocalHost();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static InetAddress getIp(){
        return myIp;
    }

    public static String getHostAddress(){
        return myIp.getHostAddress();
    }

    public static ArrayList<Connection> getNodeListbyBS() {
        return nodeListbyBS;
    }
}
