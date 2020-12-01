package BaccaratGame;

import java.io.Serializable;
import java.util.ArrayList;

import Chat.ChatData;
import Chat.ChatUser;

public class Packet implements Serializable {
    private static final long serialVersionUID = 1L;

    public BaccaratInfo gameData = null;
    public ChatData chatData = null;
    public ArrayList<ChatUser> users = null;
}
