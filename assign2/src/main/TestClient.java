package main;
import java.net.*;
import java.util.Scanner;
import java.io.*;
 
public class TestClient {
 
    public static String hostname;
    public static int port;
    public static String operation;
    public static String opnd;
    public static String path = "/clientFiles/";

    public static void main(String[] args) {
        if (args.length < 3 || args.length > 4) {
            System.out.println("Usage:\n java TestClient <IP address> <port number> <operation> [<opnd>]\n");
            return;
        }

 
        hostname = args[0];
        port = Integer.parseInt(args[1]);
        operation = args[2];

 
        try (Socket socket = new Socket(hostname, port)) {
 
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            switch (operation) {
                case "join":      
                    writer.println("join".toString());
                    break;

                case "leave":     
                    writer.println("leave".toString());
                    break;

                case "put":     
                    opnd = args[3]; //file pathname of the file with the value to add
                    String fileContent = readFile(opnd);
                    writer.println("put "+ fileContent);
                    break;

                case "get":     
                    opnd = args[3]; //string of hexadecimal symbols encoding the sha-256 key returned by put
                    writer.println("get " + opnd);
                    break;

                case "delete":      
                    opnd = args[3]; //string of hexadecimal symbols encoding the sha-256 key returned by put
                    writer.println("delete " + opnd);
                    break;
                    
                default:            
                    System.out.println("Operation not recognized");
                    break;
            }

            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
 
            String server_response = reader.readLine();
 
            System.out.println(server_response);
 
 
        } catch (UnknownHostException ex) {
 
            System.out.println("Server not found: " + ex.getMessage());
 
        } catch (IOException ex) {
 
            System.out.println("I/O error: " + ex.getMessage());
        }
    }

    static String readFile(String fileName){
        String data = "";
        //System.out.println("Working Directory = " + System.getProperty("user.dir"));
        try {
            File myObj = new File(System.getProperty("user.dir")+path+fileName);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
              data = myReader.nextLine();
            }
            myReader.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        return data;
    }
}