package lk.ac.mrt.cse;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

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


    String bsIP="";
    String bsPort="";
    String localPort="";
    String username="";
    final Node node;

    public RegWindow(final Node node) {
        ServerLog log = new ServerLog(node);

        this.node = node;
        setContentPane(panel1);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 500);
        //pack();
        setTitle("Client : Connect with Network");
        joinWithNetworkButton.setEnabled(false);


        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bsIP = textField1.getText();
                bsPort = textField4.getText();
                localPort = textField3.getText();
                username = textField2.getText();

                node.setBS_Port(Integer.parseInt(bsPort));
                node.setBsIp(bsIP);
                node.setPort(localPort);
                node.setUserName(username);

                boolean registered = false;

                    registered = node.execute();
                    textArea1.setText(node.getStaus());

                    if(registered){
                        joinWithNetworkButton.setEnabled(true);
                        connectButton.setEnabled(false);
                        showLogButton.setEnabled(false);
                        node.server.addObserver(log);
                        node.client.addObserver(log);
                        log.setVisible(true);
                        showLogButton.setEnabled(true);
                    }
            }
        });
        joinWithNetworkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                node.client.init();
            }
        });
        bookSearchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                SearchWindow sw = new SearchWindow(node.client);
                sw.setVisible(true);
            }
        });
        showLogButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                log.setVisible(true);
            }
        });
    }


    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

}
