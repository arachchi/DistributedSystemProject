package lk.ac.mrt.cse;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Created by kulakshi on 1/19/16.
 */
public class AddFile extends JFrame{
    private JButton button1;
     JPanel panel1;
    private JTextField textField1;
    private JTextPane textPane1;
    private JButton nextButton;


    public AddFile(ArrayList<String> fileList) {
        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        //pack();
        setTitle("Client : Add New Files");


        if(fileList.size()<1){
            textPane1.setText("No files are added yet..");
        }else{
            displayList(fileList);
        }


        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileList.add(textField1.getText());
                displayList(fileList);
            }
        });

        Node n = new Node(fileList);

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                ///private
                RegWindow r = new RegWindow(n);

                r.setVisible(true);

                JFrame f = new JFrame("ServerLogWindow");
                ServerLogWindow sw=new ServerLogWindow();
                f.setContentPane(sw.panel1);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setSize(500,500);
                f.setLocation(500,0);
               // f.pack();
                f.setTitle("Server : log");
                f.setVisible(true);

            }
        });
    }

    private void displayList(ArrayList<String> fileList){
        String list = "";
        for(String s :fileList){
            list += s +"\n";
        }
        textPane1.setText(list);
    }
}
