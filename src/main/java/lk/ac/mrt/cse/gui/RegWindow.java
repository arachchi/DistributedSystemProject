package lk.ac.mrt.cse.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import lk.ac.mrt.cse.rpc.RPCServer;
import lk.ac.mrt.cse.rpc.impl.RPCClientImpl;
import lk.ac.mrt.cse.system.Client;
import lk.ac.mrt.cse.system.Server;
import lk.ac.mrt.cse.system.imp.ClientImpl;
import lk.ac.mrt.cse.system.model.Connection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by kulakshi on 1/19/16.
 */
public class RegWindow extends JFrame implements Observer {
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JTextField textField4;
    private JButton connectButton;
    private JCheckBox selectRPCCheckBox;
    JPanel panel1;
    private JLabel statusLabel;
    private JButton joinWithNetworkButton;
    private JButton bookSearchButton;
    private JTextArea textArea1;
    private JButton showLogButton;
    private JButton closeButton;
    public int count = 0;
    private boolean rpc;

    String bsIP = "";
    String bsPort = "";
    String localPort = "";
    String username = "";
    Server server;

    public RegWindow(final Server server, final boolean rpc) {
        final ServerLog log = new ServerLog(server, rpc);

        this.server = server;
        this.rpc = rpc;
        setContentPane(panel1);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 500);
        setTitle("Client : Connect with Network");
        joinWithNetworkButton.setEnabled(false);
        selectRPCCheckBox.setEnabled(false);

        if (rpc) {
            selectRPCCheckBox.setSelected(true);
        } else {
            selectRPCCheckBox.setSelected(false);
        }

        final RegWindow thisWindow = this;
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = 8;
                System.out.println("testing initiated.");
                if (connectButton.getText() == "Register") {

                    bsIP = textField1.getText();
                    bsPort = textField4.getText();
                    localPort = textField3.getText();
                    username = textField2.getText();

                    server.setBS_Port(Integer.parseInt(bsPort));
                    server.setBsIp(bsIP);
                    server.setPort(localPort);
                    server.setUserName(username);
                    Client client;
                    if (rpc)
                        client = new RPCClientImpl(server.getFileList(), localPort, bsIP, Integer.parseInt(bsPort), username);
                    else
                        client = new ClientImpl(server.getRoutingTable(), server.getFileList(), localPort, bsIP, Integer.parseInt(bsPort), username);
                    server.setClient(client);

                    boolean registered = false;
                    server.addClientObserver(thisWindow);
                    registered = server.registerToServer();

                    if (registered) {
                        Thread serverThread = new Thread(server);
                        serverThread.start();
                        server.addObserver(log);
                        server.addClientObserver(log);
                        //log.setVisible(true);
                        log.setSize(getWidth(), getHeight());

                        joinWithNetworkButton.setEnabled(true);
                        showLogButton.setEnabled(true);

                        connectButton.setText("Unregister");
                    }
                    // textArea1.setText("Ok Registration Window");

                } else {
                    server.unRegisterToServer();

                    connectButton.setText("Register");
                }
            }
        });

        joinWithNetworkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                server.joinNetwork();
                ArrayList<Connection> con = server.getClient().getConnectedNodes();

                for (Connection conn : con) {
                    System.out.println(conn);
                }

                server.setConnectedNodesList(con);
            }
        });
        bookSearchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SearchWindow sw = new SearchWindow(server, rpc);

                sw.setSize(getWidth(), getHeight());
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


    private void close() {
        System.exit(0);
    }

    @Override
    public void update(Observable o, Object arg) {
        textArea1.append(server.getClientStatus() + "\n");
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(9, 5, new Insets(5, 5, 5, 5), -1, -1));
        textField1 = new JTextField();
        textField1.setText("127.0.0.1");
        panel1.add(textField1, new GridConstraints(0, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        textField3 = new JTextField();
        textField3.setText("9877");
        panel1.add(textField3, new GridConstraints(2, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        textField4 = new JTextField();
        textField4.setText("1025");
        panel1.add(textField4, new GridConstraints(1, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        joinWithNetworkButton = new JButton();
        joinWithNetworkButton.setText("Join with network");
        panel1.add(joinWithNetworkButton, new GridConstraints(7, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textField2 = new JTextField();
        textField2.setText("bashi");
        panel1.add(textField2, new GridConstraints(3, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        statusLabel = new JLabel();
        statusLabel.setText("Client Status : ");
        panel1.add(statusLabel, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Username :");
        panel1.add(label1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(75, 18), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("local port :");
        panel1.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(75, 18), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("BS port :");
        panel1.add(label3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(75, 18), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("BS IP : ");
        panel1.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(75, 18), null, 0, false));
        connectButton = new JButton();
        connectButton.setText("Register");
        panel1.add(connectButton, new GridConstraints(6, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        showLogButton = new JButton();
        showLogButton.setText("Show Log");
        panel1.add(showLogButton, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(5, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        textArea1 = new JTextArea();
        scrollPane1.setViewportView(textArea1);
        bookSearchButton = new JButton();
        bookSearchButton.setText("Book Search");
        panel1.add(bookSearchButton, new GridConstraints(8, 2, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        closeButton = new JButton();
        closeButton.setText("Close");
        panel1.add(closeButton, new GridConstraints(8, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        selectRPCCheckBox = new JCheckBox();
        selectRPCCheckBox.setText(" RPC");
        panel1.add(selectRPCCheckBox, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }
}
