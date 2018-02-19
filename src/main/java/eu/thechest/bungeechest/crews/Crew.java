package eu.thechest.bungeechest.crews;

import eu.thechest.bungeechest.BungeeChest;
import eu.thechest.bungeechest.mysql.MySQLManager;
import eu.thechest.bungeechest.user.BungeeUser;
import eu.thechest.bungeechest.user.PlayerUtilities;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by zeryt on 11.03.2017.
 */
public class Crew {
    public static HashMap<Integer,Crew> STORAGE = new HashMap<Integer,Crew>();

    public static Crew getCrew(int id){
        if(STORAGE.containsKey(id)){
            return STORAGE.get(id);
        } else {
            new Crew(id);

            if(STORAGE.containsKey(id)) {
                return STORAGE.get(id);
            } else{
                return null;
            }
        }
    }

    private int id;
    private String name;
    private String tag;
    private int startCoins;
    private int coins = 0;
    private UUID creator;
    private Timestamp timeCreated;
    private ArrayList<CrewMember> members;
    private ArrayList<String> invitedMembers;

    public Crew(int id){
        this.id = id;

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `crews` WHERE `id` = ?");
            ps.setInt(1,id);
            ResultSet rs = ps.executeQuery();

            if(rs.first()){
                this.name = rs.getString("name");
                this.tag = rs.getString("tag");
                this.startCoins = rs.getInt("coins");
                this.creator = UUID.fromString(rs.getString("creator"));
                this.timeCreated = rs.getTimestamp("time_created");
                this.invitedMembers = new ArrayList<String>();
                reloadMembers();
                STORAGE.put(id,this);
            }

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void reloadMembers() {
        BungeeChest.async(() -> {
            try {
                if(this.members == null){
                    this.members = new ArrayList<CrewMember>();
                } else {
                    this.members.clear();
                }

                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `crew_members` WHERE `crewID` = ?");
                ps.setInt(1,this.id);
                ResultSet rs = ps.executeQuery();
                rs.beforeFirst();

                while(rs.next()){
                    this.members.add(new CrewMember(UUID.fromString(rs.getString("uuid")),rs.getInt("crewID"),rs.getTimestamp("time_joined"),CrewRank.valueOf(rs.getString("rank"))));
                }

                MySQLManager.getInstance().closeResources(rs,ps);
            } catch(Exception e){
                e.printStackTrace();
            }
        });
    }

    public int getID(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }

    public String getTag(){
        return this.tag;
    }

    public void setTag(String tag){
        this.tag = tag;
    }

    public int getCoins(){
        return this.startCoins+this.coins;
    }

    public void addCoins(int coins){
        for(int i = 0; i < coins; i++){
            if((startCoins+this.coins+i)<0) break;

            this.coins++;
        }
    }

    public void reduceCoins(int coins){
        for(int i = 0; i < coins; i++){
            if((startCoins+this.coins+(i/-1))<0) break;

            this.coins--;
        }
    }

    public UUID getCreator(){
        return this.creator;
    }

    public Timestamp getTimeCreated(){
        return this.timeCreated;
    }

    public CrewMember getLeader(){
        for(CrewMember member : members){
            if(member.rank == CrewRank.LEADER) return member;
        }

        return null;
    }

    public boolean isCrewLeader(UUID uuid){
        return getLeader().uuid.toString().equals(uuid.toString());
    }

    public ArrayList<CrewMember> getMembers(){
        return this.members;
    }

    public CrewMember toCrewMember(UUID uuid){
        for(CrewMember member : getMembers()){
            if(member.uuid.toString().equals(uuid.toString())){
                return member;
            }
        }

        return null;
    }

    public boolean isMember(UUID uuid){
        return toCrewMember(uuid) != null;
    }

    public ArrayList<ProxiedPlayer> getOnlineMembers(){
        ArrayList<ProxiedPlayer> a = new ArrayList<ProxiedPlayer>();

        for(CrewMember m : getMembers()){
            ProxiedPlayer p = BungeeChest.getInstance().getProxy().getPlayer(m.uuid);

            if(p != null) a.add(p);
        }

        return a;
    }

    public ArrayList<String> getInvitedPlayers(){
        return this.invitedMembers;
    }

    public void joinCrew(ProxiedPlayer p){
        BungeeChest.async(() -> {
            if(!isMember(p.getUniqueId())){
                BungeeUser u = BungeeUser.getUser(p);

                if(u.getCrew() == null){
                    try {
                        PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `crew_members` (`uuid`,`crewID`) VALUES(?,?)");
                        ps.setString(1,p.getUniqueId().toString());
                        ps.setInt(2,this.id);
                        ps.execute();
                        ps.close();

                        getMembers().add(new CrewMember(p.getUniqueId(),this.id,new Timestamp(System.currentTimeMillis()),CrewRank.MEMBER));
                        u.updateCrew(this);

                        for(ProxiedPlayer all : getOnlineMembers()){
                            BungeeUser a = BungeeUser.getUser(all);

                            all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.GREEN + a.getTranslatedMessage("%p has joined the crew.").replace("%p",u.getRank().getColor() + p.getName() + ChatColor.GREEN)));
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void leaveCrew(UUID uuid){
        BungeeChest.async(() -> {
            if(isMember(uuid)){
                if(isCrewLeader(uuid)){
                    if(getMembers().size()-1 == 0){
                        disband();
                    } else {
                        // TODO: Pass ownership
                        disband();
                    }
                } else {
                    try {
                        PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("DELETE FROM `crew_members` WHERE `uuid` = ? AND `crewID` = ?");
                        ps.setString(1,uuid.toString());
                        ps.setInt(2,this.id);
                        ps.execute();
                        ps.close();

                        getMembers().remove(toCrewMember(uuid));

                        ProxiedPlayer p = BungeeChest.getInstance().getProxy().getPlayer(uuid);
                        if(p != null){
                            BungeeUser u = BungeeUser.getUser(p);
                            u.updateCrew(null);

                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("You have left the crew.")));
                        }

                        for(ProxiedPlayer all : getOnlineMembers()){
                            BungeeUser a = BungeeUser.getUser(all);

                            all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + a.getTranslatedMessage("%p has left the crew.").replace("%p", PlayerUtilities.getRankFromUUID(uuid).getColor() + PlayerUtilities.getNameFromUUID(uuid) + ChatColor.RED)));
                        }
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void disband(){
        STORAGE.remove(getID());

        BungeeChest.async(() -> {
            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("DELETE FROM `crews` WHERE `id` = ?");
                ps.setInt(1,getID());
                ps.execute();
                ps.close();

                ps = MySQLManager.getInstance().getConnection().prepareStatement("DELETE FROM `crew_members` WHERE `crewID` = ?");
                ps.setInt(1,getID());
                ps.execute();
                ps.close();

                for(ProxiedPlayer p : getOnlineMembers()){
                    BungeeUser u = BungeeUser.getUser(p);

                    u.updateCrew(null);
                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.DARK_RED + u.getTranslatedMessage("The crew has been disbanded.")));
                }

                getMembers().clear();
            } catch(Exception e){
                e.printStackTrace();
            }
        });
    }

    public void saveData(){
        BungeeChest.async(() -> {
            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `crews` SET `name` = ?, `tag` = ?, `coins`=`coins`+? WHERE `id` = ?");
                ps.setString(1,this.name);
                ps.setString(2,this.tag);
                ps.setInt(3,this.coins);
                ps.setInt(4,this.id);
                ps.executeUpdate();
                ps.close();
            } catch(Exception e){
                e.printStackTrace();
            }
        });
    }
}
