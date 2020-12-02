package Chat;

import java.io.Serializable;
import java.util.ArrayList;

public class Packet implements Serializable {
    private static final long serialVersionUID = 1L;

    public ChatData chat = null;
    public ArrayList<ChatUser> users = null;
}
