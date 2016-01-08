package lk.ac.mrt.cse;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * @author nuran
 * @version 1.0.
 * @since 1/8/16
 */
public class Client extends Thread {
    int size=1024;
    int port=9878;

    public Client(String port){
        this.port = Integer.parseInt(port);
    }


    public void init(){
        try {
            DatagramSocket clientSocket = new DatagramSocket();

            InetAddress IPAddress = InetAddress.getByName("localhost");

            byte[] sendData;
            byte[] receiveData = new byte[size];

            String sentence;

            sentence = "INIT " + IPAddress.getHostAddress() + " " + port;
            sendData = sentence.getBytes();

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
    public void run(){
        Scanner scanner = new Scanner(System.in);
        String command,fileName;
        while(true){
            System.out.println("Enter your command. eg 1:- INTI eg 2:- SEARCH file_name");
            command = scanner.next();
            if(command.equals("INIT"))
                init();
            else if(command.equals("SEARCH")){
                fileName = scanner.next();
            }
        }
    }
}
