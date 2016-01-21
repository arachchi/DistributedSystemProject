package lk.ac.mrt.cse;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by kulakshi on 1/21/16.
 */
public class ServerLog extends  JFrame implements Observer{
    private JTextArea textArea1;
    private JPanel panel1;
    private JTextArea textArea2;
    private JButton closeButton;
    Node node;

    ServerLog(Node node){
        this.node = node;
        setContentPane(panel1);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(500, 500);

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
    }

    @Override
    public void update(Observable o, Object arg) {
        textArea1.append(node.client.consoleMsg+"\n");
        textArea2.append(node.server.consoleMsg+"\n");
    }

    private void close(){
        setVisible(false);
    }
}
