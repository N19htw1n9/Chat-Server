package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.lang.Thread;

import Chat.ChatData;

public class Server {
    int count = 1;
    int port = 5555;
    ArrayList<ChatThread> chatThreads = new ArrayList<ChatThread>();
    ServerThread server;
    private Consumer<Serializable> callback;

    public Server(Consumer<Serializable> call, int port) {
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
                    ChatThread chatClient = new ChatThread(connection, count);

                    callback.accept("New client connected:");
                    callback.accept("\tClient #" + count);

                    // Manage chat client
                    chatThreads.add(chatClient);
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

    class ChatThread extends Thread {
        private Socket connection;
        private int id;
        private ObjectOutputStream out;
        private ObjectInputStream in;

        private Consumer<Serializable> callback;

        public ChatThread(Socket connection, int id) {
            this.connection = connection;
            this.id = id;
        }

        @Override
        public void run() {
            callback.accept("Chat created for client #" + this.id);

            try {
                this.out = new ObjectOutputStream(this.connection.getOutputStream());
                this.in = new ObjectInputStream(this.connection.getInputStream());
            } catch (Exception e) {
                callback.accept("Chat client #" + id);
                callback.accept("\tError: Could not open streams");
            }

            while (!this.connection.isClosed()) {
                try {
                    ChatData req = (ChatData) in.readObject();

                    callback.accept("Request from client #" + this.id);

                    if (req.message == null)
                        throw new Exception();

                    callback.accept(String.format("\tClient #%d sent a message to client #%d", req.from.id, req.to.id));

                    chatThreads.get(req.to.id).sendChatData(req);
                    out.writeObject(req);
                } catch (Exception e) {
                    callback.accept("Error: Could not fetch request from chat client #" + this.id);
                    try {
                        this.connection.close();
                        try {
                            chatThreads.get(id).join();
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        chatThreads.remove(id);
                        callback.accept(String.format("Client %d removed from list", id));
                    } catch (IOException e1) {
                        callback.accept("Error: Could not close connection for chat client #" + this.id);
                    }
                }
            }
            callback.accept("Connection closed for chat client #" + this.id);
        }

        public void sendChatData(ChatData data) throws IOException {
            out.writeObject(data);
        }
    }
}