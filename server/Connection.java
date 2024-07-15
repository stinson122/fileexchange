import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.stream.Collectors;

public class Connection extends Thread {

    private Socket s;
    int i = 0;

    public Connection(Socket s) {
        this.s = s;
    }

    @Override
    public void run() {
        try {
            String msg;
            DataInputStream reader = new DataInputStream(s.getInputStream());
            DataOutputStream writer = new DataOutputStream(s.getOutputStream());

            while (!(msg = reader.readUTF()).equals("END")) {
                String[] tempString = msg.split(" "); //parse string given to command and file

                try {
                    String command = tempString[0];
                    String parameter = tempString[1];
                    // System.out.println(command); uncomment for debugging, prints received command and filepath
                    // System.out.println(filepath);

                    if (command.equals("/store")) {

                        System.out.println("FILENAME: " + parameter);

                        DataOutputStream dos = new DataOutputStream(new FileOutputStream(parameter));
                        
		                byte[] filebyte = new byte[2048];
                        int file = reader.read(filebyte, 0, filebyte.length);
                        dos.write(filebyte, 0, file);
                    

                        writer.writeUTF("File '" + parameter + "' stored successfully");
                        dos.close();
                    } else if (command.equals("/get")) {
                        File tempFile = new File(parameter);
                        if (tempFile.isFile()) {
                            DataInputStream dis = new DataInputStream(new FileInputStream(parameter));
                            
                            byte[] filebyte = new byte[2048];
                            int file = dis.read(filebyte, 0, filebyte.length);
                            writer.write(filebyte, 0, file);                                
    
                            System.out.println(reader.readUTF());
                            dis.close();
                        } else {
                            System.out.println("File does not exist in storage");
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException e) { // if a command without parameters is given, i.e. /dir and /?, this exception occurs at line 29 when trying to get a parameter, so the no parameter commands go here
                    String command = tempString[0];

                    if (command.equals("/dir")) {
                        /*
                        if (Server.nameStorage[0] == null) {
                            writer.writeUTF("No files found in directory");
                        } else {
                            String directory = Stream.of(Server.nameStorage).filter(s -> s != null && !s.isEmpty()).collect(Collectors.joining("\n"));
                            writer.writeUTF(directory);
                        }*/
                    } else if (command.equals("/?")) {
                        String commands = "/join <server_ip_address> <port> \n/leave \n/register <handle> \n/store <filename> \n/dir \n/get <filename>";
                        writer.writeUTF(commands);
                    }
                }                
            }

            s.close();
            
        } catch (Exception e) {
            // e.printStackTrace(); uncomment for debugging
        } finally {
            System.out.println("Server: Client " + s.getRemoteSocketAddress() + " has disconnected");
        }
    }
}