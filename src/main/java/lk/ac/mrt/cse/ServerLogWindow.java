package main.java.lk.ac.mrt.cse;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by kulakshi on 1/20/16.
 */
public class ServerLogWindow extends OutputStream {

    private JLabel loglabel;
    JPanel panel1;
    private JTextArea textArea1;


    ServerLogWindow(){
       /* setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setTitle("Server : log"); */





    }



    @Override
    public void write(int b) throws IOException {
        // redirects data to the text area
        textArea1.append(String.valueOf((char)b));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // scrolls the text area to the end of data
        textArea1.setCaretPosition(textArea1.getDocument().getLength());
        System.out.println("hyfyghfv");
    }


}
