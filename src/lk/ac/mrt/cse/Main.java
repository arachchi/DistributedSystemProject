package lk.ac.mrt.cse;

public class Main {

    public static void main(String args[]){
        Node node = new Node();
        Node client = new Node();


    try{

//        node.server();
        client.client();

    }catch(Exception exception){
        exception.printStackTrace();
    }
    }

}
