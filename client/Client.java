import java.io.*;
import java.net.*;
import java.util.*; // For Scanner

public class Client {

    public static void main(String[] args) {
        String host = "";
        int port = 0;
        String tempString[] = null;
        String command = "";
        Socket endpoint = null;

        Scanner sc = new Scanner(System.in);
        String msg;
        Boolean flag = false;

        while (!flag) {
            System.out.print("> ");
            msg = sc.nextLine();

            tempString = msg.split(" "); //parse string to get command
            command = tempString[0];

            if (command.equals("/join")) {
                try {
                    host = tempString[1];
                    port = Integer.parseInt(tempString[2]);
                    endpoint = new Socket(host, port);
                    flag = true;
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Please enter server details");
                } catch (ConnectException e) {
                    System.out.println("Invalid server details");
                } catch (UnknownHostException e) {
                    System.out.println("Invalid server details");
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            } else if (command.equals("/leave")) {
                break;
            } else if (command.equals("/?")) {
                System.out.println("/join <server_ip_address> <port> \n/leave \n/register <handle> \n/store <filename> \n/dir \n/get <filename>");
            } else {
                System.out.println("Invalid Command");
            }
        }

        if (flag) {
            
            try {
    
                System.out.println("Client: Has connected to server " + host + ":" + port);
    
                DataOutputStream writer = new DataOutputStream(endpoint.getOutputStream());
                DataInputStream reader = new DataInputStream(endpoint.getInputStream());
                
                while (flag) {
                    System.out.print("> ");

                    msg = sc.nextLine();

                    tempString = msg.split(" ");
                    command = tempString[0];

                    if ((command.equals("/store"))) {
                        try {
                            File tempFile = new File(tempString[1]);
                            if (tempFile.isFile()) {
                                String filePath = tempFile.getCanonicalPath();

                                msg = command + " " + filePath;

                                writer.writeUTF(msg);
                                System.out.println(reader.readUTF());
                            } else {
                                System.out.println("Invalid File");
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            System.out.println("Please enter file path of file");
                        }

                    } else if (command.equals("/get")) {
                        File tempFile = new File(tempString[1]);
                        String filePath = tempFile.getCanonicalPath();

                        msg = command + " " + filePath;

                        writer.writeUTF(msg);
                        System.out.println(reader.readUTF());

                    } else if (command.equals("/leave")) {
                        break;

                    } else if (command.equals("/dir")) {
                        msg = "/dir";
                        writer.writeUTF(msg);
                        System.out.println(reader.readUTF());

                    } else if (command.equals("/register")) { //TODO: register command
                        System.out.println("/register WIP");

                    } else if (command.equals("/?")) {
                        msg = "/?";
                        writer.writeUTF(msg);
                        System.out.println(reader.readUTF());

                    } else {
                        System.out.println("Invalid Command");
                    }
                }
    
                writer.writeUTF("END");

                sc.close();
                endpoint.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            System.out.println("Client: has terminated connection");
        }
        
    }

}