package eu.thechest.bungeechest.util;

import de.dytanic.cloudnet.network.ServerInfo;
import eu.thechest.bungeechest.BungeeChest;
import eu.thechest.bungeechest.ban.BanUtilities;
import eu.thechest.bungeechest.crews.Crew;
import eu.thechest.bungeechest.mysql.MySQLManager;
import eu.thechest.bungeechest.user.BungeeUser;
import eu.thechest.bungeechest.user.PlayerUtilities;
import eu.thechest.bungeechest.user.Rank;
import eu.thechest.bungeechest.user.UUIDFetcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by zeryt on 18.02.2017.
 */
public class TaskUtils {
    private static boolean init = false;

    public static void init(){
        if(!init){
            //queueTaskSoccer();
            reloadCaches();
            //queueTaskSGDuels();

            init = true;
        }
    }

    private static void queueTaskSGDuels(){
        BungeeChest.async(() -> {
            if(BungeeChest.getInstance().getProxy().getPlayers().size() > 0){
                ArrayList<UUID> unranked = new ArrayList<UUID>();
                ArrayList<UUID> ranked = new ArrayList<UUID>();

                try {
                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `sgduels_queue`");
                    ResultSet rs = ps.executeQuery();

                    while(rs.next()){
                        int teamSize = rs.getInt("teamsize");
                        boolean isRanked = rs.getBoolean("ranked");

                        if(isRanked){
                            ranked.add(UUID.fromString(rs.getString("uuid")));
                        } else {
                            unranked.add(UUID.fromString(rs.getString("uuid")));
                        }
                    }

                    MySQLManager.getInstance().closeResources(rs,ps);

                    handleJoiningSGDuels(unranked,2, false);
                    handleJoiningSGDuels(ranked,2, true);
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        });

        BungeeChest.getInstance().getProxy().getScheduler().schedule(BungeeChest.getInstance(), new Runnable(){
            @Override
            public void run(){
                queueTaskSGDuels();
            }
        }, 10, TimeUnit.SECONDS);
    }

    private static void queueTaskSoccer(){
        BungeeChest.async(() -> {
            if(BungeeChest.getInstance().getProxy().getPlayers().size() > 0){
                ArrayList<UUID> unranked1 = new ArrayList<UUID>();
                ArrayList<UUID> unranked2 = new ArrayList<UUID>();
                ArrayList<UUID> unranked3 = new ArrayList<UUID>();
                ArrayList<UUID> unranked4 = new ArrayList<UUID>();

                try {
                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `soccer_queue` WHERE `ranked` = ?");
                    ps.setBoolean(1,false);
                    ResultSet rs = ps.executeQuery();

                    while(rs.next()){
                        int teamSize = rs.getInt("teamsize");

                        if(teamSize == 1){
                            unranked1.add(UUID.fromString(rs.getString("uuid")));
                        } else if(teamSize == 2){
                            unranked2.add(UUID.fromString(rs.getString("uuid")));
                        } else if(teamSize == 3){
                            unranked3.add(UUID.fromString(rs.getString("uuid")));
                        } else if(teamSize == 4){
                            unranked4.add(UUID.fromString(rs.getString("uuid")));
                        }
                    }

                    MySQLManager.getInstance().closeResources(rs,ps);

                    handleJoiningSoccer(unranked1,2, false);
                    handleJoiningSoccer(unranked2,4, false);
                    handleJoiningSoccer(unranked3,6, false);
                    handleJoiningSoccer(unranked4,8, false);
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        });

        BungeeChest.getInstance().getProxy().getScheduler().schedule(BungeeChest.getInstance(), new Runnable(){
            @Override
            public void run(){

            }
        }, 10, TimeUnit.SECONDS);
    }

    private static void handleJoiningSGDuels(ArrayList<UUID> list, int maxParticipants, boolean ranked){
        if(list.size() >= maxParticipants){
            while(list.size() >= maxParticipants){
                ServerInfo server = BungeeChest.getBestServer("SGDuels",maxParticipants);
                /*try {
                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `servers` WHERE `name` LIKE 'SOCCERGAME%' AND `online` = ? ORDER BY `players.online` ASC LIMIT 1");
                    ps.setBoolean(1,true);
                    ResultSet rs = ps.executeQuery();

                    if(rs.first()){
                        int online = rs.getInt("players.online");
                        int max = rs.getInt("players.max");

                        if((max-online) >= maxParticipants) server = rs.getString("name");
                    }

                    MySQLManager.getInstance().closeResources(rs,ps);
                } catch(Exception e){
                    e.printStackTrace();
                }*/

                Collections.shuffle(list);

                ArrayList<UUID> team1 = new ArrayList<UUID>();

                for(int i = 0; i < maxParticipants/2; i++) team1.add(list.get(i));
                list.removeAll(team1);

                Collections.shuffle(list);

                ArrayList<UUID> team2 = new ArrayList<UUID>();
                for(int i = 0; i < maxParticipants/2; i++) team2.add(list.get(i));

                list.removeAll(team2);

                ArrayList<UUID> participants = new ArrayList<UUID>();
                participants.addAll(team1);
                participants.addAll(team2);

                if(server != null){
                    for(UUID uuid : participants){
                        ProxiedPlayer p = BungeeChest.getInstance().getProxy().getPlayer(uuid);

                        if(p != null){
                            BungeeUser u = BungeeUser.getUser(p);
                            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.YELLOW + u.getTranslatedMessage("Preparing game..")));
                        }

                        try {
                            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("DELETE FROM `sgduels_queue` WHERE `uuid` = ?");
                            ps.setString(1,uuid.toString());
                            ps.executeUpdate();
                            ps.close();
                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    }

                    try {
                        String s_team1 = "";
                        for(UUID uuid : team1){
                            if(s_team1.isEmpty()){
                                s_team1 = s_team1 + uuid.toString();
                            } else {
                                s_team1 = s_team1 + "," + uuid.toString();
                            }
                        }

                        String s_team2 = "";
                        for(UUID uuid : team2){
                            if(s_team2.isEmpty()){
                                s_team2 = s_team2 + uuid.toString();
                            } else {
                                s_team2 = s_team2 + "," + uuid.toString();
                            }
                        }

                        PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `sgduels_upcomingGames` (`team1`,`team2`,`ranked`) VALUES(?,?,?);");
                        ps.setString(1,s_team1);
                        ps.setString(2,s_team2);
                        ps.setBoolean(3,ranked);
                        ps.executeUpdate();
                        ps.close();
                    } catch(Exception e){
                        e.printStackTrace();
                    }

                    for(UUID uuid : participants){
                        ProxiedPlayer p = BungeeChest.getInstance().getProxy().getPlayer(uuid);

                        if(p != null){
                            p.connect(BungeeChest.getInstance().getProxy().getServerInfo(server.getName()));
                        }
                    }
                } else {
                    for(UUID uuid : participants){
                        ProxiedPlayer p = BungeeChest.getInstance().getProxy().getPlayer(uuid);

                        if(p != null){
                            BungeeUser u = BungeeUser.getUser(p);
                            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Could not find an appropriate game server!")));
                        }
                    }

                    list.removeAll(participants);
                }
            }
        } else {
            int i = maxParticipants - list.size();

            for(UUID uuid : list){
                ProxiedPlayer p = BungeeChest.getInstance().getProxy().getPlayer(uuid);

                if(p != null){
                    BungeeUser u = BungeeUser.getUser(p);
                    p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + u.getTranslatedMessage("Waiting for %p more players to join the queue!").replace("%p", ChatColor.YELLOW.toString() + i + ChatColor.GREEN.toString())));
                }
            }
        }
    }

    private static void handleJoiningSoccer(ArrayList<UUID> list, int maxParticipants, boolean ranked){
        if(list.size() >= maxParticipants){
            while(list.size() >= maxParticipants){
                ServerInfo server = BungeeChest.getBestServer("SoccerMC",maxParticipants);
                /*try {
                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `servers` WHERE `name` LIKE 'SOCCERGAME%' AND `online` = ? ORDER BY `players.online` ASC LIMIT 1");
                    ps.setBoolean(1,true);
                    ResultSet rs = ps.executeQuery();

                    if(rs.first()){
                        int online = rs.getInt("players.online");
                        int max = rs.getInt("players.max");

                        if((max-online) >= maxParticipants) server = rs.getString("name");
                    }

                    MySQLManager.getInstance().closeResources(rs,ps);
                } catch(Exception e){
                    e.printStackTrace();
                }*/

                Collections.shuffle(list);

                ArrayList<UUID> participants = new ArrayList<UUID>();
                for(int i = 0; i < maxParticipants; i++){
                    participants.add(list.get(i));
                }

                list.removeAll(participants);

                if(server != null){
                    for(UUID uuid : participants){
                        ProxiedPlayer p = BungeeChest.getInstance().getProxy().getPlayer(uuid);

                        if(p != null){
                            BungeeUser u = BungeeUser.getUser(p);
                            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.YELLOW + u.getTranslatedMessage("Preparing game..")));
                        }

                        try {
                            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("DELETE FROM `soccer_queue` WHERE `uuid` = ?");
                            ps.setString(1,uuid.toString());
                            ps.executeUpdate();
                            ps.close();
                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    }

                    try {
                        String s = "";
                        for(UUID uuid : participants){
                            if(s.isEmpty()){
                                s = s + uuid.toString();
                            } else {
                                s = s + "," + uuid.toString();
                            }
                        }

                        PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `soccer_upcomingGames` (`participants`,`ranked`,`teamSize`) VALUES(?,?,?);");
                        ps.setString(1,s);
                        ps.setBoolean(2,ranked);
                        ps.setInt(3,maxParticipants/2);
                        ps.executeUpdate();
                        ps.close();
                    } catch(Exception e){
                        e.printStackTrace();
                    }

                    for(UUID uuid : participants){
                        ProxiedPlayer p = BungeeChest.getInstance().getProxy().getPlayer(uuid);

                        if(p != null){
                            p.connect(BungeeChest.getInstance().getProxy().getServerInfo(server.getName()));
                        }
                    }
                } else {
                    for(UUID uuid : participants){
                        ProxiedPlayer p = BungeeChest.getInstance().getProxy().getPlayer(uuid);

                        if(p != null){
                            BungeeUser u = BungeeUser.getUser(p);
                            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Could not find an appropriate game server!")));
                        }
                    }

                    list.removeAll(participants);
                }
            }
        } else {
            int i = maxParticipants - list.size();

            for(UUID uuid : list){
                ProxiedPlayer p = BungeeChest.getInstance().getProxy().getPlayer(uuid);

                if(p != null){
                    BungeeUser u = BungeeUser.getUser(p);
                    p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + u.getTranslatedMessage("Waiting for %p more players to join the queue!").replace("%p", ChatColor.YELLOW.toString() + i + ChatColor.GREEN.toString())));
                }
            }
        }
    }

    public static void reloadCaches(){
        reloadCaches(true);
    }

    public static void reloadCaches(boolean queueTask){
        //BungeeChest.getInstance().reloadServers();
        BungeeChest.async(() -> {
            BungeeChest.getInstance().reloadBadwords();
            MotDManager.getInstance().reload();
        });

        UUIDFetcher.uuidCache.clear();

        PlayerUtilities.UUID_RANK_CACHE.clear();
        PlayerUtilities.NAME_UUID_CACHE.clear();
        PlayerUtilities.UUID_NAME_CACHE.clear();
        PlayerUtilities.UUID_SETTINGS_CACHE.clear();

        BanUtilities.ACTIVE_IPBANS.clear();

        for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.ADMIN)){
            all.sendMessage(TextComponent.fromLegacyText(BungeeChest.STAFFNOTIFY_PREFIX + ChatColor.GREEN + "Completed Task " + ChatColor.WHITE + "ReloadCaches"));
        }

        if(queueTask){
            BungeeChest.getInstance().getProxy().getScheduler().schedule(BungeeChest.getInstance(), new Runnable(){
                @Override
                public void run(){
                    reloadCaches();
                }
            }, 10, TimeUnit.MINUTES);
        }
    }
}
