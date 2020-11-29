package Server;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.function.Consumer;

import BaccaratGame.BaccaratThread;

import java.lang.Thread;

public class GameServer {
    int count = 1;
    int port = 5555;
    ArrayList<BaccaratThread> clients = new ArrayList<BaccaratThread>();
    ServerThread server;
    private Consumer<Serializable> callback;

    public GameServer(Consumer<Serializable> call, int port) {
        this.callback = call;
        this.port = port;
        server = new ServerThread();
        server.start();
    }

    class ServerThread extends Thread {
        ServerSocket socket;

        public void run() {
            try {
                this.socket = new ServerSocket(port);

                callback.accept("Server is running on port " + port);
                callback.accept("Waiting for client to connect...");

                while (true) {
                    BaccaratThread client = new BaccaratThread(this.socket.accept(), callback, count);
                    callback.accept("New client connected:");
                    callback.accept("\tClient #" + count);
                    clients.add(client);
                    client.start();

                    count++;
                }
            } catch (Exception e) {
            }
        }

        public void close() throws IOException {
            this.socket.close();
        }
    }

    class ClientThread extends Thread {

    }
}