import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Arrays;

public class Connection extends Thread {

    private Socket s;
    Storage storage2;
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

                String[] tempString = msg.split(" "); //parse string and /command given

                String command = tempString[0];
                String filepath = tempString[1];
                System.out.println(command);
                System.out.println(filepath);

                if (command.equals("/store")) {

                    i = Storage.currentCapacity;

                    Storage.byteStorage[i] = Files.readAllBytes(Paths.get(filepath)); //assign first byte storage slot to given file
                    Storage.nameStorage[i] = Paths.get(filepath).getFileName().toString(); //add filename to storage
                    System.out.println("FILENAME: " + Storage.nameStorage[i]);
                    Storage.currentCapacity = Storage.currentCapacity + 1;

                    writer.writeUTF("File '" + Storage.nameStorage[i] + "' stored successfully");

                } else if (command.equals("/get")) {

                    String filename = Paths.get(filepath).getFileName().toString();

                    i = Arrays.asList(Storage.nameStorage).indexOf(filename);

                    if (i != -1) {
                        InputStream in = new ByteArrayInputStream(Storage.byteStorage[i]); //add stored bytes to input stream
                        FileOutputStream out = new FileOutputStream(Storage.nameStorage[i]); //output bytes to file of stored file name

                        int count;
                        byte[] buffer = new byte[2048];
                        while((count = in.read(buffer)) > 0) {
                            out.write(buffer, 0, count);
                        }

                        writer.writeUTF("File retrieved successfully");
                    } else {
                        writer.writeUTF("File does not exist in storage");
                    }
                }
            }

            s.close();
        } catch (Exception e) {
            // e.printStackTrace();
        } finally {
            System.out.println("Server: Client " + s.getRemoteSocketAddress() + " has disconnected");
        }
    }
}