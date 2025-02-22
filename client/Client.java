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
    String filesDir = "";

    int byteCapacity = 24000;
    Scanner sc = new Scanner(System.in);
    String msg;
    Boolean flag = false;
    String clientName = null;

    while (!flag) {
      System.out.print("> ");
      msg = sc.nextLine();
      tempString = msg.split(" "); // parse string to get command
      command = tempString[0];

      if (command.equals("/join")) {
        try {
          host = tempString[1];
          port = Integer.parseInt(tempString[2]);
          if (host.equals("127.0.0.1") || host.equals("localhost")) {
            endpoint = new Socket(host, port);
            flag = true;
            System.out.println("Connection to the File Exchange Server is successful!");
          } else {
            throw new ConnectException();
          }
        } catch (ArrayIndexOutOfBoundsException e) {
          System.out.println("Error: Command parameters do not match or is not allowed.");
        } catch (ConnectException e) {
          System.out.println("Error: Connection to the Server has failed! Please check IP Address and Port Number.");
        } catch (UnknownHostException e) {
          System.out.println("Error: Command not found.");
        } catch (Exception e) {
          e.printStackTrace();
          break;
        }
      } else if (command.equals("/leave")) {
        if (!flag) {
          System.out.println("Error: Disconnection failed. Please connect to the server first.");
        } else {
          break;
        }
      } else if (command.equals("/?")) {
        System.out.println("/join <server_ip_address> <port> \n/leave");
      } else {
        System.out.println("Error: Command not found.");
      }
    }

    if (flag) {
      try {
        DataOutputStream writer = new DataOutputStream(endpoint.getOutputStream());
        DataInputStream reader = new DataInputStream(endpoint.getInputStream());

        while (flag) {
          System.out.print("> ");

          msg = sc.nextLine();

          tempString = msg.split(" ");
          command = tempString[0];

          if (command.equals("/register")) {
            if (clientName == null) {
              try {
                String username = tempString[1];
                msg = command + " " + username;
                writer.writeUTF(msg);
                String response = reader.readUTF();
                System.out.println(response);
                if (response.startsWith("Welcome")) {
                  clientName = username;
                  filesDir = workingDir + "/" + clientName + "/files";
                  new File(filesDir).mkdirs();
                }
              } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Error: Command parameters do not match or is not allowed.");
              }
            } else {
              System.out.println("Error: Command not found.");
            }
          } else if (command.equals("/?") && clientName == null) {
            System.out.println("/register <handle> \n/leave");
          } else if (command.equals("/leave")) {
            msg = command + " " + clientName;
            writer.writeUTF(msg);
            break;
          } else if (clientName == null) {
            System.out.println("Error: Command not found.");
          } else {

            if ((command.equals("/store"))) {
              try {
                File tempFile = new File(filesDir, tempString[1]);
                if (tempFile.isFile()) {
                  String timeStamp = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"))
                      .format(DateTimeFormatter.ofPattern("uuuu-MM-dd.HH.mm.ss"));
                  msg = command + " " + (tempString[1]) + " " + timeStamp + " " + clientName;
                  writer.writeUTF(msg);

                  DataInputStream dis = new DataInputStream(new FileInputStream(tempFile));

                  byte[] filebyte = new byte[byteCapacity];
                  int file = dis.read(filebyte, 0, filebyte.length);
                  writer.write(filebyte, 0, file);

                  System.out.println(clientName + "<" + timeStamp + ">" + ": Uploaded " + tempString[1]);
                  dis.close();
                } else {
                  System.out.println("Error: File not found.");
                }
              } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Error: Command parameters do not match or is not allowed.");
              }

            } else if (command.equals("/get")) {
              try {
                msg = command + " " + tempString[1];
                writer.writeUTF(msg);

                String reply = reader.readUTF();
                if (reply.equals("File received")) {
                  File tempFile = new File(filesDir, tempString[1]);
                  DataOutputStream dos = new DataOutputStream(new FileOutputStream(tempFile));

                  byte[] filebyte = new byte[byteCapacity];
                  int file = reader.read(filebyte, 0, filebyte.length);
                  dos.write(filebyte, 0, file);
                  writer.writeUTF("File '" + tempString[1] + "' sent successfully to " + clientName);
                  System.out.println("File received form Server: " + tempString[1]);
                  dos.close();
                } else {
                  System.out.println("Error: File not found in the server.");
                }
              } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Please enter file path of file");
              }
            } else if (command.equals("/leave")) {
              break;

            } else if (command.equals("/dir")) {
              msg = "/dir";
              writer.writeUTF(msg);
              String directory = reader.readUTF();
              System.out.println("Server Directory");
              System.out.println(directory);

            } else if (command.equals("/?")) {
              msg = "/?";
              writer.writeUTF(msg);
              System.out.println(reader.readUTF());

            } else {
              System.out.println("Error: Command not found.");
            }
          }
        }

        sc.close();
        endpoint.close();
      } catch (SocketException e) {
        System.out.println("Server has stopped");
      } catch (EOFException e) {
        System.out.println("Server has stopped");
      } catch (Exception e) {
        e.printStackTrace();
      }

      System.out.println("Connection closed. Thank you!");
    }
  }

}
