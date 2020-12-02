package Chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.Thread;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ChatThread extends Thread {
    private Socket connection;
    private int id;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ChatUser user;
    private ConcurrentHashMap<Integer, ChatThread> chatThreads;

    private Consumer<Serializable> callback;

    public ChatThread(Socket connection, Consumer<Serializable> callback,
            ConcurrentHashMap<Integer, ChatThread> chatThreads, int id) {
        this.connection = connection;
        this.callback = callback;
        this.id = id;
        this.chatThreads = chatThreads;
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
                chat.from = this.user;

                callback.accept(String.format("\tClient #%d sent a message to client #%d", chat.from.id, chat.to.id));

                this.chatThreads.get(chat.to.id).sendChatData(chat);
                this.out.writeObject(req);
            } catch (Exception e) {
                callback.accept("Error: Could not fetch request from chat client #" + this.id);
                try {
                    this.connection.close();
                    // TODO: Test for errors
                    this.chatThreads.remove(this.user.id);
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
