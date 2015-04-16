package net.voksul.webserver;

import org.apache.http.client.utils.DateUtils;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;

public class PageHandler implements Runnable {
    Socket request;
    WebServer server;

    String[] fileTypes = {".gif",".jpeg",".jpg",".png",".bmp",".css",".js"};
    public PageHandler(WebServer webserver, Socket request) {
        this.request = request;
        this.server = webserver;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
            String route = reader.readLine();
            if (route != null) {
                String[] routeSplit = route.split(" ");
                String method = routeSplit[0];
                String page = routeSplit[1];
                HashMap<String,String> params = new HashMap<String, String>();
                if(page.indexOf("?") != -1)
                {
                    //TODO: Better variable names?
                    String[] pageSplit = page.split("\\?");
                    if(pageSplit.length == 2)
                    {
                        page = pageSplit[0];
                        String[] pageSplitSplit = pageSplit[1].split("&");
                        for(String pageSplitSplitPiece : pageSplitSplit)
                        {
                            String[] pageSplitSplitPieceSplit = pageSplitSplitPiece.split("=");
                            if(pageSplitSplitPieceSplit.length == 2)
                            {
                                params.put(pageSplitSplitPieceSplit[0],pageSplitSplitPieceSplit[1]);
                            }
                        }
                    }
                }
                String httpVer = routeSplit[2];
                if(!(request instanceof SSLSocket)) {
                    System.out.println(method + " request from " + request.getInetAddress().toString() + " for " + page + " on HTTP version " + httpVer);
                }else{
                    System.out.println(method + " request from " + request.getInetAddress().toString() + " for " + page + " on HTTPS version " + httpVer);
                }
                if (!httpVer.equalsIgnoreCase("HTTP/1.1")) {
                    httpVersionMismatch();
                    return;
                }
                if(!isFileType(page)) {
                    if (server.getHandler(page) != null) {
                        Class handler = server.getHandler(page);
                        Page pageInstance = (Page) handler.newInstance();
                        pageInstance.init(method, request, reader, params);
                    } else {
                        error404();
                        return;
                    }
                }else{
                    File f = new File(System.getProperty("user.dir")+page);
                    if(f.exists())
                    {
                        FileInputStream fr = new FileInputStream(f);
                        byte[] data = new byte[(int) f.length()];
                        fr.read(data);
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(request.getOutputStream()));
                        writer.write("HTTP/1.1 200 OK\n");
                        writer.write("Date: " + DateUtils.formatDate(new Date(System.currentTimeMillis())) + "\n");
                        writer.write("Content-Type: "+getFileType(page) +"\n");
                        writer.write("Content-Length: " + data.length +"\n");
                        writer.write("\n");
                        writer.flush();
                        request.getOutputStream().write(data);
                        writer.close();
                    }else{
                        error404();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isFileType(String page) {
        for(String url : fileTypes)
        {
            if(page.toLowerCase().endsWith(url))
            {
                return true;
            }
        }
        return false;
    }

    private String getFileType(String page) {
        for(String url : fileTypes)
        {
            if(page.toLowerCase().endsWith(url)) {
                switch(url.replace(".",""))
                {
                    case "png":
                        return "image/png";
                    case "jpg":
                        return "image/jpeg";
                    case "jpeg":
                        return "image/jpeg";
                    case "gif":
                        return "image/gif";
                    case "bmp":
                        return "image/bmp";
                    case "css":
                        return "text/css";
                    case "js":
                        return "text/javascript";
                }
            }
        }
        return "text/html";
    }

    private void error404() {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(request.getOutputStream()));
            writer.write("HTTP/1.1 404 Not Found\n");
            writer.write("Date: " + DateUtils.formatDate(new Date(System.currentTimeMillis())) + "\n");
            writer.write("Content-Type: text/html\n");
            writer.write("Content-Length: " + ("Page not found").length());
            writer.write("\n");
            writer.write("Page not found");
            writer.flush();
            writer.close();
            request.close();
        } catch (Exception e) {

        }
    }

    private void httpVersionMismatch() {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(request.getOutputStream()));
            writer.write("HTTP/1.1 400 Bad Request\n");
            writer.write("Date: " + DateUtils.formatDate(new Date(System.currentTimeMillis())) + "\n");
            writer.write("Content-Type: text/html\n");
            writer.write("Content-Length: " + ("HTTP version 1.1 is required.").length());
            writer.write("\n");
            writer.write("HTTP version 1.1 is required.");
            writer.flush();
            writer.close();
            request.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void notGet() {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(request.getOutputStream()));
            writer.write("HTTP/1.1 405 Method Not Allowed\n");
            writer.write("Date: " + DateUtils.formatDate(new Date(System.currentTimeMillis())) + "\n");
            writer.write("Allow: GET\n");
            writer.write("Content-Type: text/html\n");
            writer.write("Content-Length: " + ("Only GET method is currently allowed").length());
            writer.write("\n");
            writer.write("Only GET method is currently allowed");
            writer.flush();
            writer.close();
            request.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
