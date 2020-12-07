package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Consumer;
import java.lang.Thread;

import Chat.ChatData;

public class Server {
    int count = 1;
    int port = 5555;
    ArrayList<ChatThread> chatThreads = new ArrayList<ChatThread>();
    ArrayList<ChatData.ChatUser> clientsList = new ArrayList<ChatData.ChatUser>();
    ServerThread server;
    private Consumer<Serializable> callback;

    public Server(Consumer<Serializable> call, int port) {
        this.callback = call;
        this.port = port;
        server = new ServerThread();
        server.start();
    }

    private void updateChatClients() {
        chatThreads.stream().forEach(client -> {
            if (client == null)
                return;

            ChatData data = new ChatData();
            data.clients = (ArrayList<ChatData.ChatUser>) clientsList.clone();

            try {
                client.sendChatData(data);
            } catch (IOException e) {
                callback.accept("Error: Could not update client list...");
            }
        });
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
                callback.accept("Something went wrong. Make sure port " + port + " is not in use.");
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
        private ChatData.ChatUser user = new ChatData.ChatUser();

        public ChatThread(Socket connection, int id) {
            this.connection = connection;
            this.id = id;
            user.id = this.id;
            user.name = null;
        }

        @Override
        public void run() {
            try {
                this.out = new ObjectOutputStream(this.connection.getOutputStream());
                this.in = new ObjectInputStream(this.connection.getInputStream());
                connection.setTcpNoDelay(true);

                // Add user to clientsList
                user.id = id;
                clientsList.add(user);

                updateChatClients();
            } catch (Exception e) {
                callback.accept("Chat client #" + id);
                callback.accept("\tError: Could not open streams");
            }

            while (!this.connection.isClosed()) {
                try {
                    ChatData req = (ChatData) in.readObject();
                    // Check if client request with ChatUser me, if so then set user to req.me
                    if (req.me != null) {
                        req.me.id = id; // Set user id
                        user = req.me;
                        sendChatData(req);
                    }
                    req.from = user;

                    callback.accept("Request from client #" + this.id);
                    callback.accept(
                            String.format("\tClient #%d sent a message to: %s", id, buildToUsersString(req.to)));

                    try {
                        req.to.stream().forEach(toClient -> {
                            ChatThread toUserThread = chatThreads.get(toClient.id - 1);
                            try {
                                toUserThread.sendChatData(req);
                            } catch (IOException e) {
                                callback.accept("Something went wrong when trying to communicate with client #"
                                        + toUserThread.id);
                            }
                        });
                    } catch (Exception e) {
                        callback.accept("Could not send message to clients");
                    }

                    if (!req.to.isEmpty())
                        sendChatData(req);
                } catch (Exception e) {
                    callback.accept("Error: Could not fetch request from chat client #" + this.id);
                    try {
                        this.connection.close();
                        clientsList.set(id - 1, null);
                        chatThreads.set(id - 1, null);
                        updateChatClients();
                    } catch (IOException e1) {
                        callback.accept("Error: Could not close connection for chat client #" + this.id);
                    }
                }
            }
            callback.accept("Connection closed for chat client #" + this.id);
        }

        public void sendChatData(ChatData res) throws IOException {
            out.writeObject(res);
        }

        private String buildToUsersString(HashSet<ChatData.ChatUser> toUsersSet) {
            StringBuilder res = new StringBuilder();
            toUsersSet.stream().forEach(i -> res.append(i.id + ", "));
            return res.toString();
        }
    }
}