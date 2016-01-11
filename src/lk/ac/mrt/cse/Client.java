package lk.ac.mrt.cse;


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

    public Client(String port){
        this.port = Integer.parseInt(port);
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
            Node.sendRequest(packet);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void search(String keyword){

        //Passes the search query to the local server, then the local server handles the query
        InetAddress IPAddress = null;
        try {
            IPAddress = InetAddress.getByName("localhost");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String packet = "SEARCH " + keyword + " " + hops + " " + IPAddress + " " + port;

        Node.sendRequest(packet);
    }

}
