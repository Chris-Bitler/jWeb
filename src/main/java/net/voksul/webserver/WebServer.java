package net.voksul.webserver;

import org.apache.http.impl.bootstrap.SSLServerSetupHandler;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.HashMap;

public class WebServer {
    ServerSocket socket;
    String ip;
    int port;
    HashMap<String, Class> routes = new HashMap<String, Class>();
    public WebServer(String ip, int port)
    {
        this.ip = ip;
        this.port = port;
    }
    public void addRoute(String route, Class handler) {
        routes.put(route, handler);
    }

    public Class getHandler(String route) {
        if (routes.get(route) != null) {
            return routes.get(route);
        } else {
            return null;
        }
    }

    public void start(String keystoreName, String password) throws IOException {
        socket = new ServerSocket();
        socket.bind(new InetSocketAddress(ip, port));
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(keystoreName),password.toCharArray());
            Enumeration<String> ks_ = ks.aliases();
            KeyManagerFactory km = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            km.init(ks,password.toCharArray());
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(km.getKeyManagers(),null,null);
            SSLServerSocketFactory ssf = sc.getServerSocketFactory();
            SSLServerSocket ssl = (SSLServerSocket) ssf.createServerSocket();
            ssl.bind(new InetSocketAddress(ip,443));
            new Thread(new SSLThread(ssl,this)).start();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        while (true) {
            Socket request = socket.accept();
            new Thread(new PageHandler(this, request)).start();
        }
    }
}
