package lk.ac.mrt.cse;


import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author nuran
 * @version 1.0.
 * @since 1/8/16
 */
public class Client extends Thread {
    int size=1024;
    int port=9878;
    int hops=5;
    ArrayList<String> fileList;

    public Client(String port,ArrayList<String> fileList){
        this.fileList = fileList;
        this.port = Integer.parseInt(port);
    }

    public void run(){

    }




    public void init(){
        try {
            InetAddress IPAddress = InetAddress.getByName("localhost");
            String packet = "JOIN " + IPAddress.getHostAddress() + " " + port;
            Node.sendRequest(packet);
        }catch (Exception e){
            e.printStackTrace();
        }
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

            return Node.sendRequest(packet);


        }
    }

}
