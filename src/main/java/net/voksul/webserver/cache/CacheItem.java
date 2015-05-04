package net.voksul.webserver.cache;

/**
 * Created by Chris on 5/4/2015.
 */
public class CacheItem {
    Object item;
    long timeSet;
    long timeLasts;

    public CacheItem(Object data, int seconds) {
        this.item = data;
        this.timeLasts = seconds * 1000;
        this.timeSet = System.currentTimeMillis();
    }

    public Object getData()
    {
        return item;
    }
}
