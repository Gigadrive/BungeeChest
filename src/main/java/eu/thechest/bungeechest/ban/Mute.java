package eu.thechest.bungeechest.ban;

import eu.thechest.bungeechest.mysql.MySQLManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Created by zeryt on 24.02.2017.
 */
public class Mute {
    private int id;
    private UUID uuid;
    private UUID uuidBannedBy;
    private String reason;
    private String nameWhenBanned;
    private String server;
    private Timestamp startDate;
    private Timestamp endDate;
    private boolean markedAsExpired = false;

    public Mute(UUID uuid){
        if(!BanUtilities.ACTIVE_MUTES.containsKey(uuid)){
            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `sn_mutes` WHERE `mutedPlayer`=? AND `active`=? ORDER BY `time` DESC LIMIT 1");
                ps.setString(1, uuid.toString());
                ps.setBoolean(2,true);

                ResultSet rs = ps.executeQuery();
                if(rs.first()){
                    this.id = rs.getInt("id");
                    this.uuid = uuid;
                    if(rs.getString("mutedBy") != null) this.uuidBannedBy = UUID.fromString(rs.getString("mutedBy"));
                    this.reason = rs.getString("reason");
                    this.nameWhenBanned = rs.getString("nameWhenMuted");
                    this.server = rs.getString("server");
                    this.startDate = rs.getTimestamp("time");
                    this.endDate = rs.getTimestamp("expiry");

                    if(!isExpired()){
                        BanUtilities.ACTIVE_MUTES.put(uuid, this);
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
        if(BanUtilities.ACTIVE_MUTES.containsValue(this)) BanUtilities.ACTIVE_MUTES.remove(getUUID());

        if(!markedAsExpired){
            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `sn_mutes` SET `active` = ? WHERE `id` = ?");
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

    public void markAsUnmuted(String unmutedBy){
        if(BanUtilities.ACTIVE_MUTES.containsValue(this)) BanUtilities.ACTIVE_MUTES.remove(getUUID());

        if(!markedAsExpired){
            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `sn_mutes` SET `active` = ?, `unmute.staff` = ?, `unmute.time` = CURRENT_TIMESTAMP WHERE `id` = ?");
                ps.setBoolean(1,false);
                ps.setString(2,unmutedBy);
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
