package lk.ac.mrt.cse.gui;

import lk.ac.mrt.cse.system.Server;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
    private JButton closeButton;
    ArrayList<String> totalFilesList;
    private static ArrayList<String> nodeFileList;
    private String RESOURCE_FILE_PATH = "resources/FileNames.txt";
    private int nodeFileCount = 3;
    private Server server;
    private boolean rpc=true;

    public AddFile(final Server server, final ArrayList<String> totalFilesList, final ArrayList<String> nodeFileList, boolean rpc) {
        this.totalFilesList = totalFilesList;
        this.nodeFileList = nodeFileList;
        this.server = server;
        this.rpc = rpc;

        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocation(500,100);
        //setLocationRelativeTo(null);
        setSize(500, 500);
        //pack();
        setTitle("Client : Add New Files");

        if(nodeFileList.size()<1){
            textPane1.setText("No files are added yet..");
        }else{
            displayList(nodeFileList);
        }

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newFile = textField1.getText();
                nodeFileList.add(newFile);
                totalFilesList.add(newFile);
                writeList(newFile);
                displayList(nodeFileList);
            }
        });

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                RegWindow regWindow = new RegWindow(server, AddFile.this.rpc);
                regWindow.setLocation(x(), y());
                regWindow.setVisible(true);
            }
        });
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
    }
    private int x(){
        return getX();
    }
    private int y(){
        return getY();
    }

    private void displayList(ArrayList<String> fileList){
        String list = "";
        for(String s :fileList){
            list += s +"\n";
        }
        textPane1.setText(list);
    }


    private void writeList(String newFile){
        try{
            PrintWriter out1 = new PrintWriter(new BufferedWriter(new FileWriter(RESOURCE_FILE_PATH, true)));
            out1.println(newFile);
            out1.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void close(){
        System.exit(0);
    }
}
