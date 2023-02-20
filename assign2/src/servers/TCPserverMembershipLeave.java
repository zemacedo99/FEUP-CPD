package servers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import main.Store;
import membershipService.Message;
import storageservice.utils;

public class TCPserverMembershipLeave extends TCPserverMembership {
    public TCPserverMembershipLeave(Store node, int poolSize, int tcpMembershipPort,Integer socketTimeOut) throws IOException{

        super(node, poolSize, tcpMembershipPort, socketTimeOut); 
        
    }

    public void run() { // run the service
        String threadName = Thread.currentThread().getName();
        System.out.println("Thread Name: " + threadName);
        //ScheduledThreadPoolExecutor scheduledpool = new ScheduledThreadPoolExecutor(10);
        
        for (int joinTries = 1; joinTries < 3; joinTries++) { // isto faz sentido ?!??!?
          System.out.println("\nServer " + node.nodeId + " is listening on storePort " + node.storePort);
            
          
       //  scheduledpool.schedule(new HandlerJoinAttempt(), 10000, TimeUnit.MILLISECONDS);

         
       

         try {
             int numberOfLeaves;
            
                 numberOfLeaves= readMessages("leaveMS"); 
                 System.out.println("NumberOfLeaves: " + numberOfLeaves);
                 
             
             if (numberOfLeaves == 3) {
                 saveMsgReceived("leaveMS");
                 socket.close();
                 System.out.println("NumberOfLeaves: " + numberOfLeaves);
                 return; 
             
         }
             
             System.out.println("SIZE: " + listOfObjects.size());
             // Enviar nova mensagem join
         
             
             // Juntar de novo ao multicastSocket par enviar mensagens via multicast e UDP
             Thread.sleep(3000); // esperar 3 segundos antes de enviar outra mensagem


             InetAddress multicastGroupAdrr = InetAddress.getByName(node.ip_mcast_addr);
                 

              //Envio de mensagem via multicast
             byte[] joinMulticastMsg = ("leave " + node.nodeId + " " + node.processCounter("leaveMS") + " " + node.membershipTcpPort).getBytes(); // falta a porta TCP de onde o nó que envia mensagem com info para Join, vai receber a informação cluster nodes
      
             DatagramPacket startLeave = new DatagramPacket(joinMulticastMsg, joinMulticastMsg.length, multicastGroupAdrr, node.ip_mcast_port);

             node.multicastSocket.send(startLeave);
             
             System.out.println("NumberOfLeaves: " + numberOfLeaves);
         

         } catch (IOException | ClassNotFoundException ex) {
             System.out.println("Server exception: " + ex.getMessage());
             ex.printStackTrace();
         } catch (InterruptedException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
        
        }
        //permite que tudo seja processado nas threads da pool !
     //  System.out.println("Tasks: " + scheduledpool.getCompletedTaskCount());
       // while(!scheduledpool.isTerminated())
        // Tentei receber 3 mensagens Join, sem sucesso : Existem apenas 0, 1 ou 2 nós no cluster ! Adiciono nó ao Cluster "por defeito"
        if (listOfObjects.size() != 0){
            try {
                //Pelo menos um nós enviou mensagem joinMS
                saveMsgReceived("leaveMS");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // nenhum nó enviou mensagem de joinMS
            System.out.println("Own Leave");
            listOfObjects = ownLeave(); 
            try {
                saveMsgReceived("leaveMS");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        
        System.out.println("Thread pool terminated !"); 
    }
        
    }

    public List<Message> ownLeave(){
        List<Message> message = new ArrayList<>();

        message.add(new Message(node.nodeId + " " + node.counterMS));
        message.add(new Message(utils.sha_256(node.nodeId)+ " " + node.nodeId));
        message.add(new Message("1"));
        message.add(new Message("leaveMS"));


        return message;


    }


    

}