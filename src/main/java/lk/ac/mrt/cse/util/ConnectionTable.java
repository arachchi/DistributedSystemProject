package lk.ac.mrt.cse.util;

import lk.ac.mrt.cse.system.model.Connection;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

/**
 * Created by dilhasha on 2/7/16.
 */
public class ConnectionTable {
    public ConnectionTable(){
        connections = new ArrayList<Connection>();
        neighbourFileList=new Hashtable<String, ArrayList<Connection>>();
    }
    public ArrayList<Connection> getConnections() {
        return connections;
    }

    public void addConnections(Connection connection) {
        this.connections.add(connection);
    }
    public void removeConnection(Connection connection){
        this.connections.remove(connection);
    }

    public void updateNeighbourFileList(ArrayList<String> fileList, Connection connection){

        System.out.println("Connection " + connection.getIp() + " " + connection.getPort());

        ArrayList<Connection> existingConnections;
        for(String fileName: fileList){
            if(neighbourFileList.containsKey(fileName) && connection!=null){
                existingConnections = neighbourFileList.get(fileName);
            }else{
                existingConnections=new ArrayList<>();
            }
            existingConnections.add(connection);
            neighbourFileList.put(fileName,existingConnections);
        }
    }
    ArrayList<Connection> connections;// Routing Table

    public Hashtable<String, ArrayList<Connection>> getNeighbourFileList() {
        return neighbourFileList;
    }
    public void printNeighbourFileList(){
        Set<String> keys = neighbourFileList.keySet();
        final Object[][] table = new String[keys.size()][];
        int i=0;
        for(String key :keys){
            String listOfFiles="";
            ArrayList<Connection> connectionsList=neighbourFileList.get(key);
            for(Connection connection:connectionsList){
                listOfFiles = listOfFiles.concat(connection.getIp()+"("+connection.getPort()+"),");
            }
            table[i] = new String[] { key, listOfFiles};
            ++i;
        }
        for (final Object[] row : table) {
            System.out.format("%15s %15s\n", row);
        }
    }

    Hashtable<String, ArrayList<Connection>> neighbourFileList;
}
