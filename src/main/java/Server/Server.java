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
import Chat.ChatUser;
import Chat.Packet;

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
        private ChatUser user;

        private Consumer<Serializable> callback;

        public ChatThread(Socket connection, int id) {
            this.connection = connection;
            this.id = id;
            // Create ChatUser and set member variables
            this.user = new ChatUser();
            user.id = id;
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
                    Packet req = (Packet) in.readObject();

                    callback.accept("Client #" + this.id + " sent a request: ");

                    if (req.chat == null)
                        throw new Exception();

                    ChatData chat = req.chat;
                    chat.from = user;

                    callback.accept(
                            String.format("\tClient #%d sent a message to client #%d", chat.from.id, chat.to.id));

                    chatThreads.get(chat.to.id).sendChatData(chat);
                    out.writeObject(req);
                } catch (Exception e) {
                    callback.accept("Error: Could not fetch request from chat client #" + this.id);
                    try {
                        this.connection.close();
                        // TODO: Test for errors
                        chatThreads.remove(this.user.id);
                    } catch (IOException e1) {
                        callback.accept("Error: Could not close connection for chat client #" + this.id);
                    }
                }
            }
            callback.accept("Connection closed for chat client #" + this.id);
        }

        public void sendChatData(ChatData data) throws IOException {
            Packet res = new Packet();
            res.chat = data;
            this.out.writeObject(res);
        }
    }
}