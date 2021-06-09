package source.com.beatbox;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.*;

public class MusicServer {
    private ArrayList<ObjectOutputStream> clientOutputStreams;

    public static void main(String[] args) {
        new MusicServer().go();
    }

    public class ClientHandler implements Runnable {
        private Socket clientSocket;
        private ObjectInputStream in;

        public ClientHandler(Socket socket) throws IOException {
            try (var input= new ObjectInputStream(socket.getInputStream());) {
                this.clientSocket= socket;
                this.in= input;
            } catch (Exception e) { e.printStackTrace(); }
        }

        @Override
        public void run() {
            Object o2= null;
            Object o1= null;

            try {
                while ((o1= in.readObject()) != null) {
                    o2= in.readObject();

                    var logger= Logger.getLogger(ClientHandler.class.getName());
                    logger.log(Level.INFO, "Read 2 objects");
                    tellEveryone(o1, o2);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    public void go() {  
        this.clientOutputStreams= new ArrayList<>();

        try (var serverSock= new ServerSocket(4242);) {
            var limit= true;
            var counter= 0;

            while (limit) {
                var clientSocket= serverSock.accept();
                var out= new ObjectOutputStream(clientSocket.getOutputStream());
                this.clientOutputStreams.add(out);

                var t= new Thread(new ClientHandler(clientSocket));
                t.start();

                counter++;
                limit= counter < Integer.MAX_VALUE;
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void tellEveryone(Object o1, Object o2) {
        var it= this.clientOutputStreams.iterator();

        try (var out= it.next()) {
            out.writeObject(o1);
            out.writeObject(o2);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
