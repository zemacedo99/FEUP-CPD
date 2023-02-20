package servers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

public class TCPserver {

    private int storePort;
    private Hashtable<String, String> hashTable;



    public TCPserver(int storePort,Hashtable<String, String> hashTable){
        this.storePort =  storePort;
        this.hashTable = hashTable;
    }

    

    public void tcpSocket() {

        
        try (ServerSocket serverSocket = new ServerSocket(this.storePort)) {
 
            System.out.println("Server is listening on storePort " + this.storePort);
 
            while (true) {
                Socket socket = serverSocket.accept();
 
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
 
                String client_line = reader.readLine();

                System.out.println("New client connected: "+ client_line);

                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                String operation = client_line;
                String opnd = "";
                if (client_line.contains(" ")) {
                    String[] parts = client_line.split(" ");
                    operation = parts[0]; 
                    opnd = parts[1]; 
                } 

                switch (operation) {
                    case "join":      
                        writer.println("Membership Service");
                        break;
    
                    case "leave":     
                        writer.println("Membership Service");
                        break;
    
                    case "put":     
                        writer.println("Storage Service" + opnd);
                        break;
    
                    case "get":     
                        writer.println("Storage Service");
                        break;
    
                    case "delete":      
                        writer.println("Storage Service");
                        break;
                        
                    default:            
                        System.out.println("Operation not recognized");
                        break;
                }
            }
 
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }



    public Hashtable<String, String> getHashTable() {
        return this.hashTable;
    }
    
}
