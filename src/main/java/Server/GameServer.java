package Server;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;

import BaccaratGame.BaccaratThread;
import Chat.ChatThread;

import java.lang.Thread;

public class GameServer {
    int count = 1;
    int port = 5555;
    ArrayList<BaccaratThread> games = new ArrayList<BaccaratThread>();
    ArrayList<ChatThread> chats = new ArrayList<ChatThread>();
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
                    Socket connection = this.socket.accept();
                    BaccaratThread gameClient = new BaccaratThread(connection, callback, count);
                    ChatThread chatClient = new ChatThread(connection, callback, count);

                    callback.accept("New client connected:");
                    callback.accept("\tClient #" + count);

                    // Manage game client
                    games.add(gameClient);
                    gameClient.start(); // Start game thread for count client

                    // Manage chat client
                    chats.add(chatClient);
                    chatClient.start(); // Start chat thread for count client

                    count++;
                }
            } catch (Exception e) {
            }
        }

        public void close() throws IOException {
            this.socket.close();
        }
    }
}