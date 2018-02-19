package eu.thechest.bungeechest.user;

import eu.thechest.bungeechest.BungeeChest;
import eu.thechest.bungeechest.Queue;
import eu.thechest.bungeechest.QueueBlock;
import eu.thechest.bungeechest.Translation;
import eu.thechest.bungeechest.cmd.QueueManager;
import eu.thechest.bungeechest.crews.Crew;
import eu.thechest.bungeechest.mysql.MySQLManager;
import eu.thechest.bungeechest.util.ChatChannel;
import eu.thechest.bungeechest.util.DefaultFontInfo;
import eu.thechest.bungeechest.util.StoredChatMessage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;

import javax.jws.soap.SOAPBinding;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by zeryt on 18.02.2017.
 */
public class BungeeUser {
    public static HashMap<ProxiedPlayer,BungeeUser> STORAGE = new HashMap<ProxiedPlayer,BungeeUser>();

    public static BungeeUser getUser(ProxiedPlayer p){
        if(STORAGE.containsKey(p)){
            return STORAGE.get(p);
        } else {
            new BungeeUser(p);

            if(STORAGE.containsKey(p)) {
                return STORAGE.get(p);
            } else {
                return null;
            }
        }
    }

    private ProxiedPlayer p;
    private Rank rank = Rank.USER;
    private ArrayList<String> friends;
    private ArrayList<String> friendRequests;
    private ArrayList<StoredChatMessage> storedChatMessages;
    private Translation currentLang;
    private Crew crew;
    private ArrayList<Integer> achieved;
    private ChatChannel chatChannel;

    public ProxiedPlayer lastMsg;
    public int filteredMessages = 0;

    public boolean setting_friendRequests;
    public boolean setting_privateMessages;
    public boolean setting_partyRequests;
    public boolean setting_headSeat;
    public boolean setting_lobbySpeed;
    public boolean setting_autoNick;

    public String country;
    public ArrayList<String> reported;

    public BungeeUser(ProxiedPlayer p){
        if(STORAGE.containsKey(p)) return;

        this.p = p;
        this.friends = new ArrayList<String>();
        this.friendRequests = new ArrayList<String>();
        this.storedChatMessages = new ArrayList<StoredChatMessage>();
        this.achieved = new ArrayList<Integer>();
        this.reported = new ArrayList<String>();

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `users` WHERE `uuid` = ?");
            ps.setString(1,p.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();

            if(rs.first()){
                this.rank = Rank.valueOf(rs.getString("rank"));
                this.currentLang = Translation.getLanguage(rs.getString("language"));
                this.setting_friendRequests = rs.getBoolean("setting_friendRequests");
                this.setting_privateMessages = rs.getBoolean("setting_privateMessages");
                this.setting_partyRequests = rs.getBoolean("setting_partyRequests");
                this.setting_headSeat = rs.getBoolean("setting_headSeat");
                this.setting_lobbySpeed = rs.getBoolean("setting_lobbySpeed");
                this.setting_autoNick = rs.getBoolean("setting_autoNick");
                this.chatChannel = ChatChannel.valueOf(rs.getString("chatChannel"));

                BungeeChest.async(() -> {
                    fetchFriends();
                    fetchFriendRequests();
                    fetchCrew();
                });

                STORAGE.put(p,this);
            } else {
                PreparedStatement insert = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `users` (`uuid`,`username`,`playtime`) VALUES(?,?,?)");
                insert.setString(1,p.getUniqueId().toString());
                insert.setString(2,p.getName());
                insert.setLong(3,0);
                if(insert.execute()){
                    new BungeeUser(p);
                }
            }

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public ProxiedPlayer getProxiedPlayer(){
        return this.p;
    }

    public ArrayList<StoredChatMessage> getStoredChatMessages(){
        return this.storedChatMessages;
    }

    public void updateSettings(){
        new Thread(() -> {
            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `users` WHERE `uuid` = ?");
                ps.setString(1,p.getUniqueId().toString());
                ResultSet rs = ps.executeQuery();

                if(rs.first()){
                    this.currentLang = Translation.getLanguage(rs.getString("language"));
                    this.setting_friendRequests = rs.getBoolean("setting_friendRequests");
                    this.setting_privateMessages = rs.getBoolean("setting_privateMessages");
                    this.setting_partyRequests = rs.getBoolean("setting_partyRequests");
                    this.setting_headSeat = rs.getBoolean("setting_headSeat");
                    this.setting_lobbySpeed = rs.getBoolean("setting_lobbySpeed");
                    this.setting_autoNick = rs.getBoolean("setting_autoNick");
                }

                MySQLManager.getInstance().closeResources(rs,ps);
            } catch(Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    @Deprecated
    public void saveCountry(){
        saveData();
    }

    public ChatChannel getChatChannel(){
        return this.chatChannel;
    }

    public void setChatChannel(ChatChannel c){
        this.chatChannel = c;
    }

    public void saveData(){
        BungeeChest.async(() -> {
            if(country == null || country.isEmpty()){
                try {
                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `users` SET `chatChannel` = ? WHERE `uuid` = ?");
                    ps.setString(1,chatChannel.toString());
                    ps.setString(2,p.getUniqueId().toString());
                    ps.executeUpdate();
                    ps.close();
                } catch(Exception e){
                    e.printStackTrace();
                }
            } else {
                try {
                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `users` SET `country` = ?, `chatChannel` = ? WHERE `uuid` = ?");
                    ps.setString(1,country);
                    ps.setString(2,chatChannel.toString());
                    ps.setString(3,p.getUniqueId().toString());
                    ps.executeUpdate();
                    ps.close();
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public void fetchFriends(){
        if(this.friends == null){
            this.friends = new ArrayList<String>();
        } else {
            this.friends.clear();
        }

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `friendships` WHERE `player` = ? OR `friend` = ?");
            ps.setString(1,p.getUniqueId().toString());
            ps.setString(2,p.getUniqueId().toString());

            ResultSet rs = ps.executeQuery();
            rs.beforeFirst();

            while(rs.next()){
                if(rs.getString("player").equals(p.getUniqueId().toString())){
                    friends.add(rs.getString("friend"));
                } else if(rs.getString("friend").equals(p.getUniqueId().toString())){
                    friends.add(rs.getString("player"));
                }
            }

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void fetchFriendRequests(){
        if(this.friendRequests == null){
            this.friendRequests = new ArrayList<String>();
        } else {
            this.friendRequests.clear();
        }

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `friend_requests` WHERE `toUUID` = ?");
            ps.setString(1,p.getUniqueId().toString());

            ResultSet rs = ps.executeQuery();
            rs.beforeFirst();

            while(rs.next()){
                friendRequests.add(rs.getString("fromUUID"));
            }

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void fetchCrew(){
        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `crew_members` WHERE `uuid` = ?");
            ps.setString(1,p.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();

            if(rs.first()){
                this.crew = Crew.getCrew(rs.getInt("crewID"));
            }

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public Crew getCrew(){
        return this.crew;
    }

    public void updateCrew(Crew c){
        this.crew = c;
    }

    public Translation getLanguage(){
        return this.currentLang;
    }

    public ArrayList<String> getFriends(){
        return this.friends;
    }

    public ArrayList<String> getFriendRequests(){
        return this.friendRequests;
    }

    public String getTranslatedMessage(String originalMessage){
        Translation currentLang = this.currentLang;

        if(currentLang != null && currentLang != Translation.getLanguage("EN")){
            if(currentLang.getPhrases().containsKey(originalMessage)){
                return ChatColor.translateAlternateColorCodes('&',currentLang.getPhrases().get(originalMessage));
            } else {
                return ChatColor.translateAlternateColorCodes('&',originalMessage);
            }
        } else {
            return ChatColor.translateAlternateColorCodes('&',originalMessage);
        }
    }

    public String getTranslatedMessage(String originalMessage, Object... format){
        return String.format(getTranslatedMessage(originalMessage), format);
    }

    public void updateDisplayName(){
        p.setDisplayName(BungeeChest.limitString(getRank().getColor() + p.getName(),16));
    }

    public Queue getQueue(){
        for(Queue q : QueueManager.getQueues()){
            for(QueueBlock b : q.getBlocks()){
                if(b.p.contains(p)) return q;
            }
        }

        return null;
    }

    /*public void sendCenteredMessage(String message){
        ProxiedPlayer player = this.p;
        int CENTER_PX = 154;

        if(message == null || message.equals("")) player.sendMessage(TextComponent.fromLegacyText(""));
        message = ChatColor.translateAlternateColorCodes('&', message);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for(char c : message.toCharArray()){
            if(c == ChatColor.COLOR_CHAR){
                previousCode = true;
                continue;
            }else if(previousCode == true){
                previousCode = false;
                if(c == 'l' || c == 'L'){
                    isBold = true;
                    continue;
                }else isBold = false;
            }else{
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while(compensated < toCompensate){
            sb.append(" ");
            compensated += spaceLength;
        }
        player.sendMessage(TextComponent.fromLegacyText(sb.toString() + message));
    }*/
    public void sendCenteredMessage(String message){
        ProxiedPlayer player = p;
        int CENTER_PX = 154;
        int MAX_PX = 250;

        message = ChatColor.translateAlternateColorCodes('&', message);
        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;
        int charIndex = 0;
        int lastSpaceIndex = 0;
        String toSendAfter = null;
        String recentColorCode = "";
        for(char c : message.toCharArray()){
            if(c == '§'){
                previousCode = true;
                continue;
            }else if(previousCode == true){
                previousCode = false;
                recentColorCode = "§" + c;
                if(c == 'l' || c == 'L'){
                    isBold = true;
                    continue;
                }else isBold = false;
            }else if(c == ' ') lastSpaceIndex = charIndex;
            else{
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
            if(messagePxSize >= MAX_PX){
                toSendAfter = recentColorCode + message.substring(lastSpaceIndex + 1, message.length());
                message = message.substring(0, lastSpaceIndex + 1);
                break;
            }
            charIndex++;
        }
        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while(compensated < toCompensate){
            sb.append(" ");
            compensated += spaceLength;
        }
        player.sendMessage(TextComponent.fromLegacyText(sb.toString() + message));
        if(toSendAfter != null) sendCenteredMessage(toSendAfter);
    }

    public Rank getRank(){
        return this.rank;
    }

    public boolean hasPermission(Rank minRank){
        if(minRank == null){
            return true;
        } else {
            return rank.getID() >= minRank.getID();
        }
    }

    public void updateRank(Rank rank){
        PlayerUtilities.setRank(p.getUniqueId(), rank);
        this.rank = rank;
        updateDisplayName();
    }

    public void updateRank(Rank rank, boolean updateMySQL){
        if(updateMySQL){
            updateRank(rank);
        } else {
            this.rank = rank;
        }
    }

    public void achieve(int achievementID){
        if(achieved.contains(achievementID)) return;

        try {
            if(p.getServer() != null){
                ServerInfo server = p.getServer().getInfo();

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(stream);

                out.writeUTF("giveAchievement");
                out.writeUTF(p.getName() + ":" + achievementID);
                server.sendData("BungeeCord",stream.toByteArray());

                achieved.add(achievementID);
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
