package net.deviantevil.utilities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import net.deviantevil.decore.DEConfigOptions;
import net.deviantevil.decore.DECore;
import net.deviantevil.deplayer.DEPlayer;

public class DEPlayerDatabase extends DEDatabase
{

    public DEPlayerDatabase(SQLTable playertable)
    {
        super(playertable);
    }

    /** The SQL Columns registered for each property saved to SQL. */
    public static HashMap<DEPlayerSQLValue, Integer> SQLColumns = new HashMap<DEPlayerSQLValue, Integer>();

    public enum DEPlayerSQLValue
    {
        CONSOLE(Boolean.class, "Console"),
        CREATIVE_CURRENT(Boolean.class, "CreativeCurrent", "Currently in Creative"),
        CREATIVE_TIME_MONTH(Long.class, "CreativeTimeMonth", "Time spent in Creative this month"),
        CREATIVE_TIME_TOTAL(Long.class, "CreativeTimeTotal", "Time spent in Creative in total"),
        DEPOINTS(Long.class, "DEPoints", "DE Points (DEP)"),
        FLAGS(String.class, "Flags"),
        GODMODE(Boolean.class, "GodMode", "God Mode"),
        INFINITE_BUCKET(Boolean.class, "InfiniteBucket", "Infinite Bucket"),
        LAST_ONLINE_TIME(Long.class, "LastOnline", "Last Online Time"),
        MUTED(Boolean.class, "Muted"),
        PLAY_TIME_MONTH(Long.class, "PlayTimeMonth", "Time played this month"),
        PLAY_TIME_TOTAL(Long.class, "PlayTimeTotal", "Time played in total"),
        PLAYER_NAME(String.class, "Name", "Player Name"),
        RANK(String.class, "Rank", "Player's DERank"),
        REGISTERED_TIME(Long.class, "Registered", "Time first joined server"),
        VANISHED(Boolean.class, "Vanished"),
        VOTES_MONTH(Long.class, "VotesMonth", "Votes this month"),
        VOTES_TOTAL(Long.class, "VotesTotal", "Votes in total"),
        VOUCHES(String.class, "Vouches");

        /** Get an SQLValue instance from its SQL Name. Case insensitive. Returns null if not found. */
        public static final DEPlayerSQLValue getSQLValueFromColumn(int column)
        {
            for (DEPlayerSQLValue v : SQLColumns.keySet())
            {
                if (v.getColumn() == column)
                    return v;
            }
            return null;
        }

        /** Get an SQLValue instance from its SQL Name. Case insensitive. Returns null if not found. */
        public static final DEPlayerSQLValue matchValue(String SQLName)
        {
            for (DEPlayerSQLValue v : DEPlayerSQLValue.values())
            {
                if (v.getSQLName().equalsIgnoreCase(SQLName))
                    return v;
            }
            return null;
        }

        private final String SQLName, DisplayName;
        private final Class<?> SQLType;

        private DEPlayerSQLValue(Class<?> sqltype, String sqlname)
        {
            this.SQLType = sqltype;
            this.SQLName = this.DisplayName = sqlname;
        }

        private DEPlayerSQLValue(Class<?> sqltype, String sqlname, String displayname)
        {
            this.SQLType = sqltype;
            this.SQLName = sqlname;
            this.DisplayName = displayname;
        }

        /** Get the column this Property corresponds to in the MySQL table. Returns 0 if the property is not in the table. First column is "1" */
        public final int getColumn()
        {
            if (SQLColumns.containsKey(this))
            {
                return SQLColumns.get(this);
            }

            return 0;
        }

        /** Get the Display name for this value */
        public final String getDisplayName()
        {
            return this.DisplayName;
        }

        /** Get the SQL name for this value */
        public final String getSQLName()
        {
            return this.SQLName;
        }

        /** Get the type of this SQLValue as a class */
        public final Class<?> getValueType()
        {
            return this.SQLType;
        }

        @Override
        public String toString()
        {
            return this.SQLName;
        }
    }

    /** Attempt to populate the columns for the MySQL table. Return success. */
    public boolean populateColumnInformation()
    {
        if (super.getConnection() == null)
            return false;
        ResultSet RS = null;

        try
        {
            RS = super.getConnection().createStatement().executeQuery("SHOW FIELDS FROM " + this.table.getTablename());

            for (int column = 1; RS.next(); column++)
            {
                SQLColumns.put(DEPlayerSQLValue.matchValue(RS.getString(1)), column);

                if (DEConfigOptions.debugging)
                {
                    DECore.getDECore().getLogger().info("  MySQL value: " + RS.getString(1) + " -- " + column);
                }
            }
            RS.close();
            return !(SQLColumns.isEmpty());
        }
        catch (SQLException ex)
        {
            DECore.getDECore().getLogger().severe("Error in populating SQL column information: " + ex);
            try
            {
                if (RS != null)
                    RS.close();
            }
            catch (SQLException ex2)
            {
                //
            }
            return false;
        }
    }

    /** Change a given stat by an amount on the MySQL connection. */
    public void changeStat(String playername, String statToIncrement, long changeAmount)
    {
        super.write("UPDATE " + this.table.getTablename() + " SET " + statToIncrement + "= " + statToIncrement + " + ? WHERE " + DEPlayerSQLValue.PLAYER_NAME
                + "= ?", changeAmount, playername);
    }

    /** Change a given stat by an amount on the MySQL connection. */
    public void changeStat(String playername, String statToIncrement, double changeAmount)
    {
        super.write("UPDATE " + this.table.getTablename() + " SET " + statToIncrement + "= " + statToIncrement + " + ? WHERE " + DEPlayerSQLValue.PLAYER_NAME
                + "= ?", changeAmount, playername);
    }

    /** Do the interrogation of the MYSQL table to return a single result. Returns null if connection was not established. */
    public Object getPropertyFromMySQL(String playername, DEPlayerSQLValue sqlvalue)
    {
        if (!DECore.usingMySQL())
            return null;
        Object value = super.writeReturnFirstOnly("SELECT " + sqlvalue.getSQLName() + " FROM " + this.table.getTablename() + " WHERE "
                + DEPlayerSQLValue.PLAYER_NAME + "= ?", playername);
        if (DEConfigOptions.debugging)
        {
            DECore.getDECore().getLogger().info("Get property finished: " + sqlvalue.getSQLName() + ": " + value);
        }
        return value;
    }

    /** Do the interrogation of the MYSQL table to return results. Returns empty object array if connection was not established. */
    public HashMap<DEPlayerSQLValue, Object> getResults(String playername)
    {
        HashMap<DEPlayerSQLValue, Object> results = new HashMap<DEPlayerSQLValue, Object>();
        if (!DECore.usingMySQL())
            return results;
        HashMap<Integer, ArrayList<Object>> writeAndRead = super.writeAndRead("SELECT * FROM " + this.table.getTablename() + " WHERE "
                + DEPlayerSQLValue.PLAYER_NAME + "= ?", playername);
        if (writeAndRead != null)
        {
            for (int row : writeAndRead.keySet())
            {
                ArrayList<Object> columns = writeAndRead.get(row);
                for (int i = 0; i < columns.size(); i++)
                {
                    final int column = i + 1;
                    final Object ob = columns.get(i);
                    DEPlayerSQLValue v = DEPlayerSQLValue.getSQLValueFromColumn(column);

                    if (v == null)
                    {
                        DECore.getDECore().getLogger().warning("No SQLValue is programmed for column " + column);
                        continue;
                    }

                    if (DEConfigOptions.debugging)
                    {
                        DECore.getDECore().getLogger()
                                .info("Getting result for column " + column + " (" + v.getSQLName() + " of type " + v.getValueType().getSimpleName() + ").");
                    }

                    if (v.getValueType() == Boolean.class)
                    {
                        if (ob instanceof Boolean)
                        {
                            results.put(v, ob);
                        }
                        else if (ob instanceof Long || (ob instanceof Integer) || (ob instanceof Short))
                        {
                            results.put(v, 0 != (Integer) ob);
                        }
                        else
                        {
                            DECore.getDECore().getLogger().warning(ob.toString() + " is not a boolean or integer value for column " + column);
                        }
                    }
                    else if (v.getValueType() == Double.class)
                    {
                        if (ob instanceof Double || ob instanceof Long || ob instanceof Integer || ob instanceof Short)
                        {
                            results.put(v, ob);
                        }
                        else
                        {
                            DECore.getDECore().getLogger().warning(ob.toString() + " is not a double value for column " + column);
                        }
                    }
                    else if (v.getValueType() == Integer.class)
                    {
                        if (ob instanceof Long || ob instanceof Integer || ob instanceof Short)
                        {
                            results.put(v, ob);
                        }
                        else
                        {
                            DECore.getDECore().getLogger().warning(ob.toString() + " is not an integer value for column " + column);
                        }
                    }
                    else if (v.getValueType() == Long.class)
                    {
                        if (ob instanceof Long || ob instanceof Integer || ob instanceof Short)
                        {
                            results.put(v, ob);
                        }
                        else
                        {
                            DECore.getDECore().getLogger().warning(ob.toString() + " is not a long value for column " + column);
                        }
                    }
                    else if (v.getValueType() == Short.class)
                    {
                        if (ob instanceof Long || ob instanceof Integer || ob instanceof Short)
                        {
                            results.put(v, ob);
                        }
                        else
                        {
                            DECore.getDECore().getLogger().warning(ob.toString() + " is not a short value for column " + column);
                        }
                    }
                    else if (v.getValueType() == String.class)
                    {
                        if (ob instanceof String)
                        {
                            results.put(v, ob);
                        }
                        else
                        {
                            DECore.getDECore().getLogger().warning(ob.toString() + " is not a String value for column " + column);
                        }
                    }
                    else
                    {
                        results.put(v, ob);
                    }
                }
            }
            if (DEConfigOptions.debugging)
            {
                DECore.getDECore().getLogger().info("Get results finished:");
                for (DEPlayerSQLValue v : results.keySet())
                {
                    DECore.getDECore().getLogger().info(v.getSQLName() + " -- " + String.valueOf(results.get(v)));
                }
            }
        }
        else if (DEConfigOptions.debugging)
        {
            DECore.getDECore().getLogger().info("Get results finished. No results returned.");
        }
        return results;
    }

    /** Increment a given stat on the MySQL connection. */
    public void incrementStat(String playername, String statToIncrement)
    {
        changeStat(playername, statToIncrement, 1);
    }

    /** Load a DEPlayer from MySQL. Returns success */
    public boolean loadPlayer(DEPlayer deplayer)
    {
        final String playername = deplayer.getPlayername();

        if (DECore.usingMySQL())
        {
            try
            {
                HashMap<DEPlayerSQLValue, Object> results = getResults(playername);

                if (results.size() == 0)
                {
                    if (DEConfigOptions.debugging)
                    {
                        DECore.getDECore().getLogger().info(playername + " did not return any results. Assuming they are new. ");
                    }
                    DECore.getDECore().getServer()
                            .broadcastMessage(org.bukkit.ChatColor.GOLD + "Please welcome " + playername + " to " + DECore.SERVER_STRING + "!");
                    writeNewPlayerRow(playername);
                }
                else
                {
                    if (DEConfigOptions.debugging)
                    {
                        DECore.getDECore().getLogger().info("Analysing " + results.get(DEPlayerSQLValue.PLAYER_NAME));
                        for (DEPlayerSQLValue v : DEPlayerSQLValue.values())
                        {
                            DECore.getDECore().getLogger().info("  " + v.getSQLName() + ": " + results.get(v));
                        }
                    }
                    deplayer.setProperties(results);
                }
            }
            catch (Exception ex)
            {
                dumpException(ex);
                return false;
            }
        }
        return true;
    }

    /** Save a DEPlayer to MySQL. */
    public void save(DEPlayer p)
    {
        if (!DECore.usingMySQL() || p == null)
            return;
        final String playername = p.getPlayername();

        final long currentTime = System.currentTimeMillis();
        long creativeTime = p.getCreativeTime(currentTime);
        long loginTime = p.getPlayTime(currentTime);
        p.resetCreativeTime(currentTime);
        p.resetPlayTime(currentTime);

        for (DEPlayerSQLValue v : DEPlayerSQLValue.values())
        {
            switch (v)
            {
                case CONSOLE:
                case CREATIVE_CURRENT:
                case DEPOINTS:
                case GODMODE:
                case INFINITE_BUCKET:
                case MUTED:
                case PLAYER_NAME:
                case VANISHED:
                    writeValue(playername, v.getSQLName(), p.getProperty(v));
                    break;
                case RANK:
                    writeValue(playername, v.getSQLName(), p.getRank().name());
                    break;
                case LAST_ONLINE_TIME:
                    p.setProperty(v, currentTime, true);
                    break;
                case FLAGS:
                    if (p.vouchAndFlagData != null)
                        writeValue(playername, v.getSQLName(), Strings.stringArrayToString(p.vouchAndFlagData.getFlags()));
                    break;
                case VOUCHES:
                    if (p.vouchAndFlagData != null)
                        writeValue(playername, v.getSQLName(), Strings.stringArrayToString(p.vouchAndFlagData.getVouches()));
                    break;
                case CREATIVE_TIME_MONTH:
                case CREATIVE_TIME_TOTAL:
                    changeStat(playername, v.getSQLName(), creativeTime);
                    break;
                case PLAY_TIME_MONTH:
                case PLAY_TIME_TOTAL:
                    changeStat(playername, v.getSQLName(), loginTime);
                    break;
                default:
                    break; // default: Votes are handled separately. Registered time does not change.
            }
        }
    }

    /** Save all players on the server to MySQL. Returns success */
    public boolean saveAll()
    {
        if (!DECore.usingMySQL())
            return false;

        for (DEPlayer p : DECore.DEPlayers.values())
        {
            save(p);
        }
        return true;
    }

    /** Start of the month behaviour for DE. Returns if the actions were carried out. */
    public boolean startOfMonth(boolean force)
    {
        if (!super.isConnectionEstablished())
            return false;

        String checked = IO.getProperty(DECore.getDECore().getDataFolder().getPath(), "servertimes.properties", "mysqlmonthcheck");
        String month = String.valueOf(TimeParser.getMonth());
        if (checked == null || !month.equals(checked) || force)
        {
            writeValue("*", DEPlayerSQLValue.CREATIVE_TIME_MONTH.getSQLName(), 0);
            writeValue("*", DEPlayerSQLValue.VOTES_MONTH.getSQLName(), 0);
            writeValue("*", DEPlayerSQLValue.VOUCHES.getSQLName(), "''");
            writeValue("*", DEPlayerSQLValue.FLAGS.getSQLName(), "''");

            IO.saveProperty(DECore.getDECore().getDataFolder().getPath(), "servertimes.properties", "mysqlmonthcheck", String.valueOf(TimeParser.getMonth()));
            return true;
        }
        return false;
    }

    /** Write a new player row to the SQL Database */
    public boolean writeNewPlayerRow(String playername)
    {
        return super.writeNewRow(new String[] { DEPlayerSQLValue.PLAYER_NAME.getSQLName() }, playername);
    }

    /** Write a value on the MySQL connection. Can be Number or Boolean. Returns success. */
    public boolean writeValue(String playername, String columnToWriteTo, Object newValue)
    {
        if (newValue.getClass() == Boolean.class)
        {
            newValue = ((Boolean) newValue) ? 1 : 0;
        }
        return super.write("UPDATE " + this.table.getTablename() + " SET " + columnToWriteTo + " = ? WHERE " + DEPlayerSQLValue.PLAYER_NAME + "= ?", newValue,
                playername);
    }
}
