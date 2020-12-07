package Chat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

public class ChatData implements Serializable {
    private static final long serialVersionUID = 1L;

    public String message = null;
    public ChatUser from = null;
    public HashSet<ChatUser> to = null;
    public ArrayList<ChatUser> clients = null;
    public ChatUser me = null; // Must only be sent when connection is first established

    public static class ChatUser implements Serializable {
        private static final long serialVersionUID = 1L;

        public String name;
        public int id;
    }
}
