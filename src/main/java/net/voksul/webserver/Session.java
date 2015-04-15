package net.voksul.webserver;

import java.util.HashMap;

public class Session {
    private static HashMap<String, HashMap<String, String>> sessionData = new HashMap<String, HashMap<String, String>>();

    public static HashMap<String, String> getSession(String id) {
        return sessionData.get(id);
    }

    public static void createSession(String id) {
        sessionData.put(id, new HashMap<String, String>());
    }
}
