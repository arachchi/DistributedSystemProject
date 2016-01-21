package main.java.lk.ac.mrt.cse;

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

    public SearchWindow(final Client c) {

        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //pack();
        setSize(500, 500);
        setTitle("Client : Search Files");

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String status = c.search(textField1.getText());
                StatusLabel.setText(status);

            }
        });
    }


}
