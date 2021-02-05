package jetty;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.*;

public class Users {
    private static HashMap<Session, String> usernames = new HashMap<>();
    private static Object sync = new Object();
    public static boolean addUser(Session s, String name){
        synchronized (sync){
            if(usernames.containsValue(name)){
                return false;
            }
            usernames.put(s, name);
            return true;
        }
    }
    public static void removeUser(Session s) {
        synchronized (sync){
            String user = usernames.remove(s);
            try {
                if(user != null)
                    sendMessage("<span style='color:gray'>" + user + " left the chat</span>", user);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getName(Session s) {
        synchronized (sync){
            return usernames.get(s);
        }
    }

    public static void sendMessage(String data, String user) throws IOException {
        synchronized (sync) {
            for (Map.Entry<Session, String> entry : usernames.entrySet()) {
                if(!entry.getValue().equals(user))
                    entry.getKey().getRemote().sendString(data);
            }
        }
    }
}
