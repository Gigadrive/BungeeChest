package eu.thechest.bungeechest.party;

import eu.thechest.bungeechest.Queue;
import eu.thechest.bungeechest.QueueBlock;
import eu.thechest.bungeechest.cmd.QueueManager;
import eu.thechest.bungeechest.mysql.MySQLManager;
import eu.thechest.bungeechest.user.BungeeUser;
import eu.thechest.bungeechest.user.Rank;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by zeryt on 19.03.2017.
 */
public class Party {
    public static ArrayList<Party> STORAGE = new ArrayList<Party>();

    public static Party getParty(ProxiedPlayer p){
        for(Party party : STORAGE){
            if(party.getMembers().contains(p)){
                return party;
            }
        }

        return null;
    }

    public static boolean hasParty(ProxiedPlayer p){
        return getParty(p) != null;
    }

    public static ArrayList<Party> getRequestingParties(ProxiedPlayer p) {
        ArrayList<Party> a = new ArrayList<Party>();

        for (Party party : STORAGE) {
            if (party.getRequestedPlayers().contains(p)) {
                a.add(party);
            }
        }

        return a;
    }

    private int id;
    private ProxiedPlayer owner;
    private UUID creator;
    private ArrayList<ProxiedPlayer> members;
    private Timestamp creationDate;
    private ArrayList<ProxiedPlayer> requestedPlayers;

    public Party(ProxiedPlayer p){
        int insertID = -1;

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `parties` (`creator`,`leader`) VALUES(?,?);", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1,p.getUniqueId().toString());
            ps.setString(2,p.getUniqueId().toString());
            ps.execute();
            ResultSet rs = ps.getGeneratedKeys();
            if(rs.next()) insertID = rs.getInt(1);
            MySQLManager.getInstance().closeResources(rs,ps);

            if(insertID != -1){
                this.id = insertID;

                this.owner = p;
                this.creator = p.getUniqueId();
                this.members = new ArrayList<ProxiedPlayer>();
                this.creationDate = new Timestamp(System.currentTimeMillis());
                this.requestedPlayers = new ArrayList<ProxiedPlayer>();

                this.members.add(this.owner);

                STORAGE.add(this);
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public ProxiedPlayer getOwner(){
        return this.owner;
    }

    public UUID getCreator(){
        return this.creator;
    }

    public ArrayList<ProxiedPlayer> getMembers(){
        return this.members;
    }

    public Timestamp getCreationDate(){
        return this.creationDate;
    }

    public ArrayList<ProxiedPlayer> getRequestedPlayers(){
        return this.requestedPlayers;
    }

    public int getPartyLimit(){
        int i = 5;

        if(BungeeUser.getUser(getOwner()).hasPermission(Rank.PRO)) i = 7;
        if(BungeeUser.getUser(getOwner()).hasPermission(Rank.PRO_PLUS)) i = 9;
        if(BungeeUser.getUser(getOwner()).hasPermission(Rank.TITAN)) i = Integer.MAX_VALUE;

        return i;
    }

    public void joinParty(ProxiedPlayer p){
        BungeeUser u = BungeeUser.getUser(p);

        if(!hasParty(p)){
            if(getMembers().size() < getPartyLimit()){
                getMembers().add(p);

                for(ProxiedPlayer all : getMembers()){
                    all.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.GREEN + BungeeUser.getUser(all).getTranslatedMessage("%p has joined the party.").replace("%p",u.getRank().getColor() + p.getName() + ChatColor.GREEN)));
                }

                if(getQueue() != null) getQueue().leaveQueue(this);

                for(Party party : getRequestingParties(p)){
                    party.getRequestedPlayers().remove(p);
                }

                saveData();
            } else {
                p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.RED + u.getTranslatedMessage("The party is full.")));
            }
        }
    }

    public void leaveParty(ProxiedPlayer p){
        BungeeUser u = BungeeUser.getUser(p);

        if(hasParty(p) && getParty(p) == this){
            if(getOwner() == p){
                disband();
            } else {
                for(ProxiedPlayer all : getMembers()){
                    all.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.RED + BungeeUser.getUser(all).getTranslatedMessage("%p has left the party.").replace("%p",u.getRank().getColor() + p.getName() + ChatColor.RED)));
                }

                if(getQueue() != null) getQueue().leaveQueue(this);

                getMembers().remove(p);
                saveData();
            }
        }
    }

    public Queue getQueue(){
        for(Queue q : QueueManager.getQueues()){
            for(QueueBlock b : q.getBlocks()){
                if(b.party != null && b.party.equals(this)) return q;
            }
        }

        return null;
    }

    public void disband(){
        try {
            if(getQueue() != null) getQueue().leaveQueue(this);
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `parties` SET `status` = ? WHERE `id` = ?");
            ps.setString(1,"DISBANDED");
            ps.setInt(2,this.id);
            ps.executeUpdate();
            ps.close();

            for(ProxiedPlayer p : getMembers()){
                BungeeUser u = BungeeUser.getUser(p);

                p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.DARK_RED + u.getTranslatedMessage("The party has been disbanded.")));
            }

            getMembers().clear();
            STORAGE.remove(this);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void saveData(){
        try {
            if(this.id > 0){
                String mem = null;
                if(getMembers().size() > 0){
                    for(ProxiedPlayer p : getMembers()){
                        if(mem == null){
                            mem = p.getUniqueId().toString();
                        } else {
                            mem = mem + "," + p.getUniqueId().toString();
                        }
                    }
                }

                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `parties` SET `leader` = ?, `members` = ? WHERE `id` = ?");
                ps.setString(1,owner.getUniqueId().toString());
                ps.setString(2,mem);
                ps.setInt(3,this.id);
                ps.executeUpdate();
                ps.close();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
