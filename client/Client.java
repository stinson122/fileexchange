import java.io.*;
import java.net.*;
import java.time.*;
import java.time.format.*;
import java.util.*; // For Scanner

public class Client {

    public static void main(String[] args) {
        String host = "";
        int port = 0;
        String tempString[] = null;
        String command = "";
        Socket endpoint = null;
        String workingDir = System.getProperty("user.dir");

        int byteCapacity = 24000;
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
                System.out.println("/join <server_ip_address> <port> \n/leave");
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
                            File tempFile = new File(workingDir+"/files",tempString[1]);
                            if (tempFile.isFile()) {
                                msg = command + " " + (tempString[1]);
                                String timeStamp = ZonedDateTime.now(ZoneId.of( "Asia/Shanghai")).format(DateTimeFormatter.ofPattern( "uuuu-MM-dd.HH.mm.ss"));
                                msg += " " + timeStamp;
                                writer.writeUTF(msg);

                                DataInputStream dis = new DataInputStream(new FileInputStream(tempFile));
                                
                                byte[] filebyte = new byte[byteCapacity];
                                int file = dis.read(filebyte, 0, filebyte.length);
                                writer.write(filebyte, 0, file);                                
		
                                System.out.println(reader.readUTF());
                                dis.close();
                            } else {
                                System.out.println("Invalid File");
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                           System.out.println("Please enter file path of file");
                        }

                    } else if (command.equals("/get")) {
                        try {                      
                            msg = command + " " + tempString[1];
                            writer.writeUTF(msg);

                            String reply = reader.readUTF();
                            if (reply.equals("File received")) {
                                System.out.println("FILENAME: " + tempString[1]);
                                File tempFile = new File(workingDir+"/files",tempString[1]);
                                DataOutputStream dos = new DataOutputStream(new FileOutputStream(tempFile));

                                byte[] filebyte = new byte[byteCapacity];
                                int file = reader.read(filebyte, 0, filebyte.length);
                                dos.write(filebyte, 0, file);     
                                writer.writeUTF("File '" + tempString[1] + "' sent successfully"); 

                                dos.close();
                            }             
                            System.out.println(reply);
                        } catch (ArrayIndexOutOfBoundsException e)  {
                            System.out.println("Please enter file path of file");
                        }
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
            } catch (SocketException e) {
                System.out.println("Server has stopped");
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            System.out.println("Client: has terminated connection");
        }
        
    }

}