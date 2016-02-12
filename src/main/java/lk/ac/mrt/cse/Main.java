package lk.ac.mrt.cse;

import lk.ac.mrt.cse.gui.AddFile;
import lk.ac.mrt.cse.rpc.impl.NodeServiceImpl;
import lk.ac.mrt.cse.system.Server;
import lk.ac.mrt.cse.system.imp.ServerImpl;
import lk.ac.mrt.cse.util.ConnectionTable;
import lk.ac.mrt.cse.util.Constants;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by kulakshi on 1/19/16.
 */
public class Main {
    private static ArrayList<String> totalFilesList = new ArrayList<String>();
    private static ArrayList<String> nodeFileList = new ArrayList<String>();
    private static Server server;
    private static boolean rpc=false;
    public static void main(String args[]){
        ConnectionTable routingTable=new ConnectionTable();
        initializeFiles();
        if(rpc)
            server = new NodeServiceImpl(routingTable,nodeFileList);
        else
            server = new ServerImpl(routingTable,nodeFileList);

        AddFile addFile = new AddFile(server,totalFilesList,nodeFileList,rpc);
        addFile.setVisible(true);

    }

    private static void initializeFiles(){

        try{
            int min, max = countLines();

            if(max >0 ) {
                min = 1;

                //Initialize random number array
                ArrayList<Integer> randArr=new ArrayList<Integer>();
                //Get random numbers
                Random rand = new Random();
                int randomNum;

                while (randArr.size()<= Constants.NODE_FILE_COUNT) {
                    randomNum = rand.nextInt((max - min) + 1) + min;
                    randArr.add(randomNum);
                }
                Collections.sort(randArr);

                int index =0;
                for(int i = 0 ; i < totalFilesList.size(); i++){

                    if(i == randArr.get(index)){
                        nodeFileList.add(totalFilesList.get(i));
                        index++;
                        if(index == Constants.NODE_FILE_COUNT) break;
                    }
                }
            }
        } catch (IOException ex){
            ex.printStackTrace();
        }

    }

    private static int countLines() throws IOException {
        FileInputStream fileInputStream = new FileInputStream(Constants.RESOURCE_FILE_PATH);
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
}
