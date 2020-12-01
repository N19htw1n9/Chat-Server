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

    private Consumer<Serializable> callback;

    public ChatThread(Socket connection, Consumer<Serializable> callback,
            ConcurrentHashMap<Integer, ChatThread> chatThreads, int id) {
        this.connection = connection;
        this.callback = callback;
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
                ChatUser me = req.me;
                ChatUser friend = req.friend;
                String message = req.message;
            } catch (Exception e) {
                callback.accept("Error: Could not fetch request from chat client #" + this.id);
                try {
                    this.connection.close();
                } catch (IOException e1) {
                    callback.accept("Error: Could not close connection for chat client #" + this.id);
                }
            }
        }
        callback.accept("Connection closed for chat client #" + this.id);
    }
}
