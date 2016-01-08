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
        System.out.println("Enter the port you need to start the server eg 1:- START 9876 ");
        Scanner scanner = new Scanner(System.in);

        command = scanner.next();
        if(command.equals("START")){
        String port = scanner.next();
        Server server = new Server(port);
        Client client = new Client(port);
            server.start();
            client.start();
        }
    }




}
