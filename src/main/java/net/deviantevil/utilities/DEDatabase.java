package net.deviantevil.utilities;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

import net.deviantevil.decore.DEConfigOptions;
import net.deviantevil.decore.DECore;

/** DEDatabase stores functions for MySQL and its database. With thanks to DeityAPI; alterations have been made. */
public abstract class DEDatabase
{

    protected SQLTable table;
    private Connection conn;

    public DEDatabase(SQLTable table)
    {
        this.table = table;

        try
        {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            this.connect();
        }
        catch (Exception ex)
        {
            DECore.getDECore().getLogger().severe("Failed to initialize JDBC driver. " + ex);
            return;
        }
    }

    /** Connects to the database. Dumps errors to log. Returns success. */
    private boolean connect()
    {
        return connect(true);
    }

    /** Connects to the database. Optionally dump errors. Returns success. */
    private boolean connect(boolean dumpErrors)
    {
        try
        {
            this.conn = DriverManager.getConnection(this.table.getURL(), this.table.getUsername(), this.table.getPassword());
            return true;
        }
        catch (SQLException e)
        {
            if (dumpErrors)
                dumpSqlException(e, "Failed to connect to a MySQL table. Have you updated the config? [Table " + tableName() + "]");

            return false;
        }
    }

    /**
     * Dumps an SQLException with some debug info
     * 
     * @param ex
     */
    protected static void dumpSqlException(SQLException ex)
    {
        DECore.getDECore().getLogger().severe("SQLException: " + ex.getMessage());
        DECore.getDECore().getLogger().severe("SQLState: " + ex.getSQLState());
        DECore.getDECore().getLogger().severe("VendorError: " + ex.getErrorCode());
        ex.printStackTrace();
    }

    /**
     * Dumps an Exception with some debug info
     * 
     * @param ex
     */
    protected static void dumpException(Exception ex)
    {
        DECore.getDECore().getLogger().severe("Exception: " + ex.getMessage());
        ex.printStackTrace();
    }

    /**
     * Dumps an SQLException with some actual debug info
     * 
     * @param ex
     */
    private static void dumpSqlException(SQLException ex, String extra)
    {
        DECore.getDECore().getLogger().severe(extra);
        dumpSqlException(ex);
    }

    /**
     * Verifies connection to the database
     */
    public boolean isConnectionEstablished()
    {
        if (this.conn == null)
            return false;
        try
        {
            if (!this.conn.isValid(5))
                return this.connect(false);

            return true;
        }
        catch (SQLException ex)
        {
            return false;
        }
    }

    /**
     * Returns the connection
     */
    public Connection getConnection()
    {
        return this.conn;
    }

    /**
     * Prepares an SQL Statement to be sent
     * 
     * @param sql
     * @param params
     * @return
     * @throws SQLException
     */
    private PreparedStatement prepareSqlStatement(String sql, Object[] params) throws SQLException
    {
        PreparedStatement stmt = this.conn.prepareStatement(sql);

        int counter = 0;

        for (Object param : params)
        {
            ++counter;
            if (param instanceof Long)
            {
                stmt.setLong(counter, (Long) param);
            }
            else if (param instanceof Integer)
            {
                stmt.setInt(counter, (Integer) param);
            }
            else if (param instanceof Short)
            {
                stmt.setShort(counter, (Short) param);
            }
            else if (param instanceof Double)
            {
                stmt.setDouble(counter, (Double) param);
            }
            else if (param instanceof String)
            {
                stmt.setString(counter, (String) param);
            }
            else if (param == null)
            {
                stmt.setNull(counter, Types.NULL);
            }
            else
            {
                stmt.setObject(counter, param);
            }
        }
        if (DEConfigOptions.debugging)
        {
            DECore.getDECore().getLogger().info("Preparing SQL: " + stmt.toString());
        }

        return stmt;
    }

    /**
     * Writes a query and returns a hashmap of results. Returns null if the query failed.
     * @param sql The SQL String
     * @param params The prepared parameters to replace the SQL ?
     * @return A hashmap of results or null where the Integer keys are the ROWS and the List of Objects are its COLUMNS.
     */
    public HashMap<Integer, ArrayList<Object>> writeAndRead(String sql, Object... params)
    {
        if (!this.isConnectionEstablished())
        {
            DECore.log.warning("Could not connect to the database " + tableName());
            return null;
        }

        HashMap<Integer, ArrayList<Object>> Rows = new HashMap<Integer, ArrayList<Object>>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = this.prepareSqlStatement(sql, params);
            if (stmt.executeQuery() != null)
            {
                rs = stmt.getResultSet();
                while (rs.next())
                {
                    ArrayList<Object> Col = new ArrayList<Object>();
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++)
                    {
                        Col.add(rs.getObject(i));
                    }
                    Rows.put(rs.getRow(), Col);
                }
            }
        }
        catch (SQLException ex)
        {
            DEDatabase.dumpSqlException(ex);
        }
        finally
        {
            if (rs != null)
            {
                try
                {
                    rs.close();
                }
                catch (SQLException sqlEx)
                { //
                }
                rs = null;
            }
            if (stmt != null)
            {
                try
                {
                    stmt.close();
                }
                catch (SQLException sqlEx)
                { //
                }
                stmt = null;
            }
            if (Rows.isEmpty())
            {
                return null;
            }
        }
        return Rows;
    }

    /**
     * Returns a formatted table name in form URL.tablename
     */
    public String tableName()
    {
        return this.table.getURL() + "." + this.table.getTablename();
    }

    /**
     * Writes a query to the database. Dumps errors. Returns success.
     */
    public boolean write(String sql, Object... params)
    {
        if (!this.isConnectionEstablished())
        {
            DECore.log.warning("Could not connect to the database " + tableName());
            return false;
        }

        try
        {
            PreparedStatement stmt = this.prepareSqlStatement(sql, params);
            stmt.execute();
            stmt.close();
            return true;
        }
        catch (SQLException ex)
        {
            DEDatabase.dumpSqlException(ex);
            return false;
        }
    }

    /**
     * Writes a query to the database. Dumps errors. Returns the first column result only or null.
     */
    public Object writeReturnFirstOnly(String sql, Object... params)
    {
        if (!this.isConnectionEstablished())
        {
            DECore.log.warning("Could not connect to the database " + tableName());
            return false;
        }

        Object ob = null;
        try
        {
            PreparedStatement stmt = this.prepareSqlStatement(sql, params);
            stmt.execute();
            ResultSet RS = stmt.getResultSet();
            if (!RS.isAfterLast())
            {
                RS.next();
                ob = RS.getObject(1);
            }
            RS.close();
            stmt.close();
        }
        catch (SQLException ex)
        {
            DEDatabase.dumpSqlException(ex);
        }

        return ob;
    }

    /**
     * Writes a query to the database. Hides console output. Returns success.
     */
    public boolean writeNoError(String sql, Object... params)
    {
        if (!this.isConnectionEstablished())
        {
            DECore.log.warning("Could not connect to the database " + tableName());
            return false;
        }

        try
        {
            PreparedStatement stmt = this.prepareSqlStatement(sql, params);
            stmt.execute();
            stmt.close();
            return true;
        }
        catch (SQLException ex)
        {
            return false;
        }
    }

    /** Write a new row into the table MySQL connection. Returns success. Returns false if no columns to write were specified. */
    public boolean writeNewRow(String[] columns, Object... values)
    {
        String columnsToWrite = Strings.stringArrayToString(columns, ", ");
        if (Strings.isNullOrEmpty(columnsToWrite))
            return false;

        if (!this.isConnectionEstablished())
        {
            DECore.log.warning("Could not connect to the database " + tableName());
            return false;
        }

        try
        {
            PreparedStatement stmt = this.prepareSqlStatement("INSERT INTO " + this.table.getTablename() + " (" + columnsToWrite + ") VALUES (?)", values);
            stmt.execute();
            stmt.close();
            return true;
        }
        catch (SQLException ex)
        {
            DEDatabase.dumpSqlException(ex);
            return false;
        }
    }
}
