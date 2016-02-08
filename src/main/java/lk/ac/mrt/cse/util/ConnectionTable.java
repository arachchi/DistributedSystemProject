package lk.ac.mrt.cse.util;

import lk.ac.mrt.cse.system.model.Connection;

import java.util.ArrayList;

/**
 * Created by dilhasha on 2/7/16.
 */
public class ConnectionTable {
    public ConnectionTable(){
        connections = new ArrayList<Connection>();
    }
    public ArrayList<Connection> getConnections() {
        return connections;
    }

    public void addConnections(Connection connection) {
        this.connections.add(connection);
    }

    ArrayList<Connection> connections;// Routing Table
}
