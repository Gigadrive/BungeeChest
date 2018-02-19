package eu.thechest.bungeechest.mysql;

import eu.thechest.bungeechest.BungeeChest;

import java.sql.*;

/**
 * Created by zeryt on 18.02.2017.
 */
public class MySQLManager {
    private static MySQLManager instance;
    private static Connection con;

    private static String host;
    private static String user;
    private static String password;
    private static String database;
    private static int port;

    public static MySQLManager getInstance(){
        return instance;
    }

    static {
        instance = new MySQLManager();
    }

    public MySQLManager(){
        load();
    }

    public Connection getConnection(){
        checkConnection();

        return this.con;
    }

    public void loadDataFromConfig(){
        this.host = BungeeChest.getInstance().getConfig().getString("mysql.host");
        this.user = BungeeChest.getInstance().getConfig().getString("mysql.user");
        this.password = BungeeChest.getInstance().getConfig().getString("mysql.password");
        this.database = BungeeChest.getInstance().getConfig().getString("mysql.database");
        this.port = BungeeChest.getInstance().getConfig().getInt("mysql.port");
    }

    public void load(){
        loadDataFromConfig();
        openConnection();
    }

    private void openConnection(){
        try {
            System.out.println("[MySQL] Loading driver..");
            Class.forName("org.gjt.mm.mysql.Driver").newInstance();
        } catch(Exception e){
            System.err.println("[MySQL] Unable to load driver.");
            e.printStackTrace();
        }

        try {
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
            con = DriverManager.getConnection(url, user, password);
        } catch(SQLException sqle){
            System.out.println("SQLException: " + sqle.getMessage());
            System.out.println("SQLState: " + sqle.getSQLState());
            System.out.println("VendorError: " + sqle.getErrorCode());
            sqle.printStackTrace();
        }
    }

    private void checkConnection() {
        try {
            if(!(this.con.isValid(2))){
                openConnection();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void unload(){
        try {
            if(con != null && this.con.isValid(2)){
                try {
                    con.close();
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void closeResources(ResultSet rs, PreparedStatement ps){
        try {
            if(rs != null) rs.close();
            if(ps != null) ps.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
