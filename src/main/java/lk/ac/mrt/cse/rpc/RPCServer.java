package lk.ac.mrt.cse.rpc;

import lk.ac.mrt.cse.rpc.impl.NodeServiceImpl;
import lk.ac.mrt.cse.system.model.Connection;
import lk.ac.mrt.cse.util.ConnectionTable;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

import java.util.ArrayList;

/**
 * Created by sabra on 2/6/16.
 */
public class RPCServer {
    ArrayList<String> fileList;
    private String port;
    private ConnectionTable routingTable;
    public RPCServer(ConnectionTable routingTable){
        this.routingTable=routingTable;
    }

    public void start(ArrayList<String> fileList) {
        try {
            TServerSocket serverTransport = new TServerSocket(Integer.parseInt(port));

            NodeService.Processor processor = new NodeService.Processor(new NodeServiceImpl(this.routingTable,fileList));

            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).
                    processor(processor));
            System.out.println("Starting the RPC server on  ...");
            server.serve();
        } catch (TTransportException e) {
            e.printStackTrace();
        }
    }

    public void setPort(String port) {
        this.port = port;
    }
    public void setRoutingTable(ConnectionTable routingTable){this.routingTable=routingTable;}
}
