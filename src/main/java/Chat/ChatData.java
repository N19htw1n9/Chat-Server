package Chat;

import java.io.Serializable;

public class ChatData implements Serializable {
    private static final long serialVersionUID = -2270421533467436142L;

    public ChatUser from;
    public ChatUser to;

    public String message;
}
