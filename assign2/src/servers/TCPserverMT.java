package servers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import main.Store;
import storageservice.StorageService;
import storageservice.utils;

public class TCPserverMT implements Runnable {

    private final ServerSocket serverSocket;
    private final ExecutorService pool;
    private Store node;

 
    public TCPserverMT(Store node, int poolSize) throws IOException {
        this.node = node;
        InetAddress ip =InetAddress.getByName(node.nodeIpAddr);  

        serverSocket = new ServerSocket(node.storePort,50,ip);
        pool = Executors.newFixedThreadPool(poolSize);
    }

    //construtor que usa nova porta tcp para o membership
    public TCPserverMT(Store node, int poolSize, int tcpMembershipPort) throws IOException {
        this.node = node;

        this.node.membershipTcpPort = tcpMembershipPort;
        InetAddress ip =InetAddress.getByName(node.nodeId);  

        serverSocket = new ServerSocket(this.node.membershipTcpPort,50,ip);
        pool = Executors.newFixedThreadPool(poolSize);
    }

    public void run() { // run the service
      try {
        for (;;) {
          System.out.println("\nServer " + node.nodeIpAddr + " is listening on storePort " + node.storePort);
          pool.execute(new Handler(node, serverSocket.accept()));
        }
      } catch (IOException ex) {
        pool.shutdown();
      }
    }

}
 
class Handler implements Runnable 
{
    private final Socket socket;
    StorageService storageService;
    Store node;

    Handler(Store node,Socket socket){
        this.socket = socket;
        this.node = node;
    }

    public void run() {
        // read and service request on socket
        String threadName = Thread.currentThread().getName();
        System.out.println("Thread Name: " + threadName);

        try{
            while (true) {

                InputStream input = this.socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                
                String client_line = reader.readLine();

                if(client_line == null)
                {
                    break;
                }
                
                System.out.println("New client connected: "+ client_line);
                
                OutputStream output = this.socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                String operation = client_line;
                String opnd = "";


                if (client_line.contains(" ")) {
                    String[] parts = client_line.split(" ");
                    operation = parts[0]; 
                    opnd = parts[1]; 
                }

                System.out.println("hash table size: " + node.hashTable.size());
                System.out.println("CounterMS " + node.counterMS);

                String file_key;
                
                switch (operation) {
                    case "join":
                        writer.println("Membership Service ");
                        InetAddress multicastGroupAdrr = InetAddress.getByName(node.ip_mcast_addr);
                        try{

                            
                            node.multicastSocket.leaveGroup(multicastGroupAdrr); // tentar sair do grupoMulticast sem pertencer a ele/logo ao cluster
                        }catch(IOException e){
                        // e.printStackTrace();
                            node.membership("join",node.multicastSocket);   
                        
                            
                        return;
                        }
                        System.out.println("Server already belongs to the cluster !");
                        node.multicastSocket.joinGroup(multicastGroupAdrr);

                        
                    break;
                    
                    case "leave":
                        
                        writer.println("Membership Service ");
                        try{

                            InetAddress multicastGroupAdrrr = InetAddress.getByName(node.ip_mcast_addr);
                            node.multicastSocket.leaveGroup(multicastGroupAdrrr); // tentar sair do grupoMulticast sem pertencer a ele/logo ao cluster

                        }catch(IOException e){
                          //  e.printStackTrace();
                           System.out.println("Server does not belong to the cluster !");
                            
                           return;
                        }
                        
                        node.membership("leave",node.multicastSocket);   
                        
                        
                        
                        
                    break;
                    
                    case "put":     
                        storageService = new StorageService(node);
                        System.out.println("hash table size before put: " + node.hashTable.size());
                        file_key = storageService.put(opnd);
                        System.out.println("hash table size after put: " + node.hashTable.size());
                        writer.println(file_key);
                    break;
                    
                    case "get":     
                        storageService = new StorageService(node);
                        String file_value = storageService.get(opnd);
                        writer.println(file_value);
                    break;
                    
                    case "delete":      
                        storageService = new StorageService(node);
                        String delete_status = storageService.delete(opnd);
                        System.out.println("hash table size after delete: " + node.hashTable.size());
                        writer.println(delete_status);
                    break;

                    case "join_event":      
                        storageService = new StorageService(node);
                        storageService.joinEvent(opnd);
                    break;

                    default:            
                        System.out.println("Operation not recognized");
                    break;
                }

            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
