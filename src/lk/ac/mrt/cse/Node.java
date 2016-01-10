package lk.ac.mrt.cse;

import com.sun.org.apache.xpath.internal.SourceTree;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author nuran
 * @version 1.0.
 * @since 1/4/16
 */
class Node implements Serializable {

    private static ArrayList<Connection> nodeListbyBS = new ArrayList<Connection>();
    private static int BS_Port;
    private static String BS_IP;
    private static String port;

    public static void main(String args[]){
        String command;
        Server server;
        Client client;
        boolean begin = true,registration;

        System.out.println("Enter the ip and port of the Bootstrap Server eg 1:- 52.74.101.117 1027");
        Scanner scanner = new Scanner(System.in);

        String bsDetails = scanner.nextLine();
        String[] bsData = bsDetails.split(" ");

        BS_IP = bsData[0];
        BS_Port = Integer.parseInt(bsData[1]);

        while(begin) {
            System.out.println("Enter the port you need to start the server eg 1:- START 9876 ");

            command = scanner.next();
            if (command.equals("START")) {
                port = scanner.next();
                registration = registerToServer();
                if(registration){
                    server = new Server(port);
                    client = new Client(port);
                    server.start();
                    client.start();
                    begin=false;
                }
            }
        }
    }

    public static boolean registerToServer(){
        //registration details are included here.
        //Connect to the bootstrap server and get the list of nodes
        //send these list of nodes to the server instance
        //if you cannot connect to the server using the user name and the password, ask a port and other details again
        //then connect to the bootstrap server again

        boolean registered = false;

        try{
            BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
            System.out.println("Enter Username to Register with BS");
            String userName = inFromUser.readLine();

            InetAddress IP = InetAddress.getLocalHost();
            String ipAddress = IP.getHostAddress();

            String command = " REG " + ipAddress + " " + port + " " + userName;
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
            }
            else{
                if("REGOK".equals(serverResponseParts[1])){

                    int noOfNodes = Integer.parseInt(serverResponseParts[2]);
                    int count=0;
                    int index = 3;
                    String ip, port, username;

                    if(noOfNodes == 9999){
                        System.out.println("failed, there is some error in the command");
                    } else if(noOfNodes == 9998){
                        System.out.println("failed, already registered to you, unregister first");
                    } else if(noOfNodes == 9997){
                        System.out.println("failed, registered to another user, try a different IP and port");
                    } else if(noOfNodes == 9996){
                        System.out.println("failed, can’t register. BS full");
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

            for(Connection con : nodeListbyBS){
                System.out.println(con.getIp() + " " + con.getPort() + " " + con.getUserName());
            }
        }
        else{
            System.out.println("Registration Failure, Check Again");
        }

        return registered;
    }

    public static ArrayList<Connection> getNodeListbyBS() {
        return nodeListbyBS;
    }
}
