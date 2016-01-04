package lk.ac.mrt.cse;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * @author nuran
 * @version 1.0.
 * @since 1/4/16
 */
public class Node implements Serializable {
    ArrayList<String> fileList;
    InetAddress ipAddress;
    String port;

    public Node(){
        try {
            ipAddress = InetAddress.getLocalHost();
        }catch (Exception e){
            
        }

    }
}
