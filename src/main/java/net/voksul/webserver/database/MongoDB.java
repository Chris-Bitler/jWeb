package net.voksul.webserver.database;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Chris on 5/4/2015.
 */
public class MongoDB {
    MongoClient client;
    public MongoDB(String username, String password, String db, String host)
    {
        MongoCredential credential = MongoCredential.createCredential(username,db,password.toCharArray());
        client = new MongoClient(new ServerAddress(host), Arrays.asList(credential));
    }

    public MongoDB(String username, String password, String db, String host, int port)
    {
        MongoCredential credential = MongoCredential.createCredential(username,db,password.toCharArray());
        client = new MongoClient(new ServerAddress(host,port),Arrays.asList(credential));
    }

    public DB getDB(String name)
    {
        return client.getDB(name);
    }

    public MongoClient getUnderlyingClient()
    {
        return client;
    }

}
