package net.deviantevil.core;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataSource {
    
    private Connection mCon;
    private String mUrl;
    private Logger mLogger;
    
    public DataSource(String url, Logger logger) {
        if(url == null) throw new IllegalArgumentException();
        mUrl = url;
        mLogger = logger;
    }
    
    public void setLogger(Logger logger) {
        mLogger = logger;
    }
    
    private boolean connect() {
        try {
            if(!mCon.isClosed() && mCon.isValid(1)) {
                return true;
            }
        } catch (SQLException e) {
            mLogger.log(Level.SEVERE, "Could not connect to "+mUrl);
            e.printStackTrace();
        }
        return false;
    }
    
    public Collection<Map<String, Object>> select(String db_name, 
                                                  List<String> fields,
                                                  Map<String, Object> keys)
                                                  throws IOException {
        if(!connect()) throw new IOException();
        return null;
    }
    
    public boolean insert(String db_name, Map<String, Object> data) 
                                          throws IOException {
        if(!connect()) throw new IOException();
        return true;
    }
    
    public boolean update(String db_name, Map<String, Object> data,
                                          Map<String, Object> keys)
                                          throws IOException {
        if(!connect()) throw new IOException();
        return true;
    }
}
