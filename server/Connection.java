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

                        i = Server.currentCapacity;

                        Server.byteStorage[i] = Files.readAllBytes(Paths.get(parameter)); //assign first byte storage slot to given file
                        Server.nameStorage[i] = Paths.get(parameter).getFileName().toString(); //add filename to storage
                        System.out.println("FILENAME: " + Server.nameStorage[i]);
                        Server.currentCapacity = Server.currentCapacity + 1;

                        writer.writeUTF("File '" + Server.nameStorage[i] + "' stored successfully");

                    } else if (command.equals("/get")) {

                        String filename = Paths.get(parameter).getFileName().toString();

                        i = Arrays.asList(Server.nameStorage).indexOf(filename);

                        if (i != -1) {
                            InputStream in = new ByteArrayInputStream(Server.byteStorage[i]); //add stored bytes to input stream
                            FileOutputStream out = new FileOutputStream(Server.nameStorage[i]); //output bytes to file of stored file name

                            int count;
                            byte[] buffer = new byte[2048];
                            while((count = in.read(buffer)) > 0) {
                                out.write(buffer, 0, count);
                            }

                            writer.writeUTF("File retrieved successfully");
                            out.close();
                        } else {
                            writer.writeUTF("File does not exist in storage");
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException e) { // if a command without parameters is given, i.e. /dir and /?, this exception occurs at line 29 when trying to get a parameter, so the no parameter commands go here
                    String command = tempString[0];

                    if (command.equals("/dir")) {
                        if (Server.nameStorage[0] == null) {
                            writer.writeUTF("No files found in directory");
                        } else {
                            String directory = Stream.of(Server.nameStorage).filter(s -> s != null && !s.isEmpty()).collect(Collectors.joining("\n"));
                            writer.writeUTF(directory);
                        }
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