package lk.ac.mrt.cse.gui;

import lk.ac.mrt.cse.rpc.RPCServer;
import lk.ac.mrt.cse.rpc.impl.RPCClientImpl;
import lk.ac.mrt.cse.system.Client;
import lk.ac.mrt.cse.system.Server;
import lk.ac.mrt.cse.system.imp.ClientImpl;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by kulakshi on 1/19/16.
 */
public class RegWindow extends JFrame{
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JTextField textField4;
    private JButton connectButton;
    JPanel panel1;
    private JLabel statusLabel;
    private JButton joinWithNetworkButton;
    private JButton bookSearchButton;
    private JTextArea textArea1;
    private JButton showLogButton;
    private JButton closeButton;
    public int count = 0;
    private boolean rpc;

    String bsIP="";
    String bsPort="";
    String localPort="";
    String username="";
    Server server;

    public RegWindow(final Server server,final boolean rpc) {
        final ServerLog log = new ServerLog(server,rpc);

        this.server = server;
        this.rpc = rpc;
        setContentPane(panel1);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 500);
        setTitle("Client : Connect with Network");
        joinWithNetworkButton.setEnabled(false);


        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i=8;
                System.out.println("testing initiated.");
                if(connectButton.getText()=="Register") {
                    System.out.println(textField1);
                    bsIP = textField1.getText();
                    bsPort = textField4.getText();
                    localPort = textField3.getText();
                    username = textField2.getText();

                    server.setBS_Port(Integer.parseInt(bsPort));
                    server.setBsIp(bsIP);
                    server.setPort(localPort);
                    server.setUserName(username);
                    Client client;
                    if(rpc)
                        client = new RPCClientImpl(server.getFileList(),localPort,bsIP,Integer.parseInt(bsPort),username);
                    else
                        client = new ClientImpl(server.getFileList(),localPort,bsIP,Integer.parseInt(bsPort),username);
                    server.setClient(client);

                    count++;
                    System.out.println("count value is "+count);

                    boolean registered = false;

                    registered = server.registerToServer();
                    if(registered){
                        Thread serverThread = new Thread(server);
                        serverThread.start();
                        server.addObserver(log);
                        server.addClientObserver(log);
                        log.setVisible(true);
                        log.setSize(getWidth(), getHeight());

                        joinWithNetworkButton.setEnabled(true);
                        showLogButton.setEnabled(true);

                        connectButton.setText("Unregister");
                    }
                    textArea1.setText("Ok Registration Window");

                }else{

                    server.unRegisterToServer();
                    connectButton.setText("Register");
                }
            }
        });
        joinWithNetworkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                server.joinNetwork();
            }
        });
        bookSearchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SearchWindow sw = new SearchWindow(server,rpc);

                sw.setSize(getWidth(),getHeight());
                sw.setVisible(true);
            }
        });
        showLogButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                log.setVisible(true);
            }
        });
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
    }


    private void close(){
        System.exit(0);
    }



}
