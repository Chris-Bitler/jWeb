package net.voksul.webserver.database;

import javax.xml.transform.Result;
import java.sql.*;

/**
 * Created by Chris on 5/4/2015.
 */
public class MySQLDB {
    Connection connection;

    public MySQLDB(String username, String password, String host, String data)
    {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://"+host+"/"+data,username,password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void runPreparedStatement(String query, Object[] data)
    {
        try {
            PreparedStatement prepared = connection.prepareCall(query);
            for(int i = 1; i <= data.length; i++) {
                prepared.setObject(i,data[i-1]);
            }
            prepared.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet runPreparedQuery(String query, Object[] data)
    {
        try {
            PreparedStatement prepared = connection.prepareCall(query);
            for(int i = 1; i <= data.length; i++) {
                prepared.setObject(i,data[i-1]);
            }
            return prepared.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
