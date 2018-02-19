package eu.thechest.bungeechest.ban;

import eu.thechest.bungeechest.mysql.MySQLManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Created by zeryt on 24.02.2017.
 */
public class Ban {
    private int id;
    private UUID uuid;
    private UUID uuidBannedBy;
    private String reason;
    private String nameWhenBanned;
    private String server;
    private Timestamp startDate;
    private Timestamp endDate;
    private boolean markedAsExpired = false;

    public Ban(UUID uuid){
        if(!BanUtilities.ACTIVE_BANS.containsKey(uuid)){
            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `sn_bans` WHERE `bannedPlayer`=? AND `active`=? ORDER BY `time` DESC LIMIT 1");
                ps.setString(1, uuid.toString());
                ps.setBoolean(2,true);

                ResultSet rs = ps.executeQuery();
                if(rs.first()){
                    this.id = rs.getInt("id");
                    this.uuid = uuid;
                    if(rs.getString("bannedBy") != null) this.uuidBannedBy = UUID.fromString(rs.getString("bannedBy"));
                    this.reason = rs.getString("reason");
                    this.nameWhenBanned = rs.getString("nameWhenBanned");
                    this.server = rs.getString("server");
                    this.startDate = rs.getTimestamp("time");
                    this.endDate = rs.getTimestamp("expiry");

                    if(!isExpired()){
                        BanUtilities.ACTIVE_BANS.put(uuid, this);
                    } else {
                        markAsExpired();
                    }
                } else {
                    markedAsExpired = true;
                }

                MySQLManager.getInstance().closeResources(rs,ps);
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public boolean isExpired(){
        if(this.endDate == null){
            return false;
        } else {
            if(new Timestamp(System.currentTimeMillis()).after(this.endDate)){
                markAsExpired();
                return true;
            } else {
                return false;
            }
        }
    }

    public void markAsExpired(){
        if(BanUtilities.ACTIVE_BANS.containsValue(this)) BanUtilities.ACTIVE_BANS.remove(getUUID());

        if(!markedAsExpired){
            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `sn_bans` SET `active` = ? WHERE `id` = ?");
                ps.setBoolean(1,false);
                ps.setInt(2,this.id);
                ps.executeUpdate();
                ps.close();

                markedAsExpired = true;
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public void markAsUnbanned(String unbannedBy){
        if(BanUtilities.ACTIVE_BANS.containsValue(this)) BanUtilities.ACTIVE_BANS.remove(getUUID());

        if(!markedAsExpired){
            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `sn_bans` SET `active` = ?, `unban.staff` = ?, `unban.time` = CURRENT_TIMESTAMP WHERE `id` = ?");
                ps.setBoolean(1,false);
                ps.setString(2,unbannedBy);
                ps.setInt(3,this.id);
                ps.executeUpdate();
                ps.close();

                markedAsExpired = true;
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public int getID(){
        return this.id;
    }

    public UUID getUUID(){
        return this.uuid;
    }

    public UUID getUUIDBannedBy(){
        return this.uuidBannedBy;
    }

    public String getReason(){
        return this.reason;
    }

    public String getNameWhenBanned(){
        return this.nameWhenBanned;
    }

    public String getServer(){
        return this.server;
    }

    public Timestamp getStartDate(){
        return this.startDate;
    }

    public Timestamp getEndDate(){
        return this.endDate;
    }
}
