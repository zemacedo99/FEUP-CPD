package membershipService;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import main.Store;

public class ReceiveObjectsTcp {
    public Socket socket;

    public ServerSocket serverSocket;
    Store nodeServer;
    public ReceiveObjectsTcp(Store nodeServer,Socket socket, ServerSocket serverSocket) throws UnknownHostException, IOException {
        this.nodeServer = nodeServer;
        this.socket = socket;
        this.serverSocket = serverSocket;
        System.out.println("Server "+ nodeServer.nodeIpPort +" waiting for connections on port:  "+ nodeServer.membershipTcpPort    );
    }


    public List<Message> receivedObjects() throws IOException, ClassNotFoundException{
        
        System.out.println("Connection from " + this.socket + "!");

        // get the input stream from the connected socket
        InputStream inputStream = socket.getInputStream();

         // create a DataInputStream so we can read data from it.
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

         // read the list of objects from the socket
        List<Message> listOfObjects = (List<Message>) objectInputStream.readObject();

        System.out.println("Closing sockets.");
        return listOfObjects;
    }
}
