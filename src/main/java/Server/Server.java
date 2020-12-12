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

    /**
     * Synchronized method to update all non-null clients on the clientThreads list.
     * Can be used if a new client joins the network, or if an existing client is
     * updated with new information.
     */
    private synchronized void updateChatClients() {
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

    /**
     * Synchronized method to add new clients to chatThreads
     * 
     * @param client
     */
    private synchronized void addChatThreads(ChatThread client) {
        chatThreads.add(client);
    }

    /**
     * Synchronized method to set a new value for the i-th element in chatThreads
     * 
     * @param i         Element to set new value on
     * @param newClient Object that replaces the current value
     */
    private synchronized void setClient(int i, ChatThread newClient) {
        chatThreads.set(i, newClient);
    }

    /**
     * Synchronized method to add new ChatData.ChatUser to clientsList
     * 
     * @param user
     */
    private synchronized void addClientsList(ChatData.ChatUser user) {
        clientsList.add(user);
    }

    /**
     * Synchronized method to set a new value for the i-th element in clientsList
     * 
     * @param i         Element to set new value on
     * @param newClient Object that replaces the current value
     */
    private synchronized void setClientsList(int i, ChatData.ChatUser newUser) {
        clientsList.set(i, newUser);
    }

    /**
     * Synchronized method to increment the count by i
     * 
     * @param i
     */
    private synchronized void incrementCount(int i) {
        count += i;
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
                    addChatThreads(chatClient); // synchronously add chatThread to threads list
                    chatClient.start(); // Start chat thread for count client
                    incrementCount(1);
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

        private void userNameSet(ChatData req) throws Exception {
            req.me.id = id;
            
            user.name = req.me.name;
            user.id = id;
            addClientsList(user);

            updateChatClients();

            callback.accept(String.format("\tRequested a name change to %s", user.name));
            sendChatData(req);
        }

        private void handleChat(ChatData req) throws Exception {
            req.from = user;
            callback.accept(String.format("\tClient #%d sent a message to: %s", id, buildToUsersString(req.to)));

            try {
                req.to.stream().forEach(toClient -> {
                    ChatThread toUserThread = chatThreads.get(toClient.id - 1);
                    try {
                        toUserThread.sendChatData(req);
                    } catch (IOException e) {
                        callback.accept(
                                "\tSomething went wrong when trying to communicate with client #" + toUserThread.id);
                    }
                });
            } catch (Exception e) {
                callback.accept("\tCould not send message to clients");
            }

            if (!req.to.isEmpty())
                sendChatData(req);
        }

        @Override
        public void run() {
            try {
                this.out = new ObjectOutputStream(this.connection.getOutputStream());
                this.in = new ObjectInputStream(this.connection.getInputStream());
                connection.setTcpNoDelay(true);
            } catch (Exception e) {
                callback.accept("Chat client #" + id);
                callback.accept("\tError: Could not open streams");
            }

            while (!this.connection.isClosed()) {
                try {
                    ChatData req = (ChatData) in.readObject();

                    callback.accept(String.format("Client #%d sent a request", this.id));

                    // Set user to req.me if me is set to ChatUser with name field
                    if (req.me != null && req.me.name != null) {
                        userNameSet(req);
                    }

                    if (req.to != null && req.message != null) {
                        handleChat(req);
                    }
                } catch (Exception e) {
                    callback.accept("Error: Could not fetch request from chat client #" + this.id);

                    try {
                        this.connection.close();

                        setClientsList(id - 1, null);
                        setClient(id - 1, null);
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