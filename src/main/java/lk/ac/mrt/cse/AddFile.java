package lk.ac.mrt.cse;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/**
 * Created by kulakshi on 1/19/16.
 */
public class AddFile extends JFrame{
    private JButton button1;
    JPanel panel1;
    private JTextField textField1;
    private JTextPane textPane1;
    private JButton nextButton;
    private JButton exitButton;

    private ArrayList<String> totalFilesList = new ArrayList<String>();
    private static ArrayList<String> nodeFileList = new ArrayList<String>();
    private String RESOURCE_FILE_PATH = "resources/FileNames.txt";
    private int nodeFileCount = 3;

    public void initializeFiles(){

        try{
            int min, max = countLines();

            if(max >0 ) {
                min = 1;

                //Initialize random number array
                ArrayList<Integer> randArr=new ArrayList<Integer>();
                //Get random numbers
                Random rand = new Random();
                int randomNum;

                while (randArr.size()<= nodeFileCount) {
                    randomNum = rand.nextInt((max - min) + 1) + min;
                    randArr.add(randomNum);
                }
                Collections.sort(randArr);

                int index =0;
                for(int i = 0 ; i < totalFilesList.size(); i++){

                    if(i == randArr.get(index)){
                        nodeFileList.add(totalFilesList.get(i));
                        index++;
                        if(index == nodeFileCount) break;
                    }
                }
            }
        } catch (IOException ex){
            ex.printStackTrace();
        }

    }

    public int countLines() throws IOException {
        FileInputStream fileInputStream = new FileInputStream(RESOURCE_FILE_PATH);
        InputStreamReader inStreamReaderObject = new InputStreamReader(fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(inStreamReaderObject );
        String line = bufferedReader.readLine();

        while (line != null) {
            totalFilesList.add(line);
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        return totalFilesList.size();
    }

    public AddFile() {
        initializeFiles();
        final ArrayList<String> fileList = nodeFileList;

        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        //pack();
        setTitle("Client : Add New Files");
        initializeFiles();

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

        final Node node = new Node(fileList);

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                ///private
                RegWindow regWindow = new RegWindow(node);
                regWindow.setVisible(true);

                JFrame serverLogWindow = new JFrame("ServerLogWindow");


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
