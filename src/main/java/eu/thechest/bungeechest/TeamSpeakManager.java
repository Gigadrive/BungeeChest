package eu.thechest.bungeechest;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.TextMessageTargetMode;
import com.github.theholywaffle.teamspeak3.api.event.ClientJoinEvent;
import com.github.theholywaffle.teamspeak3.api.event.TS3EventAdapter;
import com.github.theholywaffle.teamspeak3.api.event.TS3EventType;
import com.github.theholywaffle.teamspeak3.api.event.TextMessageEvent;
import com.github.theholywaffle.teamspeak3.api.exception.TS3ConnectionFailedException;
import com.github.theholywaffle.teamspeak3.api.reconnect.ConnectionHandler;
import com.github.theholywaffle.teamspeak3.api.reconnect.ReconnectStrategy;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerGroup;
import com.sun.corba.se.spi.activation.Server;
import eu.thechest.bungeechest.mysql.MySQLManager;
import eu.thechest.bungeechest.user.PlayerUtilities;
import eu.thechest.bungeechest.user.Rank;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by zeryt on 02.03.2017.
 */
public class TeamSpeakManager {
    static {
        instance = new TeamSpeakManager();
    }

    private static TeamSpeakManager instance;
    private TS3Config ts3Config;
    private TS3Query ts3Query;
    private TS3Api ts3Api;

    private HashMap<Rank,Integer> rankGroups;
    private int verifiedGroup;

    public static TeamSpeakManager getInstance(){
        return instance;
    }

    private void connectedVoid(){
        ts3Api = ts3Query.getApi();
        ts3Api.login("***********","*************");

        ts3Api.selectVirtualServerById(1);
        ts3Api.setNickname("TheChest TS Verify (" + new Random().nextInt(9999) + ")");

        ts3Api.registerAllEvents();
    }

    public void connect(){
        try {
            ts3Config = new TS3Config();
            ts3Config.setHost("thechest.eu");
            ts3Config.setQueryPort(10011);
            ts3Config.setDebugLevel(Level.OFF);
            ts3Config.setReconnectStrategy(ReconnectStrategy.exponentialBackoff());

            ts3Config.setConnectionHandler(new ConnectionHandler() {
                @Override
                public void onConnect(TS3Query ts3Query) {
                    connectedVoid();
                }

                @Override
                public void onDisconnect(TS3Query ts3Query) {

                }
            });

            ts3Query = new TS3Query(this.ts3Config);
            ts3Query.connect();

            rankGroups = new HashMap<Rank,Integer>();
            for(ServerGroup serverGroup : ts3Api.getServerGroups()){
                if(serverGroup.getId() == 18){
                    rankGroups.put(Rank.ADMIN,serverGroup.getId());
                } else if(serverGroup.getId() == 17){
                    rankGroups.put(Rank.CM,serverGroup.getId());
                } else if(serverGroup.getId() == 16){
                    rankGroups.put(Rank.SR_MOD,serverGroup.getId());
                } else if(serverGroup.getId() == 15){
                    rankGroups.put(Rank.MOD,serverGroup.getId());
                } else if(serverGroup.getId() == 14){
                    rankGroups.put(Rank.BUILD_TEAM,serverGroup.getId());
                } else if(serverGroup.getId() == 13){
                    rankGroups.put(Rank.STAFF,serverGroup.getId());
                } else if(serverGroup.getId() == 12){
                    rankGroups.put(Rank.VIP,serverGroup.getId());
                } else if(serverGroup.getId() == 11){
                    rankGroups.put(Rank.TITAN,serverGroup.getId());
                } else if(serverGroup.getId() == 10){
                    rankGroups.put(Rank.PRO_PLUS,serverGroup.getId());
                } else if(serverGroup.getId() == 9){
                    rankGroups.put(Rank.PRO,serverGroup.getId());
                } else if(serverGroup.getId() == 24){
                    verifiedGroup = serverGroup.getId();
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            ts3Query.exit();
        }catch (TS3ConnectionFailedException e) {
            e.printStackTrace();
        }
    }

    public Client getClient(String teamspeakId) {
        // Get all clients
        for (Client c : this.ts3Api.getClients()) {
            if(c.getUniqueIdentifier().equals(teamspeakId)) {
                return c;
            }
        }
        return null;
    }

    public ServerGroup getServerGroupById(int i){
        for(ServerGroup s : ts3Api.getServerGroups()){
            if(s.getId() == i) return s;
        }

        return null;
    }

    public void sendMessage(String teamspeakUUID, String message) {
        Client client = getClient(teamspeakUUID);
        ts3Api.sendPrivateMessage(client.getId(), message);
    }


    public void giveRank(String teamspeakId, Rank rank){
        Client c = getClient(teamspeakId);

        if(c != null){
            for(int i : c.getServerGroups()){
                if(i != 6){
                    ServerGroup s = getServerGroupById(i);

                    if(s != null){
                        ts3Api.removeClientFromServerGroup(s,c);
                    }
                }
            }

            ts3Api.addClientToServerGroup(verifiedGroup,c.getDatabaseId());
            if(rankGroups.containsKey(rank)){
                ts3Api.addClientToServerGroup(rankGroups.get(rank),c.getDatabaseId());
            }
        }
    }

    public void takeRanks(String teamSpeakId){
        Client c = getClient(teamSpeakId);

        if(c != null){
            for(int i : c.getServerGroups()){
                if(i != 6){
                    ServerGroup s = getServerGroupById(i);

                    if(s != null){
                        ts3Api.removeClientFromServerGroup(s,c);
                    }
                }
            }
        }
    }

    public boolean isVerified(UUID uuid, String teamspeakId) {
        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT minecraft_uuid, teamspeak_uuid FROM teamspeak_verify WHERE minecraft_uuid=? AND teamspeak_uuid=?");
            ps.setString(1, uuid.toString());
            ps.setString(2, teamspeakId);
            ResultSet rs = ps.executeQuery();
            if(!rs.first()) {
                // is not already verified
                return false;
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean isVerified(String teamspeakId) {
        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT minecraft_uuid, teamspeak_uuid FROM teamspeak_verify WHERE teamspeak_uuid=?");
            ps.setString(1, teamspeakId);
            ResultSet rs = ps.executeQuery();
            if(!rs.first()) {
                // is not already verified
                return false;
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean isOnline(String teamspeakId) {
        if(this.ts3Api == null) this.ts3Api = ts3Query.getApi();
        // Get all clients
        for (Client c : this.ts3Api.getClients()) {
            if(c.getUniqueIdentifier().equals(teamspeakId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInVerifyProcess(UUID minecraftUUID) {
        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT minecraft_uuid FROM teamspeak_verify WHERE minecraft_uuid=? AND confirmed=?");
            ps.setString(1, minecraftUUID.toString());
            ps.setBoolean(2, false);
            ResultSet rs = ps.executeQuery();
            if(!rs.next())
                return false;
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean isInVerifyProcess(String teamspeakUUID) {
        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT teamspeak_uuid FROM teamspeak_verify WHERE teamspeak_uuid=? AND confirmed=?");
            ps.setString(1, teamspeakUUID);
            ps.setBoolean(2, false);
            ResultSet rs = ps.executeQuery();
            if(!rs.next())
                return false;
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void setVerified(UUID minecraftUUID, String teamspeakUUID) {
        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE teamspeak_verify SET confirmed=? WHERE minecraft_uuid=? AND teamspeak_uuid=?");
            ps.setBoolean(1, true);
            ps.setString(2, minecraftUUID.toString());
            ps.setString(3, teamspeakUUID);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeTSVerificationFromDatabase(String teamspeakUUID) {
        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("DELETE FROM teamspeak_verify WHERE teamspeak_uuid=?");
            ps.setString(1, teamspeakUUID);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getTeamspeakIdentities(UUID minecraftUUID) {
        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT teamspeak_uuid FROM teamspeak_verify WHERE minecraft_uuid=?");
            ps.setString(1, minecraftUUID.toString());
            ResultSet rs = ps.executeQuery();
            List<String> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rs.getString("teamspeak_uuid"));
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public UUID getUUIDFromTeamSpeakID(String tsID){
        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT minecraft_uuid FROM teamspeak_verify WHERE teamspeak_uuid=?");
            ps.setString(1,tsID);
            ResultSet rs = ps.executeQuery();
            if(rs.first()){
                return UUID.fromString(rs.getString("minecraft_uuid"));
            }
        } catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public void giveIcon(String tsID,UUID uuid){
        Client c = getClient(tsID);
        if(c == null) return;

        Rank rank = PlayerUtilities.getRankFromUUID(uuid);

        if(rank.getID() >= Rank.TITAN.getID()){
            try {
                URL skinURL = new URL("https://crafatar.com/avatars/" + uuid.toString() + "?size=16&overlay&default=MHF_Steve");
                BufferedImage img = ImageIO.read(skinURL);

                int iconID = BungeeChest.randomInteger(1000,Integer.MAX_VALUE);
                File file = new File("/usr/local/teamspeak3/files/virtualserver_1/internal/icons/icon_" + iconID);
                ImageIO.write(img,"png",file);

                ts3Api.deleteClientPermission(c.getDatabaseId(),"i_icon_id");
                ts3Api.addClientPermission(c.getDatabaseId(),"i_icon_id",iconID,false);
            } catch(Exception e){
                e.printStackTrace();
            }
        } else {
            ts3Api.deleteClientPermission(c.getDatabaseId(),"i_icon_id");
        }
    }

    public void takeIcon(String tsID){
        Client c = getClient(tsID);
        if(c == null) return;

        ts3Api.deleteClientPermission(c.getDatabaseId(),"i_icon_id");
    }

    public void startListeners() {
        //ts3Api.registerEvent(TS3EventType.TEXT_PRIVATE);

        ts3Api.addTS3Listeners(new TS3EventAdapter() {
            @Override
            public void onClientJoin(ClientJoinEvent e){
                BungeeChest.async(() -> {
                    Client c = getClient(e.getUniqueClientIdentifier());
                    Rank r = null;

                    for(int i : c.getServerGroups()){
                        ServerGroup s = getServerGroupById(i);

                        if(rankGroups.containsValue(s.getId())){
                            for(Rank rank : rankGroups.keySet()){
                                if(rankGroups.get(rank) == s.getId()){
                                    r = rank;
                                }
                            }
                        }
                    }

                    UUID uuid = getUUIDFromTeamSpeakID(e.getUniqueClientIdentifier());

                    if(uuid != null){
                        Rank currentRank = PlayerUtilities.getRankFromUUID(uuid);

                        if(r == null || (currentRank != null && currentRank != r)){
                            takeRanks(e.getUniqueClientIdentifier());
                            giveRank(e.getUniqueClientIdentifier(),currentRank);
                        }

                        if(c.getIconId() == 0){
                            giveIcon(e.getUniqueClientIdentifier(),uuid);
                        }
                    } else {
                        ts3Api.sendPrivateMessage(c.getId(), "Welcome to the official TheChest.eu TeamSpeak server!");
                        ts3Api.sendPrivateMessage(c.getId(), "We have noticed your TS3 identity isn't linked with a Minecraft account.");
                        ts3Api.sendPrivateMessage(c.getId(), "To link your Minecraft account and use extra features on our TS3 server, type the command [B]/ts " + e.getUniqueClientIdentifier() + "[/B] on our minecraft server ([B]thechest.eu[/B])!");
                    }
                });
            }

            @Override
            public void onTextMessage(TextMessageEvent e) {
                if(e.getTargetMode() == TextMessageTargetMode.CLIENT) {
                    BungeeChest.async(() -> {
                        String message = e.getMessage().toLowerCase();
                        // remove all spaces
                        message = message.replaceAll("\\s+","");
                        // check is abort typed
                        if(message.equalsIgnoreCase("!abort")) {
                            ts3Api.sendPrivateMessage(e.getInvokerId(), "You aborted the verify process!");
                            // remove from Database
                            removeTSVerificationFromDatabase(e.getInvokerUniqueId());
                            return;
                        }
                        // check string is too long for minecraft name
                        boolean valid = true;
                        if(message.length() > 16) {
                            valid = false;
                        }
                        // check is potential minecraft name
                        for (int i = 0; i < message.length(); i++) {
                            char c = message.charAt(i);
                            if(c == '\'') {
                                valid = false;
                                continue;
                            }
                            if(c == '<' || c == '>') {
                                valid = false;
                                continue;
                            }
                            if(c == '.') {
                                valid = false;
                                continue;
                            }
                            if(c == ';') {
                                valid = false;
                                continue;
                            }
                            if(c == '\"') {
                                valid = false;
                                continue;
                            }
                            if(c == '*') {
                                valid = false;
                                continue;
                            }
                            if(c == '!') {
                                valid = false;
                                continue;
                            }
                            if(c == 'ä' || c == 'ö' || c == 'ü') {
                                valid = false;
                                continue;
                            }
                            if(c == '$') {
                                valid = false;
                            }
                        }
                        if(!valid) {
                            // remove from Database
                            removeTSVerificationFromDatabase(e.getInvokerUniqueId());
                            return;
                        }
                        // try to verify, get minecraft uuid from users table
                        UUID minecraftUUID = PlayerUtilities.getUUIDFromName(message);
                        if (minecraftUUID != null && isInVerifyProcess(minecraftUUID)) {
                            setVerified(minecraftUUID, e.getInvokerUniqueId());
                            giveRank(e.getInvokerUniqueId(),PlayerUtilities.getRankFromUUID(minecraftUUID));
                            ts3Api.sendPrivateMessage(e.getInvokerId(), "Successfully verified!");
                            ts3Api.sendPrivateMessage(e.getInvokerId(), "Minecraft UUID: " + minecraftUUID);
                            ts3Api.sendPrivateMessage(e.getInvokerId(), "Teamspeak UUID: " + e.getInvokerUniqueId());
                            ts3Api.sendPrivateMessage(e.getInvokerId(), "New Group: Verified");
                            giveIcon(e.getInvokerUniqueId(),minecraftUUID);
                        } else {
                            ts3Api.sendPrivateMessage(e.getInvokerId(), "You're currently not in a verify process!");
                        }
                    });
                }
            }
        });
    }
}
