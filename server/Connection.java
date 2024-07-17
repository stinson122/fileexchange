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
            String workingDir = System.getProperty("user.dir");
            DataInputStream reader = new DataInputStream(s.getInputStream());
            DataOutputStream writer = new DataOutputStream(s.getOutputStream());
            int byteCapacity = 24000;

            while (!(msg = reader.readUTF()).equals("END")) {
                String[] tempString = msg.split(" "); //parse string given to command and file

                try {
                    String command = tempString[0];
                    String parameter = tempString[1];
                    // System.out.println(command); uncomment for debugging, prints received command and filepath
                    // System.out.println(filepath);

                    if (command.equals("/store")) {
                        System.out.println("User <"+tempString[2] + "> is sending file.");
                        System.out.println("FILENAME: " + parameter);
                        File tempFile = new File(workingDir+"/files",tempString[1]);
                        DataOutputStream dos = new DataOutputStream(new FileOutputStream(tempFile));

		                byte[] filebyte = new byte[byteCapacity];
                        int file = reader.read(filebyte, 0, filebyte.length);
                        dos.write(filebyte, 0, file);
                    

                        writer.writeUTF("File '" + parameter + "' stored successfully");
                        dos.close();
                    } else if (command.equals("/get")) {
                        System.out.println("FILENAME: " + parameter);
                        File tempFile = new File(workingDir+"/files",parameter);

                        if (tempFile.isFile()) {
                            writer.writeUTF("File received");
                            DataInputStream dis = new DataInputStream(new FileInputStream(tempFile));
                            
                            byte[] filebyte = new byte[byteCapacity];
                            int file = dis.read(filebyte, 0, filebyte.length);
                            writer.write(filebyte, 0, file); 
    
                            System.out.println(reader.readUTF());
                            dis.close();
                        } else {
                            writer.writeUTF("File does not exist in storage");
                            System.out.println("File does not exist in storage");
                        }
                    } else if (command.equals("/register")){
                        String username = tempString[1];
                        if(username != null){
                            //System.out.println(username);
                            writer.writeUTF("Welcome " + username);
                            System.out.println("Welcome " + username);
                        } else if (tempString.length < 2) {
                            writer.writeUTF("Error: Unsuccessful registration, incorrect syntax.");
                            System.out.println("Error: Unsuccessful registration, incorrect syntax.");
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException e) { // if a command without parameters is given, i.e. /dir and /?, this exception occurs at line 29 when trying to get a parameter, so the no parameter commands go here
                    String command = tempString[0];

                    if (command.equals("/dir")) {
                        File directoryPath = new File(workingDir+"/files");
                        File[] files =  directoryPath.listFiles();
                        if (files == null) {
                            writer.writeUTF("No files found in directory");
                        } else {
                            String fileNames = "";
                            for (int i=0;i<files.length;i++) { 
                                fileNames += files[i].getName()+"\n";
                              } 
                            writer.writeUTF(fileNames);
                        }
                    } else if (command.equals("/?")) {
                        String commands = "/join <server_ip_address> <port> \n/leave \n/register <handle> \n/store <filename> \n/dir \n/get <filename>";
                        writer.writeUTF(commands);
                    }
                }                
            }

            s.close();
            
        } catch (Exception e) {
            e.printStackTrace(); //uncomment for debugging
        } finally {
            System.out.println("Server: Client " + s.getRemoteSocketAddress() + " has disconnected");
        }
    }
}