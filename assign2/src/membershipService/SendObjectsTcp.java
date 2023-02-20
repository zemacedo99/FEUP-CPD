package membershipService;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import main.Store;

public class SendObjectsTcp {

    Socket socket;

    
    Store nodeClient;
    public SendObjectsTcp(String nodeIp, int tcpPort,Store nodeCLient) throws UnknownHostException, IOException{
        System.out.println("On SendObjects");
        this.nodeClient = nodeCLient;
        this.socket = new Socket(nodeIp,tcpPort);
        System.out.println("Server "+ nodeCLient.nodeIpPort +" connected!" );


    }


    public void sendObjects(List<Message> objects) throws IOException{
        OutputStream outputStream = this.socket.getOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
    
        
        System.out.println("Sending messages to the ServerSocket");
    
        objectOutputStream.writeObject(objects);


        System.out.println("Closing socket ");
        socket.close();
    }



    
}



