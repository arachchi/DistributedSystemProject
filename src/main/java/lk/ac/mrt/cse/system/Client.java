package lk.ac.mrt.cse.system;

import lk.ac.mrt.cse.system.model.Connection;

import java.util.Observer;

/**
 * @author nuran
 * @version 1.0.
 * @since 2/6/16
 */
public interface Client{
    public void init();
    public void connectToNode(Connection con);
    public String search(String keyword);
    public void addObserver(Observer observer);
    public String getConsoleMsg();
    public boolean registerToServer();
    public boolean unRegisterToServer();
}
