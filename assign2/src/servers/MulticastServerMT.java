package servers;
import main.Store;
import membershipService.Message;
import membershipService.SendObjectsTcp;
import storageservice.utils;

import java.net.MulticastSocket;


import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



import java.io.IOException;
import java.net.DatagramPacket;

import java.net.InetAddress;

public class MulticastServerMT implements Runnable{

    //private int storePort; preciso disto?Ou só depois para TCP ?
    public MulticastSocket multicastSocket;
    public final ExecutorService pool;
    private Store node;
    public InetAddress multicastGroupAdrr ; 


    public MulticastServerMT(Store node, int poolSize, MulticastSocket multicastSocket ) throws IOException{
        //We bound a server(node) to connect a multicast group
        this.node = node;
        InetAddress multicastGroupAdrr = InetAddress.getByName(node.ip_mcast_addr);
        this. multicastGroupAdrr = multicastGroupAdrr;
        this.multicastSocket = multicastSocket;
       

        pool = Executors.newFixedThreadPool(poolSize);
        
    }

   
    @Override
    public void run() {
        try {
            
            System.out.println("\nServer " + node.nodeId + " is connected on multicastGroupPort:  " + node.ip_mcast_port);
            pool.execute(new MulticastHandler( node, this.multicastSocket, this.multicastGroupAdrr));
            
        } catch (Exception e) {
            e.printStackTrace();
            pool.shutdown();
        }
    }

}

class MulticastHandler implements Runnable{
    Store node;
    private final MulticastSocket multicastSocket;
    InetAddress multicastGroupAdrr;

    MulticastHandler(Store node, MulticastSocket multicastSocket,InetAddress multicastGroupAdrr) throws IOException{
        this.node = node;
        this.multicastSocket = multicastSocket;
        this.multicastGroupAdrr = multicastGroupAdrr;
    }


    @Override
    public void run() {
        // TODO Auto-generated method stub
        // read and service request on socket
        try{
            while (true) {
                String threadName = Thread.currentThread().getName();
                System.out.println("\nServer " + node.nodeId + " is connected on multicastGroupPort:  " + node.ip_mcast_port + " waiting for join or leave messages !");
                
                System.out.println("MulticastThread Name: " + threadName);
                // Aqui estou a ouvir se recebo um Join ou Leave com a info de um nó que quer join/leave no Cluster
                System.out.println("waiting for join or leave messages");
                byte[] buf = new byte[256];
                // ler a mensagem do socket que deve ter join id_node counter TCPPortNova
                DatagramPacket pack= new DatagramPacket(buf, buf.length); 
                
                this.multicastSocket.receive(pack); // lê info para operação pelo socket
                String received = new String(pack.getData(),0, pack.getLength());
                System.out.println("Server: " +node.nodeId + " received msg: " + received   );
                
                String messageArgs[] = received.split(" ",0);
                String operation = messageArgs[0];
                int tcpPortToSendMessages = Integer.parseInt(messageArgs[3]); //vem da mensagem enviada
                for (String s: messageArgs){
                    System.out.println(s);
                }   
                switch(operation){
                    case "join":
                        // send info from events and cluster members to the node that wants to join
                        try {


                            // Criar flag de pedidos de nós, de forma a só enviar uma vez a informação pedida .
                            System.out.println("Sending membershipMessage to node who wants to join");
                            node.eventsLog.put(messageArgs[1],Integer.parseInt( messageArgs[2]));
                           
                            
                            
                            String nodeIdPortReceiver = messageArgs[1];
                            //marcar nó que já enviou com flag que indica o para onde enviou, de forma a não enviar de novo sem necessicade para o mesmo nó que quer fazer join
                            sendMemberShipMsgInfo(tcpPortToSendMessages, nodeIdPortReceiver, node, "joinMS");
        
        
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        break;


                    case "leave":
                    try {


                        // Criar flag de pedidos de nós, de forma a só enviar uma vez a informação pedida .
                        System.out.println("Sending membershipMessage to node who wants to join");
                        node.eventsLog.put(messageArgs[1],Integer.parseInt( messageArgs[2]));
                       
                        
                        
                        String nodeIdPortReceiver = messageArgs[1];
                        //marcar nó que já enviou com flag que indica o para onde enviou, de forma a não enviar de novo sem necessicade para o mesmo nó que quer fazer join
                        sendMemberShipMsgInfo(tcpPortToSendMessages, nodeIdPortReceiver, node, "leaveMS");
    
    
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                        break;
                    // more operation received via multicast if nedeed !    

                    default:
                    // other code if nedeed
                }

                

            }   
        }catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();   
        }

    }   


    private void sendMemberShipMsgInfo(int tcpPortToSendMessages,String nodeIdPortReceiver, Store node,String memberShipFlagMsg) throws InterruptedException, UnknownHostException, IOException {
        System.out.println("sendMemberShipMsgInfo");
        Random randomTime = new Random();
        //Esperar um x nº aleatório de segundos entre 1 seg e 2 seg para enviar resposta via TCP ao nó que quer fazer join
        
        int time = 1000 + randomTime.nextInt(2001 - 1000);
        Thread.sleep(time);

        
        List<Message> memberShipInfo = new ArrayList<>();
        System.out.println("Sending objects to Server "+ node.nodeIpAddr  + " with membershipTcpPort: " + " " + tcpPortToSendMessages);
        SendObjectsTcp sendObjectsTcp = new SendObjectsTcp(node.nodeIpAddr,tcpPortToSendMessages , node);
        System.out.println("Sending objects to Server "+ nodeIdPortReceiver  + " with membershipTcpPort: " + " " + tcpPortToSendMessages);
        //save eventLogs in a List of Message
        int counterAux = 0; // para saber situar na lista a informação sobre event logs e clusterNodes
        

        //diferenciar join de leave na resposta dos nós do cluster !
        if(memberShipFlagMsg.equals("joinMS")){
            node.eventsLog.put(nodeIdPortReceiver, node.counterMS);
            node.nodesInCluster.put(utils.sha_256(nodeIdPortReceiver), nodeIdPortReceiver);
        }else{
            node.eventsLog.put(nodeIdPortReceiver, node.counterMS);
            node.nodesInCluster.remove(utils.sha_256(node.nodeId));


        }
        getMembershipInfo(tcpPortToSendMessages, nodeIdPortReceiver, node, memberShipFlagMsg, memberShipInfo, counterAux);
        
        
        
        
        sendObjectsTcp.sendObjects(memberShipInfo);
        System.out.println("Already sended ");
    }


    private void getMembershipInfo(int tcpPortToSendMessages, String nodeIdPortReceiver, Store node, String memberShipFlagMsg,
        List<Message> memberShipInfo, int counterAux) {
        Iterator<String> it =node.eventsLog.keySet().iterator(); 
        while(it.hasNext()){
            String id_node = (String)it.next();
            int counterMb = node.eventsLog.get(id_node);
            System.out.println(id_node + " " + counterMb );

            Message mbEventLogInfo = new Message (id_node + " " + counterMb);

            memberShipInfo.add(mbEventLogInfo);
            counterAux++;
        }
        System.out.println("Sending objects to Server "+ nodeIdPortReceiver  + " with membershipTcpPort: " + " " + tcpPortToSendMessages);
        
        //save ClusterMembers after eventlogs
        Iterator<String> iter =node.nodesInCluster.keySet().iterator(); 
        while(iter.hasNext()){
            String hashNodeIpAddr = (String)iter.next();
            String nodeIpAdrr = node.nodesInCluster.get(hashNodeIpAddr);
            System.out.println(hashNodeIpAddr  + " " + nodeIpAdrr );
            Message mbEventLogInfo = new Message (hashNodeIpAddr +  " " + nodeIpAdrr );

            memberShipInfo.add(mbEventLogInfo);
            
        }
        
        memberShipInfo.add(new Message(""+counterAux));
        memberShipInfo.add(new Message(memberShipFlagMsg));//flag so who receives know what to do join or leave
    }

    


    



}
