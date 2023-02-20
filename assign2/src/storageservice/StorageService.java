package storageservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Set;

import main.Store;

public class StorageService {

    Store node;

    public StorageService(Store node) {
        this.node = node;
    }

    public String put(String value) {
        String keyAfterSHA = utils.sha_256(value);
        String nodeCloserId = consistentHashing(keyAfterSHA,node);
        String keyClientNeed = "No node available";
        nodeCloserId = checkNodeWorking(nodeCloserId);
        if(nodeCloserId == null)
        {
            return keyClientNeed;
        }
        String[] parts = nodeCloserId.split(":");
        String nodeCloserIp = parts[0]; 
        int nodeCloserPort = Integer.valueOf(parts[1]); 

        if(nodeCloserIp.equals(node.nodeIpAddr))
        {
            this.put(keyAfterSHA, value);
            keyClientNeed = keyAfterSHA;
            // System.out.println(keyClientNeed);
        }
        else
        {
            try (Socket socket = new Socket(nodeCloserIp, nodeCloserPort)) {
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                writer.println("put "+ value);
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                keyClientNeed = reader.readLine();
                // System.out.println(keyClientNeed);
            } catch (UnknownHostException ex) {
                System.out.println("Server with node ip "+ nodeCloserIp + " not found: " + ex.getMessage());
            } catch (IOException ex) {
                System.out.println("I/O error: " + ex.getMessage());
            }
        }

        return keyClientNeed;


    }

    public void put(String key,String value) {
        node.hashTable.put(key, value);
        String PATH = System.getProperty("user.dir") + "/nodesFolders/" + node.nodeIpAddr;
        try {
            FileWriter myWriter = new FileWriter(PATH+"/"+key+".txt");
            myWriter.write(value);
            myWriter.close();
          } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public String get(String key) {
        String nodeCloserId = consistentHashing(key,node);
        String file_value = "No node available";
        nodeCloserId = checkNodeWorking(nodeCloserId);
        if(nodeCloserId == null)
        {
            return file_value;
        }
        String[] parts = nodeCloserId.split(":");
        String nodeCloserIp = parts[0]; 
        int nodeCloserPort = Integer.valueOf(parts[1]); 

        if(nodeCloserIp.equals(node.nodeIpAddr))
        {
            file_value = node.hashTable.get(key);
        }
        else
        {
            try (Socket socket = new Socket(nodeCloserIp, nodeCloserPort)) {
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                writer.println("get "+ key);
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                file_value = reader.readLine();
            } catch (UnknownHostException ex) {
     
                System.out.println("Server not found: " + ex.getMessage());
     
            } catch (IOException ex) {
     
                System.out.println("I/O error: " + ex.getMessage());
            }
        }

        return file_value;
    }
    
    // improvement: use of "tombstones" for deletion
    public String delete(String key) {
        String nodeCloserId = consistentHashing(key,node);
        String delete_status = "No node available";
        nodeCloserId = checkNodeWorking(nodeCloserId);
        if(nodeCloserId == null)
        {
            return delete_status;
        }
        String[] parts = nodeCloserId.split(":");
        String nodeCloserIp = parts[0]; 
        int nodeCloserPort = Integer.valueOf(parts[1]); 

        
        if(nodeCloserIp.equals(node.nodeIpAddr))
        {
            node.hashTable.remove(key);
            String PATH = System.getProperty("user.dir") + "/nodesFolders/" + node.nodeIpAddr;
            File myObj = new File(PATH+"/"+key+".txt"); 
            if (myObj.delete()) { 
                System.out.println("Deleted the file: " + myObj.getName());
            } else {
                System.out.println("Failed to delete the file.");
            } 
            delete_status = "Delete Successful";
        }
        else
        {
            try (Socket socket = new Socket(nodeCloserIp, nodeCloserPort)) {
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                writer.println("delete "+ key);
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                delete_status = reader.readLine();
                System.out.println(delete_status);
            } catch (UnknownHostException ex) {
                System.out.println("Server not found: " + ex.getMessage());
            } catch (IOException ex) {
                System.out.println("I/O error: " + ex.getMessage());
            }
        }

        return delete_status;
        
    }

    public String consistentHashing(String hashCode, Store node) {

        String target = hashCode;

        //Processing when not included
        if (!node.nodesInCluster.containsKey(hashCode)) {
            target = node.nodesInCluster.ceilingKey(hashCode);
            if (target == null && !node.nodesInCluster.isEmpty()) {
                target = node.nodesInCluster.firstKey();
            }
        }

        return node.nodesInCluster.get(target);
          
    }

    public String checkNodeWorking(String nodeCloserId){
        String nextNode = null;

        while(true)
        {
            String[] parts = nodeCloserId.split(":");
            String nodeCloserIp = parts[0]; 
            int nodeCloserPort = Integer.valueOf(parts[1]); 
            System.out.println("Trying node: " + nodeCloserId);

            try (Socket socket = new Socket(nodeCloserIp, nodeCloserPort)) {
                socket.close();
                nextNode = nodeCloserId;
                return nextNode;
                
            } catch (UnknownHostException ex) {
     
                System.out.println("Server not found: " + ex.getMessage());
     
            } catch (IOException ex) {
     
                System.out.println(ex.getMessage() + " with node ip "+ nodeCloserIp);
                
                String target = node.nodesInCluster.higherKey(utils.sha_256(nodeCloserId));
                if(target == null)
                {
                    break;
                }
                nodeCloserId = node.nodesInCluster.get(target);
                
            }

        }

        return nextNode;
    }

    public void askForFiles(){
        
        String nodeAhead = this.node.nodesInCluster.higherKey(utils.sha_256(this.node.nodeId));
                if(nodeAhead == null)
                {
                    return;
                }
                nodeAhead = node.nodesInCluster.get(nodeAhead);

        
        System.out.println("NodeAhead" + nodeAhead);
        
        
        if(nodeAhead==null){
            return;
        }
        nodeAhead = checkNodeWorking(nodeAhead);

        String[] parts = nodeAhead.split(":");
        String nodeCloserIp = parts[0]; 
        int nodeCloserPort = Integer.valueOf(parts[1]); 

        try (Socket socket = new Socket(nodeCloserIp, nodeCloserPort)) {
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            writer.println("join_event "+ this.node.nodeId);

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }

    public void joinEvent(String joiningNodeId){
        
        String nodeId = this.node.nodeId;
        Set<String> keys = this.node.hashTable.keySet();
        for(String key: keys){
            System.out.println("Value of "+key+" is: "+ this.node.hashTable.get(key));
            nodeId = consistentHashing(key, this.node);

            if(!nodeId.equals(this.node.nodeId))
            {
                String[] parts = nodeId.split(":");
                String nodeCloserIp = parts[0]; 
                int nodeCloserPort = Integer.valueOf(parts[1]); 

                try (Socket socket = new Socket(nodeCloserIp, nodeCloserPort)) {
                    OutputStream output = socket.getOutputStream();
                    PrintWriter writer = new PrintWriter(output, true);
                    writer.println("put "+ this.node.hashTable.get(key));

                } catch (UnknownHostException ex) {
                    System.out.println("Server with node ip "+ nodeId + " not found: " + ex.getMessage());
                } catch (IOException ex) {
                    System.out.println("I/O error: " + ex.getMessage());
                }
            }
        }
    }

    public void sendFilesBeforeLeaving(){
        
        String nodeAhead = this.node.nodesInCluster.higherKey(utils.sha_256(this.node.nodeId));
        if(nodeAhead == null)
        {
            return;
        }
        nodeAhead = node.nodesInCluster.get(nodeAhead);


        System.out.println("NodeAhead" + nodeAhead);


        if(nodeAhead==null){
            return;
        }
        nodeAhead = checkNodeWorking(nodeAhead);
        
        String[] parts = nodeAhead.split(":");
        String nodeAheadIp = parts[0]; 
        int nodeAheadPort = Integer.valueOf(parts[1]); 


        Set<String> keys = this.node.hashTable.keySet();
        for(String key: keys){
            System.out.println("Value of "+key+" is: "+ this.node.hashTable.get(key));
          

            try (Socket socket = new Socket(nodeAheadIp, nodeAheadPort)) {
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                writer.println("put "+ this.node.hashTable.get(key));

            } catch (UnknownHostException ex) {
                System.out.println("Server with node ip "+ nodeAheadIp + " not found: " + ex.getMessage());
            } catch (IOException ex) {
                System.out.println("I/O error: " + ex.getMessage());
            }


            delete(key);

            
        }
    }
}
