package eu.thechest.bungeechest.user;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.thechest.bungeechest.BungeeChest;
import eu.thechest.bungeechest.mysql.MySQLManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by zeryt on 23.02.2017.
 */
public class PlayerUtilities {
    public static HashMap<UUID,Rank> UUID_RANK_CACHE = new HashMap<UUID,Rank>();
    public static HashMap<String,UUID> NAME_UUID_CACHE = new HashMap<String,UUID>();
    public static HashMap<UUID,String> UUID_NAME_CACHE = new HashMap<UUID,String>();
    public static HashMap<UUID,HashMap<String,Boolean>> UUID_SETTINGS_CACHE = new HashMap<UUID,HashMap<String,Boolean>>();
    public static HashMap<String, String> IP_COUNTRY_CACHE = new HashMap<String, String>();

    public static String getCountryCodeFromIP(String ip){
        if(IP_COUNTRY_CACHE.containsKey(ip)) return IP_COUNTRY_CACHE.get(ip);
        try {
            //URL url = new URL("http://api.key-ative.com/ip2location/v1/" + ip);
            URL url = new URL("http://ip-api.com/json/" + ip);
            InputStream stream = url.openStream();
            InputStreamReader inr = new InputStreamReader(stream);
            BufferedReader reader = new BufferedReader(inr);
            String s = null;
            StringBuilder sb = new StringBuilder();
            while ((s = reader.readLine()) != null) {
                sb.append(s);
            }
            String result = sb.toString();

            JsonElement element = new JsonParser().parse(result);
            JsonObject obj = element.getAsJsonObject();

            String countryCode = obj.get("countryCode").toString();
            countryCode = countryCode.replace("\"", "");

            if(countryCode != null){
                IP_COUNTRY_CACHE.put(ip, countryCode);

                try {
                    PreparedStatement sql = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `ip_info` WHERE `ip` = ?");
                    sql.setString(1,ip);
                    ResultSet sqlRS = sql.executeQuery();

                    if(sqlRS.first()){
                        PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `ip_info` SET `country` = ? WHERE `ip` = ?");
                        ps.setString(1,countryCode);
                        ps.setString(2,ip);
                        ps.executeUpdate();
                        ps.close();
                    } else {
                        PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT IGNORE INTO `ip_info` (`ip`,`country`) VALUES(?,?)");
                        ps.setString(1,ip);
                        ps.setString(2,countryCode);
                        ps.executeUpdate();
                        ps.close();
                    }

                    MySQLManager.getInstance().closeResources(sqlRS,sql);
                } catch(Exception e){
                    e.printStackTrace();
                }
            }

            return countryCode;
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private static void fetchSettings(UUID uuid){
        if(!UUID_SETTINGS_CACHE.containsKey(uuid)){
            HashMap<String,Boolean> settings = new HashMap<String,Boolean>();

            BungeeChest.async(() -> {
                try {
                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `users` WHERE `uuid` = ?");
                    ps.setString(1,uuid.toString());
                    ResultSet rs = ps.executeQuery();

                    if(rs.first()){
                        String[] toFetch = new String[]{"language","setting_friendRequests","setting_privateMessages","setting_partyRequests","setting_headSeat","setting_lobbySpeed","setting_autoNick"};

                        for(String s : toFetch){
                            settings.put(s,rs.getBoolean(s));
                        }
                    }

                    MySQLManager.getInstance().closeResources(rs,ps);
                } catch(Exception e){
                    e.printStackTrace();
                }
            });

            UUID_SETTINGS_CACHE.put(uuid,settings);
        }
    }

    public static boolean allowsFriendRequests(UUID uuid){
        if(uuid == null) return false;

        if(BungeeChest.getInstance().getProxy().getPlayer(uuid) != null) return BungeeUser.getUser(BungeeChest.getInstance().getProxy().getPlayer(uuid)).setting_friendRequests;

        if(UUID_SETTINGS_CACHE.containsKey(uuid)){
            return UUID_SETTINGS_CACHE.get(uuid).get("setting_friendRequests");
        } else {
            fetchSettings(uuid);
            return UUID_SETTINGS_CACHE.get(uuid).get("setting_friendRequests");
        }
    }

    public static boolean allowsPrivateMessages(UUID uuid){
        if(uuid == null) return false;

        if(BungeeChest.getInstance().getProxy().getPlayer(uuid) != null) return BungeeUser.getUser(BungeeChest.getInstance().getProxy().getPlayer(uuid)).setting_privateMessages;

        if(UUID_SETTINGS_CACHE.containsKey(uuid)){
            return UUID_SETTINGS_CACHE.get(uuid).get("setting_privateMessages");
        } else {
            fetchSettings(uuid);
            return UUID_SETTINGS_CACHE.get(uuid).get("setting_privateMessages");
        }
    }

    public static boolean allowsPartyRequests(UUID uuid){
        if(uuid == null) return false;

        if(BungeeChest.getInstance().getProxy().getPlayer(uuid) != null) return BungeeUser.getUser(BungeeChest.getInstance().getProxy().getPlayer(uuid)).setting_partyRequests;

        if(UUID_SETTINGS_CACHE.containsKey(uuid)){
            return UUID_SETTINGS_CACHE.get(uuid).get("setting_partyRequests");
        } else {
            fetchSettings(uuid);
            return UUID_SETTINGS_CACHE.get(uuid).get("setting_partyRequests");
        }
    }

    public static boolean allowsHeadSeat(UUID uuid){
        if(uuid == null) return false;

        if(BungeeChest.getInstance().getProxy().getPlayer(uuid) != null) return BungeeUser.getUser(BungeeChest.getInstance().getProxy().getPlayer(uuid)).setting_headSeat;

        if(UUID_SETTINGS_CACHE.containsKey(uuid)){
            return UUID_SETTINGS_CACHE.get(uuid).get("setting_headSeat");
        } else {
            fetchSettings(uuid);
            return UUID_SETTINGS_CACHE.get(uuid).get("setting_headSeat");
        }
    }

    public static boolean allowsLobbySpeed(UUID uuid){
        if(uuid == null) return false;

        if(BungeeChest.getInstance().getProxy().getPlayer(uuid) != null) return BungeeUser.getUser(BungeeChest.getInstance().getProxy().getPlayer(uuid)).setting_lobbySpeed;

        if(UUID_SETTINGS_CACHE.containsKey(uuid)){
            return UUID_SETTINGS_CACHE.get(uuid).get("setting_lobbySpeed");
        } else {
            fetchSettings(uuid);
            return UUID_SETTINGS_CACHE.get(uuid).get("setting_lobbySpeed");
        }
    }

    public static boolean allowsAutoNick(UUID uuid){
        if(uuid == null) return false;

        if(BungeeChest.getInstance().getProxy().getPlayer(uuid) != null) return BungeeUser.getUser(BungeeChest.getInstance().getProxy().getPlayer(uuid)).setting_autoNick;

        if(UUID_SETTINGS_CACHE.containsKey(uuid)){
            return UUID_SETTINGS_CACHE.get(uuid).get("setting_autoNick");
        } else {
            fetchSettings(uuid);
            return UUID_SETTINGS_CACHE.get(uuid).get("setting_autoNick");
        }
    }

    public static Rank getRankFromUUID(String uuid){
        return getRankFromUUID(UUID.fromString(uuid));
    }

    public static Rank getRankFromUUID(UUID uuid){
        if(uuid == null) return Rank.USER;

        if(BungeeChest.getInstance().getProxy().getPlayer(uuid) != null) return BungeeUser.getUser(BungeeChest.getInstance().getProxy().getPlayer(uuid)).getRank();

        if(UUID_RANK_CACHE.containsKey(uuid)){
            return UUID_RANK_CACHE.get(uuid);
        } else {
            Rank r = Rank.USER;

            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `users` WHERE `uuid` = ?");
                ps.setString(1,uuid.toString());

                ResultSet rs = ps.executeQuery();
                if(rs.first()){
                    r = Rank.valueOf(rs.getString("rank"));
                    UUID_RANK_CACHE.put(uuid,r);
                }

                MySQLManager.getInstance().closeResources(rs,ps);
            } catch(Exception e){
                e.printStackTrace();
            }

            return r;
        }
    }

    public static UUID getUUIDFromName(String name){
        if(name == null || name.isEmpty()) return null;

        if(BungeeChest.getInstance().getProxy().getPlayer(name) != null) return BungeeChest.getInstance().getProxy().getPlayer(name).getUniqueId();

        if(NAME_UUID_CACHE.containsKey(name)){
            return NAME_UUID_CACHE.get(name);
        } else {
            UUID uuid = null;

            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `users` WHERE `username` = ?");
                ps.setString(1,name);

                ResultSet rs = ps.executeQuery();
                if(rs.first()){
                    name = rs.getString("username");
                    String u = rs.getString("uuid");
                    uuid = UUID.fromString(u);

                    NAME_UUID_CACHE.put(name,uuid);
                }

                MySQLManager.getInstance().closeResources(rs,ps);
            } catch(Exception e){
                e.printStackTrace();
            }

            if(uuid == null){
                String u = UUIDFetcher.getUUID(name);

                if(u != null && !u.isEmpty()){
                    uuid = UUID.fromString(u);
                }
            }

            return uuid;
        }
    }

    public static String getNameFromUUID(String uuid){
        return getNameFromUUID(UUID.fromString(uuid));
    }

    public static String getNameFromUUID(UUID uuid){
        if(uuid == null) return null;

        if(BungeeChest.getInstance().getProxy().getPlayer(uuid) != null) return BungeeChest.getInstance().getProxy().getPlayer(uuid).getName();

        if(UUID_NAME_CACHE.containsKey(uuid)){
            return UUID_NAME_CACHE.get(uuid);
        } else {
            String name = null;

            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `users` WHERE `uuid` = ?");
                ps.setString(1,uuid.toString());

                ResultSet rs = ps.executeQuery();
                if(rs.first()){
                    name = rs.getString("username");

                    UUID_NAME_CACHE.put(uuid,name);
                }

                MySQLManager.getInstance().closeResources(rs,ps);
            } catch(Exception e){
                e.printStackTrace();
            }

            return name;
        }
    }

    public static boolean setRank(UUID uuid, Rank rank){
        if(uuid == null || rank == null) return false;

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `users` SET `rank` = ?, `requiresForumsRankUpdate` = 1 WHERE `uuid` = ?");
            ps.setString(1,rank.toString());
            ps.setString(2,uuid.toString());
            ps.execute();
            ps.close();

            ProxiedPlayer p2 = BungeeChest.getInstance().getProxy().getPlayer(uuid);
            if(p2 != null){
                BungeeUser u2 = BungeeUser.getUser(p2);

                u2.updateRank(rank,false);
                p2.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + u2.getTranslatedMessage("Your rank has been updated! For the changes to fully take effect, we highly recommend that you reconnect to the server ASAP!")));
            }

            return true;
        } catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static ArrayList<ProxiedPlayer> getOnlineStaff(){
        return getOnlineStaff(Rank.STAFF);
    }

    public static ArrayList<ProxiedPlayer> getOnlineStaff(Rank minRank){
        ArrayList<ProxiedPlayer> a = new ArrayList<ProxiedPlayer>();

        for(ProxiedPlayer all : BungeeChest.getInstance().getProxy().getPlayers()){
            if(BungeeUser.getUser(all).hasPermission(minRank)){
                a.add(all);
            }
        }

        return a;
    }

    public static void logRankUpdate(UUID executor, UUID player, Rank rank){
        if(player == null || rank == null) return;

        if(executor != null){
            // Updated by player

            BungeeChest.async(() -> {
                try {
                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `rankUpdates` (`executor`,`player`,`newRank`) VALUES(?,?,?)");
                    ps.setString(1,executor.toString());
                    ps.setString(2,player.toString());
                    ps.setString(3,rank.toString());
                    ps.execute();
                    ps.close();

                    ProxiedPlayer p = BungeeChest.getInstance().getProxy().getPlayer(executor);
                    BungeeUser u = BungeeUser.getUser(p);
                    String playerName = getNameFromUUID(player);

                    System.out.println("[RANK UPDATE] From " + p.getName() + " for " + playerName + ": " + rank.getName());

                    for(ProxiedPlayer all : getOnlineStaff(Rank.ADMIN)){
                        BungeeUser a = BungeeUser.getUser(all);

                        all.sendMessage(TextComponent.fromLegacyText(BungeeChest.STAFFNOTIFY_PREFIX + a.getTranslatedMessage("%e has updated %p's rank to %r").replace("%e",u.getRank().getColor() + p.getName() + ChatColor.YELLOW).replace("%p",rank.getColor() + playerName + ChatColor.YELLOW).replace("%r",rank.getColor() + rank.getName() + ChatColor.YELLOW)));
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }
            });
        } else {
            // Updated by console

            BungeeChest.async(() -> {
                try {
                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `rankUpdates` (`player`,`newRank`) VALUES(?,?)");
                    ps.setString(1,player.toString());
                    ps.setString(2,rank.toString());
                    ps.execute();
                    ps.close();

                    String playerName = getNameFromUUID(player);

                    System.out.println("[RANK UPDATE] From " + "BungeeConsole" + " for " + playerName + ": " + rank.getName());

                    for(ProxiedPlayer all : getOnlineStaff(Rank.ADMIN)){
                        BungeeUser a = BungeeUser.getUser(all);

                        all.sendMessage(TextComponent.fromLegacyText(BungeeChest.STAFFNOTIFY_PREFIX + a.getTranslatedMessage("%e has updated %p's rank to %r").replace("%e",ChatColor.DARK_PURPLE + "BungeeConsole" + ChatColor.YELLOW).replace("%p",rank.getColor() + playerName + ChatColor.YELLOW).replace("%r",rank.getColor() + rank.getName() + ChatColor.YELLOW)));
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }
            });
        }
    }
}