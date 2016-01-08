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

        return true;
    }




}
