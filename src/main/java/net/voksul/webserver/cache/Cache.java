package net.voksul.webserver.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Chris on 5/4/2015.
 */
public class Cache {
    private static HashMap<String,CacheItem> cache = new HashMap<>();

    public static void insertToCache(String key, Object data)
    {
        cache.put(key, new CacheItem(data,60));
    }

    public static void insertToCache(String key, Object data, int time)
    {
        cache.put(key, new CacheItem(data,time));
    }

    public static void tick()
    {
        Iterator<Map.Entry<String,CacheItem>> iter = cache.entrySet().iterator();
        while(iter.hasNext())
        {
            CacheItem item = iter.next().getValue();
            if(System.currentTimeMillis() - item.timeSet > item.timeLasts)
            {
                iter.remove();
            }
        }
    }

    public static CacheItem getFromCache(String key)
    {
        return cache.get(key);
    }


}
