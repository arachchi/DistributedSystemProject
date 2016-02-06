package lk.ac.mrt.cse.gui;

import lk.ac.mrt.cse.system.Server;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by kulakshi on 1/20/16.
 */
public class SearchWindow extends  JFrame{
    private JButton searchButton;
    private JTextField textField1;
    private JLabel StatusLabel;
    private JPanel panel1;
    private JButton closeButton;

    public SearchWindow(final Server server) {

        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 500);
        setTitle("Client : Search Files");

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String status = server.search(textField1.getText());
                StatusLabel.setText(status);
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
        setVisible(false);
    }

}
