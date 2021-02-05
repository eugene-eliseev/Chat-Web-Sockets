package jetty;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.stream.Collectors;

public class EventServer
{
    public static void main(String[] args)
    {
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(8080);
        server.addConnector(connector);

        // Setup the basic application "context" for this application at "/"
        // This is also known as the handler tree (in jetty speak)
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // Add a websocket to a specific path spec
        ServletHolder holderEvents = new ServletHolder("ws-events", EventServlet.class);
        context.addServlet(holderEvents, "/events/*");

        try
        {
            server.start();
            HttpServer httpServer = HttpServer.create();
            InetAddress addr = InetAddress.getByName("0.0.0.0");

            httpServer.bind(new InetSocketAddress(addr,8081), 0);
            httpServer.createContext("/", new EchoHandler());
            httpServer.start();
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.err);
        }
    }

    static class EchoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Connection from " + exchange.getRemoteAddress());
            String d = "/index.html";
            if(!exchange.getRequestURI().toString().equalsIgnoreCase("/")){
                d = exchange.getRequestURI().toString();
            }
            try {
                InputStream in = this.getClass().getClassLoader().getResourceAsStream("web" + d);
                String s = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));

                byte[] bytes = s.toString().getBytes();
                exchange.sendResponseHeaders(200, bytes.length);

                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}