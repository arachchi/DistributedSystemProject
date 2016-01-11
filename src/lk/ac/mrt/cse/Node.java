package lk.ac.mrt.cse;

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
    private static int BSServerPort = 1026;
    private static String ip = "127.0.0.1";
    private static  int size=1024;
    private static int port=9878;

    public static void main(String args[]){
        String command;
        Server server;
        Client client;
        boolean begin = true,registration;
        while(begin) {
            System.out.println("Enter the port you need to start the server eg 1:- START 9876 ");
            Scanner scanner = new Scanner(System.in);

            command = scanner.next();
            if (command.equals("START")) {
                String port = scanner.next();
                registration = registerToServer();
                if(registration){
                    Node.port=Integer.parseInt(port);
                    server = new Server(port);
                    client = new Client(port);
                    server.start();
                    client.start();
                    begin=false;
                }
            }
        }
    }

    public static void sendRequest(String packet){
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

    public static boolean registerToServer(){
        //registration details are included here.
        //Connect to the bootstrap server and get the list of nodes
        //send these list of nodes to the server instance
        //if you cannot connect to the server using the user name and the password, ask a port and other details again
        //then connect to the bootstrap server again

        boolean registered = false;

        try{
            BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
            System.out.println("Enter Command to Register with BS");
            String userCommand = inFromUser.readLine();

            Socket clientSocket = new Socket(ip, BSServerPort);
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
