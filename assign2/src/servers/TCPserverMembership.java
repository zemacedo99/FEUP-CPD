package servers;
import java.io.FileWriter;
import java.io.IOException;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import main.Store;
import membershipService.Message;
import membershipService.ReceiveObjectsTcp;
import storageservice.StorageService;
import storageservice.utils;
import java.net.SocketTimeoutException;
import java.sql.Time;

// Receives the messages
public abstract class TCPserverMembership implements Runnable {

    protected final ServerSocket serverSocket;
    protected final ExecutorService pool;
    protected Store node;
    static public int tries;  
    List<Message> listOfObjects = new ArrayList<>();
    protected Socket socket;
    StorageService storageService;
    protected final Integer socketTimeout;


    //construtor que usa nova porta tcp para o membership
    public TCPserverMembership(Store node, int poolSize, int tcpMembershipPort, Integer socketTimeOut) throws IOException {
        this.node = node;
        this.node.membershipTcpPort = tcpMembershipPort;
        this.serverSocket = new ServerSocket(this.node.membershipTcpPort);
        serverSocket.setSoTimeout(socketTimeOut);
        this.pool = Executors.newFixedThreadPool(poolSize);
        this.socketTimeout =socketTimeOut;
        
        
    }

    

    public int  readMessages(String checkOperation) throws ClassNotFoundException, IOException {
        String operation = ""; 
        int numberOfOperations = 0; 
        List<Message> message = new ArrayList<>(); 
       // socket = serverSocket.accept();
        for(int i = 0 ; i < 3; i++) {
            // TODO: timer or alarm: se passarem mais de 3 segundos cancela thread. Chama uma função que faz return. 
           
            
            //evita ficar preso no socket demasiado tempo quando já não tem mensagens para receber !
            try{
                
                socket = serverSocket.accept();
               
            
                
            }catch (SocketTimeoutException sTE){
                System.out.println("Socket dont have Messages right now !");
               // sTE.printStackTrace();
                continue;// continua o cliclo for


            }
            // Receive message
            // Como ter a certeza que as mensagens recebidas estão "up-to-date ?"
            ReceiveObjectsTcp operationMembershipInfo = new ReceiveObjectsTcp(node ,this.socket, this.serverSocket );

            message = operationMembershipInfo.receivedObjects();

            if (message.size() != 0){
                System.out.println("Message size " + message.size());
                operation = message.get(message.size()-1).getMsgToSend();   //joinMS
                listOfObjects = message;
                System.out.println("operation " + operation);
                if (operation.equals(checkOperation)){
                    numberOfOperations ++; 
                    System.out.println("NumberOfOp_increment " +checkOperation +": " + numberOfOperations);
                }
            }
            
        }

        return numberOfOperations; 
    }

    protected void saveMsgReceived(String operation) throws IOException {
        int splitLogClusterInfoCounter = Integer.parseInt(listOfObjects.get(listOfObjects.size()- 2).getMsgToSend());
        //guardar eventLogs no novo nó

        for(int i = 0; i < splitLogClusterInfoCounter;i ++){
            String event = listOfObjects.get(i).getMsgToSend();
            String saveEvent[] = event.split(" ",0);
            node.eventsLog.put(saveEvent[0], Integer.parseInt(saveEvent[1]));
            System.out.println(event);
        }

        for (int i = splitLogClusterInfoCounter; i < listOfObjects.size() - 2; i++){
            String clusterNodeInfo = listOfObjects.get(i).getMsgToSend();

            String nodeInfo[] = clusterNodeInfo.split(" ",0);
            node.nodesInCluster.put(nodeInfo[0], (nodeInfo[1]));

            System.out.println(clusterNodeInfo);
            

        }


        //salvar info dos event_logs e cluster_nodes em ficheiros
        membershipInfoSaveFile(node, operation);
        

    }

    //Ver como escrever por baixo nos ficheiros,Não se pretede reescrever sempre que abrimosos ficheiros
    public void membershipInfoSaveFile(Store node, String operation) throws IOException {
        String PATH = System.getProperty("user.dir") + "/nodesFolders/" + node.nodeIpAddr;
        FileWriter myWriter = new FileWriter(PATH+"/"+ "eventsLog"+".txt");

        Iterator<String> it =node.eventsLog.keySet().iterator(); 
        while(it.hasNext()){
            String id_node = (String)it.next();
            int counterMb = node.eventsLog.get(id_node);
            myWriter.write(id_node + " " + counterMb);
            
            
            System.out.println(id_node + " " + counterMb );
            myWriter.write("\n");

           
        }
        
        myWriter.close();
        
        myWriter = new FileWriter(PATH+"/"+ "nodesInCluster"+".txt");

        //save ClusterMembers after eventlogs
        Iterator<String> iter =node.nodesInCluster.keySet().iterator(); 
        while(iter.hasNext()){
            String hashIdNode = (String)iter.next();
            String idNode = node.nodesInCluster.get(hashIdNode);
            myWriter.write(hashIdNode + " " + idNode);
            myWriter.write("\n");
            System.out.println(hashIdNode + " " + idNode );
           
           
        }

        myWriter.close();
        

        InetAddress multicastGroupAdrr = InetAddress.getByName(node.ip_mcast_addr);
        
        if(operation.equals("joinMS")){
            node.multicastSocket.joinGroup(multicastGroupAdrr);
            //TODO: integração do Storage Service com o Membership Service join Falar com o zé !
            System.out.println("ASK FOR FILES");
            storageService = new StorageService(node);
            storageService.askForFiles();
        }
        else{
            node.multicastSocket.joinGroup(multicastGroupAdrr);
            node.multicastSocket.leaveGroup(multicastGroupAdrr);
            //TODO: integração do Storage Service com o Membership Service leave
            storageService = new StorageService(node);
            storageService.sendFilesBeforeLeaving();
        }

        System.out.println("Server "+ node.nodeId + " " + operation);
        
    }

}

