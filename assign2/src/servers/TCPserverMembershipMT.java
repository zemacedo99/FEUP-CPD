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

import main.Store;
import membershipService.Message;
import membershipService.ReceiveObjectsTcp;
import storageservice.StorageService;


// Receives the messages
public class TCPserverMembershipMT implements Runnable {

    private final ServerSocket serverSocket;
    private final ExecutorService pool;
    private Store node;
    static public int tries; 
    static public String operation;
    //construtor que usa nova porta tcp para o membership
    public TCPserverMembershipMT(Store node, int poolSize, int tcpMembershipPort, String operation) throws IOException {
        this.node = node;
        this.node.membershipTcpPort = tcpMembershipPort;
        this.serverSocket = new ServerSocket(this.node.membershipTcpPort);
        this.pool = Executors.newFixedThreadPool(poolSize);
        this.tries = 0;
        this.operation = operation; 
    }

    public void run() { // run the service
        String threadName = Thread.currentThread().getName();
        System.out.println("Thread Name: " + threadName);

      try {
        while(tries < 3) { 
          System.out.println("\nServer " + node.nodeId + " is listening on tcpMemberShipPort " + node.membershipTcpPort);
          pool.execute(new HandlerTCPserverMsMT(node, serverSocket.accept(),serverSocket));
        }
      } catch (IOException ex) {
        pool.shutdown();
      }
    }

}
 
class HandlerTCPserverMsMT implements Runnable 
{
    private final Socket socket;
    private final ServerSocket serverSocket;
    StorageService storageService;
    Store node;

    HandlerTCPserverMsMT(Store node,Socket socket, ServerSocket serverSocket){
        this.socket = socket;
        this.node = node;
        this.serverSocket = serverSocket;

    }

    public void run() {
        // read and service request on socket
        String threadName = Thread.currentThread().getName();
        System.out.println("Thread Name: " + threadName);
        
        int counterNumbMsgRecv = 0;
        System.out.println("Received messages: " + counterNumbMsgRecv);
        List<Message> listOfObjects = new ArrayList<>();
        String operation = "";
        int isJoinMS = 0;
        int isLeaveMS = 0;
        int splitLogClusterInfoCounter = -1;
        int joinSended = 1;
        try{
            
            for(int i = 0; i < 3; i++){
                // Receive message
                // Como ter a certeza que as mensagens recebidas estão "up-to-date ?"
                ReceiveObjectsTcp joinMembershipInfo = new ReceiveObjectsTcp(node ,this.socket, this.serverSocket );

                listOfObjects = joinMembershipInfo.receivedObjects();
                counterNumbMsgRecv++;

                operation = listOfObjects.get(listOfObjects.size() -1).getMsgToSend();   //joinMS or leaveMS
                splitLogClusterInfoCounter = Integer.parseInt(listOfObjects.get(listOfObjects.size()- 2).getMsgToSend());

                // Evitar que 3 mensagens sem ser do mesmo tipo, sejam contabilizadas
                // Temos de receber sempre: 3 Msg de JoinMS ou 3 Msg de leaveMS para que efetivamente nó entre no cluster e guarde info dos nós do cluster

                if(operation.equals("joinMS")){
                    isJoinMS++;
                }else if(operation.equals("leaveMS")){
                    isLeaveMS++;
                }else{
                    isJoinMS = 0;
                    isLeaveMS = 0;
                }
            }

             System.out.println("Received [" + listOfObjects.size() + "] messages from: " + this.socket);

            System.out.println("hash table size: " + node.hashTable.size());
            System.out.println("CounterMS " + node.counterMS);
            
            switch (operation) {
                case "leaveMS":     
                    // node.membership("leave",node.multicastSocket);   
                    System.out.println("Membership Service");

                    if( counterNumbMsgRecv  >= 1 && isLeaveMS >=1){

                    }

                break;
                
                
                case "joinMS":
                    System.out.println("Membership Service");
                    

                    //Após receber 3 mensagens destas, ou ter feito 3 tentativas de join, sem receber resposta de nós do cluster
                    // fazer no fim joinGroup do multicast ao recém nó no cluster !!!
                    // após estar no cluster, fechar o serverSocket/porta utilizada !
                    if( isJoinMS >=1){
                        // já podemos atualizar info sobre logs e cluster members do nó que pediu join
                        // podemos fechar ligação TCP e thread : como fazer isso ?
                        serverSocket.close();
                        node.membershipTcpPort = -1;

                        saveMsgReceived(listOfObjects, splitLogClusterInfoCounter);
                        try {
                            Thread.sleep(300000);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        socket.close();

                        // use splitLogClusterInfoCounter

                    }else if(joinSended < 3){
                        //enviar nova mensagem join
                        InetAddress multicastGroupAdrr = InetAddress.getByName(node.ip_mcast_addr);
                        
                        //Juntar de novo ao multicastSocket par enviar mensagens via multicast e UDP
                        node.multicastSocket.joinGroup(multicastGroupAdrr);
                        node.membership("join", node.multicastSocket);
                        joinSended++;

                    }else{




                    }

                break;
                
                default:            
                    System.out.println("Operation not recognized");
                break;
            }

        } catch (IOException | ClassNotFoundException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void saveMsgReceived(List<Message> listOfObjects, int splitLogClusterInfoCounter) throws IOException {
        //guardar eventLogs no novo nó
        for(int i = 0; i < splitLogClusterInfoCounter;i ++){
            String event= listOfObjects.get(i).getMsgToSend();

            String saveEvent[] = event.split(" ",0);
            node.eventsLog.put(saveEvent[0], Integer.parseInt(saveEvent[1]));

            System.out.println(event);

        }

        for (int i = splitLogClusterInfoCounter; i < listOfObjects.size() - 2; i++){
            String clusterNodeInfo= listOfObjects.get(i).getMsgToSend();

            String nodeInfo[] = clusterNodeInfo.split(" ",0);
            node.nodesInCluster.put(nodeInfo[0], (nodeInfo[1]));

            System.out.println(clusterNodeInfo);
            

        }


        //salvar info dos event_logs e cluster_nodes em ficheiros
        membershipInfoSaveFile(node);


    }

    //Ver como escrever por baixo nos ficheiros,Não se pretede reescrever sempre que abrimosos ficheiros
    public void membershipInfoSaveFile(Store node) throws IOException {
        String PATH = System.getProperty("user.dir") + "/nodesFolders/" + node.nodeId;
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
        

        node.multicastSocket.joinGroup(multicastGroupAdrr);

        System.out.println("Server "+ node.nodeId + " already belongs to the cluster nodes");
        
    }



}

