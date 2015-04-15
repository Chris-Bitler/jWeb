package net.voksul.webserver;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.IOException;

public class SSLThread implements Runnable {
    SSLServerSocket socket;
    WebServer server;
    public SSLThread(SSLServerSocket socket, WebServer server)
    {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        while(true)
        {
            SSLSocket s = null;
            try {
                s = (SSLSocket) socket.accept();
                new Thread(new PageHandler(server,s)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
