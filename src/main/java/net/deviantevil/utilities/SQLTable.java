package net.deviantevil.utilities;

public class SQLTable {
    private final String URL;
    private final String Tablename;
    private final String Username;
    private final String Password;

    public SQLTable(String URL, String Tablename, String Username, String Password) {
        this.URL = URL;
        this.Tablename = Tablename;
        this.Username = Username;
        this.Password = Password;
    }

    public String getURL() {
        return this.URL;
    }

    public String getTablename() {
        return this.Tablename;
    }

    public String getUsername() {
        return this.Username;
    }
    public String getPassword() {
        return this.Password;
    }
}