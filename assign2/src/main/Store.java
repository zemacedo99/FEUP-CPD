package main;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.SortedMap;
import java.util.TreeMap;

import membershipService.MembershipOperation;
import servers.MulticastServerMT;
import java.net.InetAddress;
import java.net.MulticastSocket;

//import servers.TCPserver;
import servers.TCPserverMT;
import storageservice.utils;

public class Store {

    public String ip_mcast_addr;//Class D IP addresses are in the range 224.0.0.0 to 239.255.255.255, inclusive. The address 224.0.0.0 is reserved and should not be used.
    public int ip_mcast_port;
    public String nodeIpAddr;
    public int storePort;
    public String nodeId;
    public int membershipTcpPort;
    public SortedMap<String,Integer> eventsLog = Collections.synchronizedSortedMap(new TreeMap<String,Integer>());
    public Hashtable<String, String> hashTable;
    public TreeMap<String, String> nodesInCluster;
    public int counterMS;
    public MulticastSocket multicastSocket;
    public String nodeIpPort;


    public Store(String ip_mcast_addr,int ip_mcast_port,String nodeIpAddr,int storePort) {
        this.ip_mcast_addr = ip_mcast_addr;
        this.ip_mcast_port = ip_mcast_port;
        this.nodeIpAddr = nodeIpAddr;
        this.storePort = storePort;
        this.nodeId = nodeIpAddr+":"+storePort;
        this.hashTable = new Hashtable<String, String>();
        this.nodesInCluster = new TreeMap<String, String>();
        this.counterMS = 0;
        
        this.nodesInCluster.put(utils.sha_256(nodeId), nodeId);
        // this.nodesInCluster.put("03f6d7ba09b0531a178059659f12e65ab6a75adddf2f548b1f37624d55d95fba", "127.0.0.1:8000");
        // this.nodesInCluster.put("e6a1f3d4c716f9f8d0d756fb418d8c19e13a1b8ff5cfdf8705fe853ba5cc67c2", "127.0.0.2:8000");
        // this.nodesInCluster.put("c358736b439b68432e5e0d7612797948c1c1235ef457f8d98c99d411b2cafc40", "127.0.0.3:8000");
        // this.nodesInCluster.put("a094e9709e76e41680e191497006a52af2470fac9ab12e80a4ff6447d55d846b", "127.0.0.4:8000");

        try {
            this.multicastSocket = new MulticastSocket(this.ip_mcast_port);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

  
    /**
     * Increment join/leave counter and checks if is the first join
     * 
     * 
     * 
     *  **/
    public  int processCounter(String operation){
        if( this.counterMS == 0 && operation.equals("joinMS")){
            return this.counterMS;
        }
        else if(this.counterMS % 2 == 0 && operation.equals("leaveMS")){
            counterMS++;
            return this.counterMS;
        }else if(this.counterMS % 2 != 0 && operation.equals("joinMS")){
            counterMS++;
            return this.counterMS;
        }

        return this.counterMS;
    }

    public static void main(String[] args) {
        if (args.length < 1 || args.length > 4) {
            System.out.println("Usage:\n java Store <IP_mcast_addr> <IP_mcast_port> <node_id> <Store_port>\n");
            return;
        }
        
        String ip_mcast_addr = args[0];
        int ip_mcast_port = Integer.parseInt(args[1]);
        String nodeIpAddr = args[2];
        int storePort = Integer.parseInt(args[3]);

        Store node = new Store(ip_mcast_addr,ip_mcast_port,nodeIpAddr,storePort);
 
        createNodeFolder(nodeIpAddr);

        /*
        TCPserver tcpServer = new TCPserver(storePort,hashTable);
        tcpServer.tcpSocket();
        hashTable = tcpServer.getHashTable();
        */

        //Open TCP server with Multithread
        try {
            TCPserverMT tcpServerMT = new TCPserverMT(node, 10);
            Thread thread = new Thread(tcpServerMT);
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        //Open UDP/multicast server with Multithread
        try {
            System.out.println("MulticastServer");
            MulticastServerMT multicastServerMT = new MulticastServerMT(node,10,node.multicastSocket);
            Thread thread = new Thread(multicastServerMT);
            thread.start();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
                   

        
        System.out.println("Main Thread");
        
    }

    private static void createNodeFolder(String nodeIpAddr) {
        String PATH = System.getProperty("user.dir") + "/nodesFolders/" + nodeIpAddr;
        
        // Create a directory; all non-existent ancestor directories are automatically created
        new File(PATH).mkdirs();
    }

    public void membership(String string, MulticastSocket multicastSocket) throws IOException, InterruptedException {
        
        
        
        switch (string)
        {
            case "join":
                
                try { 
                    
                    InetAddress multicastGroupAdrr = InetAddress.getByName(this.ip_mcast_addr);
                    


                    MembershipOperation membershipJoin = new  MembershipOperation(this,multicastGroupAdrr);
                    
                    membershipJoin.sendJoin(multicastSocket); 
                     
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

              
                break;

            case "leave":
                try { 
                        
                    InetAddress multicastGroupAdrr = InetAddress.getByName(this.ip_mcast_addr);
                    


                    MembershipOperation membershipLeave = new  MembershipOperation(this,multicastGroupAdrr);
                    
                    membershipLeave.sendLeave(multicastSocket); 
                    
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

              

                break;

                
            
            

            default:
                break;
        }
    }


}