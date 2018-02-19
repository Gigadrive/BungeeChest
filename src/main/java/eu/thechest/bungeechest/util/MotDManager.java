package eu.thechest.bungeechest.util;

import eu.thechest.bungeechest.mysql.MySQLManager;
import net.md_5.bungee.api.ChatColor;

import java.sql.Timestamp;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by zeryt on 18.02.2017.
 */
public class MotDManager {
    private static MotDManager instance;

    public static MotDManager getInstance(){
        if(instance == null) instance = new MotDManager();

        return instance;
    }

    private String firstLine;
    private String secondLine;
    private Timestamp lastUpdate;

    public MotDManager(){
        reload();
    }

    public void reload(){
        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `motd_manager` ORDER BY `time` DESC LIMIT 1");
            ResultSet rs = ps.executeQuery();

            if(rs.first()){
                this.firstLine = ChatColor.translateAlternateColorCodes('&', rs.getString("first_line"));
                this.secondLine = ChatColor.translateAlternateColorCodes('&', rs.getString("second_line"));
                this.lastUpdate = rs.getTimestamp("time");
            }

            ps.close();
            rs.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public String getMessageOfTheDay(){
        if(firstLine == null || secondLine == null){
            return ChatColor.GOLD + "TheChest.eu";
        } else {
            return firstLine + "\n" + secondLine;
        }
    }
}
