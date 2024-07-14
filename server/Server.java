import java.net.*;

public class Server {

    public static byte[][] byteStorage = new byte[10][];
    public static String[] nameStorage = new String[10];
    public static int currentCapacity = 0;
    public static void main(String[] args) {
        int port = 4000; // Can be changed

        try {
            ServerSocket ss = new ServerSocket(port);

            System.out.println("Server: Listening to port " + port);

            while (true) {
                // Waits for a client to connect
                Socket endpoint = ss.accept();

                System.out.println("Server: Client at " + endpoint.getRemoteSocketAddress() + " has connected");

                // Make the Thread Object
                Connection connect = new Connection(endpoint);
                // Start the thread
                connect.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}