package lk.ac.mrt.cse.util;

import java.net.*;
import java.util.Enumeration;

/**
 * @author nuran
 * @version 1.0.
 * @since 2/6/16
 */
public class Utility {
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
            sendData = packet.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Integer.parseInt(port));
            clientSocket.send(sendPacket);
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
        return command;
    }

    public static InetAddress getMyIp(){
        InetAddress myIp = null;
        try {
            for (final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces( );interfaces.hasMoreElements( ); )
            {
                final NetworkInterface cur = interfaces.nextElement( );
                if ( cur.isLoopback( ) ) continue;
                for ( final InterfaceAddress addr : cur.getInterfaceAddresses( ) ) {

                    myIp = addr.getAddress( );
                    if ( !( myIp instanceof Inet4Address) ) continue;
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
            return myIp;
        }
    }
    public static String getHostAddress(InetAddress myIp){
        if(myIp != null)
            return myIp.getHostAddress();
        else
            try {
                if (myIp == null) {
                    myIp = InetAddress.getLocalHost();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return myIp.getHostAddress();
    }

    public static String getHostAddress(){
        return getMyIp().getHostAddress();
    }
}
