package jetty;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.io.IOException;

public class EventSocket extends WebSocketAdapter
{
    @Override
    public void onWebSocketConnect(Session sess)
    {
        super.onWebSocketConnect(sess);
        System.out.println("Socket Connected: " + sess);
    }

    @Override
    public void onWebSocketText(String message)
    {
        super.onWebSocketText(message);
        System.out.println("Received TEXT message: " + message);
        try {
            if(message.startsWith("/iam ")){
                String user = message.substring(5);
                if(Users.addUser(this.getSession(), user)){
                    this.getRemote().sendString("<b style='color:red'>Server:</b> Welcome to chat, " + user);
                    Users.sendMessage("<span style='color:gray'>" + user + " joined to chat</span>", user);
                }else{
                    this.getRemote().sendString("<b style='color:red'>Server:</b> " + user + " already exists!");
                }
            }else{
                String user = Users.getName(this.getSession());
                if(user != null){
                    this.getRemote().sendString("<b style='color:yellow'>" + user + ":</b> " + message);
                    Users.sendMessage("<b>" + user + ":</b> " + message, user);
                }else{
                    this.getRemote().sendString("<b style='color:red'>Server:</b> Auth error! Update this page and login, please!");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason)
    {
        Users.removeUser(this.getSession());
        super.onWebSocketClose(statusCode,reason);
        System.out.println("Socket Closed: [" + statusCode + "] " + reason);
    }

    @Override
    public void onWebSocketError(Throwable cause)
    {
        Users.removeUser(this.getSession());
        super.onWebSocketError(cause);
        cause.printStackTrace(System.err);
    }
}