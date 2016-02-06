package lk.ac.mrt.cse.system;

import lk.ac.mrt.cse.system.Client;

import java.util.ArrayList;
import java.util.Observer;

/**
 * @author nuran
 * @version 1.0.
 * @since 2/6/16
 */
public interface Server extends Runnable {
    public void requestProcess(String query);

    public void search(String[] message);

    public String search(String message);

    public void addObserver(Observer observer);

    public void addClientObserver(Observer observer);

    public boolean unRegisterToServer();

    public void joinNetwork();

    public String getConsoleMsg();

    public String getClientConsoleMsg();

    public boolean registerToServer();

    public ArrayList<String> getFileList();

    public void setConsoleMsg(String consoleMsg);

    public Client getClient();

    public void setClient(Client client);

    public int getBS_Port();

    public void setBS_Port(int BS_Port);

    public String getBsIp();

    public void setBsIp(String bsIp);

    public String getPort();

    public void setPort(String port);

    public String getNodeIp();

    public void setNodeIp(String nodeIp);

    public String getUserName();

    public void setUserName(String userName);

    public int getSize();

}
