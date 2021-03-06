

import Chat.ChatData;
import org.junit.Before;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class ChatTest
{
    private ChatData cd1, cd2;
    private ChatData.ChatUser cu1, cu2, cur;

    @Before
    public void init()
    {
        cd1 = new ChatData();
        cu1 = new ChatData.ChatUser();

        cd2 = new ChatData();
        cd2.message = "Hi";

        cd2.from = new ChatData.ChatUser();
        cd2.from.name = "Client 1";
        cd2.from.id = 1;

        cd2.to = new HashSet<>();
        cd2.to.add(cu1);
        Iterator<ChatData.ChatUser> it = cd2.to.iterator();
        cur = it.next();

        cd2.clients = new ArrayList<>();
        cd2.clients.add(cu1);

        cu2 = new ChatData.ChatUser();
        cu2.id = 32;
        cu2.name = "User";
    }

    @Test
    public void CardTest()
    {
        // ChatData Constructor test
        assertEquals("Chat.ChatData", cd1.getClass().getName());
        assertEquals(null, cd1.message);
        assertEquals(null, cd1.from);
        assertEquals(null, cd1.to);
        assertEquals(null, cd1.clients);

        assertEquals("Hi", cd2.message);
        assertEquals("Client 1", cd2.from.name);
        assertEquals(1, cd2.from.id);
        assertEquals(1, cd2.to.size());
        assertEquals(0, cur.id);
        assertEquals(null, cur.name);
        assertEquals(1, cd2.clients.size());
        assertEquals(cu1, cd2.clients.get(0));

        // ChatUser Constructor test
        assertEquals("Chat.ChatData$ChatUser", cu1.getClass().getName());
        assertEquals(null, cu1.name);
        assertEquals(0, cu1.id);

        assertEquals(32, cu2.id);
        assertEquals("User", cu2.name);
    }
}