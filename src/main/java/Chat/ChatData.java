package Chat;

import java.io.Serializable;
import java.util.ArrayList;

public class ChatData implements Serializable {
    private static final long serialVersionUID = -2270421533467436142L;

    public String message = null;
    public ChatUser from = null;
    public ChatUser to = null;
    public ArrayList<ChatUser> clients = null;

    public static class ChatUser {
        public String name;
        public int id;
    }
}
