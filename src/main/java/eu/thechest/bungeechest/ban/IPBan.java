package eu.thechest.bungeechest.ban;

import eu.thechest.bungeechest.mysql.MySQLManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.UUID;

public class IPBan {
    private int id;
    private String ip;
    private String reason;
    private boolean active;
    private Timestamp time;
    private UUID bannedBy;
    private UUID unbanStaff;
    private Timestamp unbanTime;

    public IPBan(String ip){
        if(ip == null || ip.isEmpty()) return;
        ip = ip.trim();

        if(!BanUtilities.ACTIVE_IPBANS.containsKey(ip)){
            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `sn_ipBans` WHERE `ip` = ? AND `active` = ?");
                ps.setString(1,ip);
                ps.setBoolean(2,true);
                ResultSet rs = ps.executeQuery();

                if(rs.first()){
                    this.id = rs.getInt("id");
                    this.ip = rs.getString("ip");
                    this.reason = rs.getString("reason");
                    this.active = rs.getBoolean("active");
                    this.time = rs.getTimestamp("time");
                    if(rs.getString("bannedBy") != null) bannedBy = UUID.fromString(rs.getString("bannedBy"));
                    if(rs.getString("unban.staff") != null) unbanStaff = UUID.fromString(rs.getString("unban.staff"));
                    this.unbanTime = rs.getTimestamp("unban.time");

                    BanUtilities.ACTIVE_IPBANS.put(ip,this);
                }

                MySQLManager.getInstance().closeResources(rs,ps);
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public int getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public String getReason() {
        return reason;
    }

    public boolean isActive() {
        return active;
    }

    public Timestamp getTime() {
        return time;
    }

    public UUID getBannedBy() {
        return bannedBy;
    }

    public UUID getUnbanStaff() {
        return unbanStaff;
    }

    public Timestamp getUnbanTime() {
        return unbanTime;
    }
}
