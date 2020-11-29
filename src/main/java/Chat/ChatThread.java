package Chat;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.Thread;
import java.net.Socket;
import java.util.function.Consumer;

public class ChatThread extends Thread {
    private Socket connection;
    private int id;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private Consumer<Serializable> callback;

    public ChatThread(Socket connection, Consumer<Serializable> callback, int id) {
        this.connection = connection;
        this.callback = callback;
        this.id = id;
    }

    @Override
    public void run() {
        System.out.println("Chat thread started for client #" + this.id);
    }
}
