package membershipService;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import main.Store;
import servers.TCPserverMembership;
import servers.TCPserverMembershipJoin;
import servers.TCPserverMembershipLeave;
import storageservice.utils;

public class MembershipOperation {
    Store node;
    InetAddress groupMulticastAdrr;

    public MembershipOperation(Store node, InetAddress groupMulticastAdrr){
        this.groupMulticastAdrr = groupMulticastAdrr;
        this.node = node ;
        
        
    }

    public void sendJoin(MulticastSocket multicastSocket) throws IOException, InterruptedException {
        
        //Criar thread para novo TCP, usar import java.net.ServerSocket; /criar Server Socket
        //enviar na mensagem porta para nova ligação TCP criando usando uma TCPServerThread
        //message to send, via multiCast

        //Thread

        //porta nova para membership Tcp thread/comunicação
        utils utilsFreePort = new utils();

        int freeTcpPort = utilsFreePort.getFreeTcpPort();
        
        //Thread para servidorTCP que recebe as mensagens TCP após nós do cluster receberem a mensagem join via multicast
        try {
            TCPserverMembership tcpServerMembershipJoin = new TCPserverMembershipJoin(node, 10, freeTcpPort, 300);
            Thread thread = new Thread(tcpServerMembershipJoin);
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


        //Envio de mensagem via multicast
        byte[] joinMulticastMsg = ("join " + node.nodeId + " " + node.processCounter("joinMS") + " " + freeTcpPort).getBytes(); // falta a porta TCP de onde o nó que envia mensagem com info para Join, vai receber a informação cluster nodes
         
        DatagramPacket startJoin = new DatagramPacket(joinMulticastMsg, joinMulticastMsg.length, groupMulticastAdrr, node.ip_mcast_port);

        //mensagem com join nodeId , counter e porta TCP para receber info dos clusterNodes  enviada via Multicast
        
        //Fazemos leaveGroup a quem envia mensagem de Join, de forma a que quem ainda não pertence ao cluster
        // não tem de receber mensagens de Join dele próprio !
        // no final de juntar ao Cluster, fazemos joinGroup ao novo Store que ainda tem de receber mensagens TCP com informação Stores existentes no Cluster
     //   multicastSocket.leaveGroup(this.groupMulticastAdrr);
        multicastSocket.send(startJoin);

            
        
        System.out.println( "Server: "    + this.node.nodeId + " sended msg: "+ new String(startJoin.getData(),0,startJoin.getLength() ) + " to multicast group with port: " + node.ip_mcast_port );



    }


    public void sendLeave(MulticastSocket multicastSocket) throws IOException, InterruptedException {
        
        //Criar thread para novo TCP, usar import java.net.ServerSocket; /criar Server Socket
        //enviar na mensagem porta para nova ligação TCP criando usando uma TCPServerThread
        //message to send, via multiCast

        //Thread

        //porta nova para membership Tcp thread/comunicação
        utils utilsFreePort = new utils();

        int freeTcpPort = utilsFreePort.getFreeTcpPort();
        
        //Thread para servidorTCP que recebe as mensagens TCP após nós do cluster receberem a mensagem join via multicast
        try {
            TCPserverMembership tcpServerMembershipLeave = new TCPserverMembershipLeave(node, 10, freeTcpPort, 300);
            Thread thread = new Thread(tcpServerMembershipLeave);
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


        //Envio de mensagem via multicast
        byte[] joinMulticastMsg = ("leave " + node.nodeId + " " + node.processCounter("leaveMS") + " " + freeTcpPort).getBytes(); // falta a porta TCP de onde o nó que envia mensagem com info para Join, vai receber a informação cluster nodes
         
        DatagramPacket startLeave = new DatagramPacket(joinMulticastMsg, joinMulticastMsg.length, groupMulticastAdrr, node.ip_mcast_port);

        //mensagem com join nodeId , counter e porta TCP para receber info dos clusterNodes  enviada via Multicast
        
        //Fazemos leaveGroup a quem envia mensagem de Join, de forma a que quem ainda não pertence ao cluster
        // não tem de receber mensagens de Join dele próprio !
        // no final de juntar ao Cluster, fazemos joinGroup ao novo Store que ainda tem de receber mensagens TCP com informação Stores existentes no Cluster
     //   multicastSocket.leaveGroup(this.groupMulticastAdrr);
        multicastSocket.send(startLeave);

            
        
        System.out.println( "Server: "    + this.node.nodeId + " sended msg: "+ new String(startLeave.getData(),0,startLeave.getLength() ) + " to multicast group with port: " + node.ip_mcast_port );



    }


}
