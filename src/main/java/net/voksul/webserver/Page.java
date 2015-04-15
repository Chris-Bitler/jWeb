package net.voksul.webserver;

import org.apache.http.client.utils.DateUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Page {
    private HashMap<String, String> headers;
    private HashMap<String, String> cookies;
    private HashMap<String, String> params;
    private HashMap<String, String> cookiesToSet = new HashMap<String, String>();
    private String response = "";
    private HashMap<String,Object> pageParams = new HashMap<String, Object>();
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public void init(Socket socket, BufferedReader reader, HashMap<String,String> params) {
        this.socket = socket;
        this.reader = reader;
        this.params = params;
        try {
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        headers = new HashMap<String, String>();
        cookies = new HashMap<String, String>();
        String line;
        try {
            while ((line = reader.readLine()) != "\n" && line != null && line.length() != 0) {
                String key = line.substring(0, line.indexOf(":"));
                String value = line.substring(line.indexOf(":") + 1).trim();
                if (!key.equalsIgnoreCase("Cookie")) {
                    headers.put(key, value);
                } else {
                    String[] cookieSplit = value.split(";");
                    for (String cookie : cookieSplit) {
                        String trimmed = cookie.trim();
                        String[] trimmedSplit = trimmed.split("=");
                        cookies.put(trimmedSplit[0], trimmedSplit[1]);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (cookies.get("sess_id") == null) {
            //TODO: Generate more secure session key
            String id = UUID.randomUUID().toString();
            setCookie("sess_id", id);
            Session.createSession(id);
        } else {
            if (Session.getSession(cookies.get("sess_id")) == null) {
                String id = UUID.randomUUID().toString();
                setCookie("sess_id", id);
                Session.createSession(id);
            }
        }
        handle();
        try {
            done();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void echo(String msg) {
        response += (msg + "\n");
    }

    //This is the method to override
    public void handle() {

    }

    public void setCookie(String key, String value) {
        cookiesToSet.put(key, value);
    }

    public HashMap<String, String> getCookies() {
        return cookies;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    private void done() throws IOException {
        writer.write("HTTP/1.1 200 OK\n");
        writer.write("Date: " + DateUtils.formatDate(new Date(System.currentTimeMillis())) + "\n");
        for (Map.Entry<String, String> toSet : cookiesToSet.entrySet()) {
            writer.write("Set-Cookie: " + toSet.getKey() + "=" + toSet.getValue() + "; Domain=" + headers.get("Host") + "; Expires=" + DateUtils.formatDate(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(2))) + "\n");
        }
        writer.write("Content-Type: text/html\n");
        writer.write("Content-Length: " + response.getBytes().length + "\n");
        writer.write("\n");
        writer.write(response);
        writer.flush();
        reader.close();
        writer.close();
        socket.close();
    }

    public void include(Class<? extends Page> page)
    {
        try {
            Page include = page.newInstance();
            include.cookiesToSet = this.cookiesToSet;
            include.socket = this.socket;
            include.cookies = this.cookies;
            include.headers = this.headers;
            include.response = this.response;
            include.handle();
            this.cookiesToSet = include.cookiesToSet;
            this.socket = include.socket;
            this.cookies = include.cookies;
            this.headers = include.headers;
            this.response = include.response;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void include(Class<? extends Page> page, HashMap<String,Object> params)
    {
        try {
            Page include = page.newInstance();
            include.pageParams = params;
            include.cookiesToSet = this.cookiesToSet;
            include.socket = this.socket;
            include.cookies = this.cookies;
            include.headers = this.headers;
            include.response = this.response;
            include.handle();
            this.cookiesToSet = include.cookiesToSet;
            this.socket = include.socket;
            this.cookies = include.cookies;
            this.headers = include.headers;
            this.response = include.response;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public Object getPageParameter(String key)
    {
        return pageParams.get(key);
    }

    public String get(String key)
    {
        return params.get(key);
    }
}
