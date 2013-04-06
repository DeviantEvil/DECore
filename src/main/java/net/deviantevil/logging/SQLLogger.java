package net.deviantevil.logging;

import java.util.logging.Logger;

public class SQLLogger extends Logger {
    
    private SQLHandler mSqlHandler;

    public SQLLogger(String name, String resourceBundleName)  {
        super(name, resourceBundleName);
        mSqlHandler = new SQLHandler();
        this.addHandler(mSqlHandler);
    }

}
