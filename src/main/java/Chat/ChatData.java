package Chat;

import java.io.Serializable;
import java.util.List;

public class ChatData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public String message = null;
    public ChatUser from = null;
    public ChatUser to = null;
    public List<ChatUser> clients = null;

    public static class ChatUser implements Serializable {
        private static final long serialVersionUID = 1L;

        public String name;
        public int id;
    }
}
