package eu.thechest.bungeechest;

import com.vexsoftware.votifier.bungee.events.VotifierEvent;
import com.vexsoftware.votifier.model.Vote;
import de.dytanic.cloudnet.api.CloudNetAPI;
import de.dytanic.cloudnet.bukkitproxy.api.CloudProxy;
import eu.thechest.bungeechest.ban.Ban;
import eu.thechest.bungeechest.ban.BanUtilities;
import eu.thechest.bungeechest.ban.IPBan;
import eu.thechest.bungeechest.ban.Mute;
import eu.thechest.bungeechest.crews.Crew;
import eu.thechest.bungeechest.mysql.MySQLManager;
import eu.thechest.bungeechest.party.Party;
import eu.thechest.bungeechest.user.BungeeUser;
import eu.thechest.bungeechest.user.PlayerUtilities;
import eu.thechest.bungeechest.user.Rank;
import eu.thechest.bungeechest.util.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by zeryt on 18.02.2017.
 */
public class MainListener implements Listener {
    private static HashMap<String,String> CONNECTING = new HashMap<String,String>();

    @EventHandler
    public void onPing(ProxyPingEvent e){
        ServerPing ping = e.getResponse();
        String usedIP = null;
        if(e.getConnection() != null && e.getConnection().getVirtualHost() != null && e.getConnection().getVirtualHost().getHostName() != null) usedIP = e.getConnection().getVirtualHost().getHostName();

        if(CloudProxy.getInstance().getProxyLayout().isMaintenance()){
            ping.setDescription(ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + "TheChest" + ChatColor.YELLOW + ".eu " + ChatColor.DARK_GRAY + "|" + ChatColor.RED.toString() + ChatColor.BOLD.toString() + " Maintenance");
        } else {
            if(usedIP != null && usedIP.equalsIgnoreCase("team.thechest.eu")){
                ping.setDescription(ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + "TheChest" + ChatColor.YELLOW + ".eu " + ChatColor.DARK_GRAY + "|" + ChatColor.WHITE.toString() + ChatColor.ITALIC.toString() + " Team-Server");
            } else {
                ping.setDescription(MotDManager.getInstance().getMessageOfTheDay());
            }
        }

        String[] hoverInfo = new String[]{
                ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + "TheChest.eu",
                "",
                ChatColor.YELLOW + "Website: " + ChatColor.AQUA + "https://thechest.eu",
                ChatColor.YELLOW + "TeamSpeak3: " + ChatColor.AQUA + "thechest.eu"
        };

        ArrayList<ServerPing.PlayerInfo> a = new ArrayList<ServerPing.PlayerInfo>();
        for(String s : hoverInfo){
            a.add(new ServerPing.PlayerInfo(s,UUID.randomUUID()));
        }

        ping.setPlayers(
                new ServerPing.Players(
                        3000, // <- define max. slots (possibly define in database?)
                        BungeeChest.getInstance().getProxy().getOnlineCount(), // <- define currently online players, may be faked but we are not mineplex.
                        a.toArray(new ServerPing.PlayerInfo[]{})
                )
        );

        e.setResponse(ping);

        if(usedIP != null && e.getConnection().getName() != null) CONNECTING.put(e.getConnection().getName(),usedIP);
    }

    @EventHandler
    public void onVote(VotifierEvent e){
        Vote v = e.getVote();
        String name = v.getUsername();
        UUID uuid = PlayerUtilities.getUUIDFromName(name);
        ProxiedPlayer p = BungeeChest.getInstance().getProxy().getPlayer(name);

        BungeeChest.executeConsoleCommand("givecoins " + name + " 50");

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `votes` (`uuid`,`username`,`ip`,`service`) VALUES(?,?,?,?);");
            ps.setString(1,uuid.toString());
            ps.setString(2,name);
            ps.setString(3,v.getAddress());
            ps.setString(4,v.getServiceName());
            ps.executeUpdate();
            ps.close();
        } catch(Exception e1){
            e1.printStackTrace();
        }

        if(p != null){
            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + BungeeUser.getUser(p).getTranslatedMessage("Thank you for voting! Your reward has been credited!")));
        }
    }

    @EventHandler
    public void onLogin(LoginEvent e){
        String p = e.getConnection().getName();
        String usedIP = e.getConnection().getVirtualHost().getHostName();
        String ip = e.getConnection().getAddress().toString().replace("/","").split(":")[0];

        boolean ipIsLocal = false;

        if(ip == null || ip.isEmpty() || ip.equalsIgnoreCase("localhost") || ip.equalsIgnoreCase("127.0.0.1")){
            ipIsLocal = true;
        }

        if(!ipIsLocal){
            VPNCheckResult r = VPNCheckUtil.getResult(ip);
            if(r != null){
                if(r.isVPN()){
                    e.setCancelled(true);
                    e.setCancelReason(ChatColor.RED + "Your IP has been detected as malicious.\nPlease disable any ip changing software such as VPNs and try again.");
                    return;
                }
            }

            IPBan ban = BanUtilities.getIPBan(ip);
            if(ban != null){
                e.setCancelled(true);
                if(ban.getReason() == null || ban.getReason().trim().isEmpty()){
                    e.setCancelReason(ChatColor.RED.toString() + "Your IP has been banned permanently.");
                } else {
                    e.setCancelReason(ChatColor.RED.toString() + "Your IP has been banned permanently.\nReason: " + ChatColor.YELLOW + ban.getReason());
                }
                return;
            }
        }

        if(p != null && !p.isEmpty()){
            UUID uuid = PlayerUtilities.getUUIDFromName(p);

            if(CloudProxy.getInstance().getProxyLayout().isMaintenance()){
                boolean cancel = false;

                if(uuid == null){
                    cancel = true;
                } else {
                    Rank rank = PlayerUtilities.getRankFromUUID(uuid);

                    if(rank != null){
                        if(!(rank.getID() >= Rank.STAFF.getID())){
                            cancel = true;
                        }
                    } else {
                        cancel = true;
                    }
                }

                if(cancel){
                    e.setCancelled(true);
                    e.setCancelReason(ChatColor.RED.toString() + "Currently in maintenance!");
                    return;
                }
            }

            if(uuid != null){
                if(BanUtilities.getBan(uuid) != null){
                    Ban ban = BanUtilities.getBan(uuid);

                    if(!ban.isExpired()){
                        e.setCancelled(true);

                        if(ban.getReason() == null){
                            if(ban.getEndDate() == null){
                                e.setCancelReason(
                                        ChatColor.GOLD.toString() + ChatColor.BOLD + "TheChest.eu" + "\n" +
                                                ChatColor.RED + "You were banned from this server." + "\n" +
                                                "\n" +
                                                ChatColor.YELLOW + "Reason: " + ChatColor.WHITE + "No reason specified." + "\n" +
                                                ChatColor.GRAY + "Ban ID: " + ChatColor.WHITE + "#" + ban.getID() + "\n\n" +
                                                ChatColor.GRAY + "Appeal at " + ChatColor.AQUA.toString() + ChatColor.UNDERLINE.toString() + "https://go.thechest.eu/appeal"
                                );
                            } else {
                                e.setCancelReason(
                                        ChatColor.GOLD.toString() + ChatColor.BOLD + "TheChest.eu" + "\n" +
                                                ChatColor.RED + "You were banned from this server." + "\n" +
                                                "\n" +
                                                ChatColor.YELLOW + "Reason: " + ChatColor.WHITE + "No reason specified." + "\n" +
                                                ChatColor.YELLOW + "Ends: " + ChatColor.WHITE + ban.getEndDate().toGMTString() + "\n" +
                                                ChatColor.GRAY + "Ban ID: " + ChatColor.WHITE + "#" + ban.getID() + "\n\n" +
                                                ChatColor.GRAY + "Appeal at " + ChatColor.AQUA.toString() + ChatColor.UNDERLINE.toString() + "https://go.thechest.eu/appeal"
                                );
                            }
                        } else {
                            if(ban.getEndDate() == null){
                                e.setCancelReason(
                                        ChatColor.GOLD.toString() + ChatColor.BOLD + "TheChest.eu" + "\n" +
                                                ChatColor.RED + "You were banned from this server." + "\n" +
                                                "\n" +
                                                ChatColor.YELLOW + "Reason: " + ChatColor.WHITE + ban.getReason() + "\n" +
                                                ChatColor.GRAY + "Ban ID: " + ChatColor.WHITE + "#" + ban.getID() + "\n\n" +
                                                ChatColor.GRAY + "Appeal at " + ChatColor.AQUA.toString() + ChatColor.UNDERLINE.toString() + "https://go.thechest.eu/appeal"
                                );
                            } else {
                                e.setCancelReason(
                                        ChatColor.GOLD.toString() + ChatColor.BOLD + "TheChest.eu" + "\n" +
                                                ChatColor.RED + "You were banned from this server." + "\n" +
                                                "\n" +
                                                ChatColor.YELLOW + "Reason: " + ChatColor.WHITE + ban.getReason() + "\n" +
                                                ChatColor.YELLOW + "Ends: " + ChatColor.WHITE + ban.getEndDate().toGMTString() + "\n" +
                                                ChatColor.GRAY + "Ban ID: " + ChatColor.WHITE + "#" + ban.getID() + "\n\n" +
                                                ChatColor.GRAY + "Appeal at " + ChatColor.AQUA.toString() + ChatColor.UNDERLINE.toString() + "https://go.thechest.eu/appeal"
                                );
                            }
                        }
                    } else {
                        ban.markAsExpired();
                    }
                }

                if(!ipIsLocal){
                    new Thread(() -> {
                        try {
                            String country = PlayerUtilities.getCountryCodeFromIP(ip);

                            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `logins` (`uuid`,`ip`,`usedIP`,`country`) VALUES(?,?,?,?);");
                            ps.setString(1,uuid.toString());
                            ps.setString(2,ip);
                            ps.setString(3,usedIP);
                            ps.setString(4,country);
                            ps.execute();
                            ps.close();
                        } catch(Exception e1){
                            e1.printStackTrace();
                        }
                    }).start();
                }
            }
        }
    }

    @EventHandler
    public void onJoin(PostLoginEvent e){
        ProxiedPlayer p = e.getPlayer();
        BungeeUser u = BungeeUser.getUser(p);

        if(PlayerUtilities.UUID_NAME_CACHE.containsKey(p.getUniqueId())) PlayerUtilities.UUID_NAME_CACHE.remove(p.getUniqueId());
        if(PlayerUtilities.NAME_UUID_CACHE.containsKey(p.getName())) PlayerUtilities.NAME_UUID_CACHE.remove(p.getName());
        if(PlayerUtilities.UUID_RANK_CACHE.containsKey(p.getUniqueId())) PlayerUtilities.UUID_RANK_CACHE.remove(p.getUniqueId());
        if(PlayerUtilities.UUID_SETTINGS_CACHE.containsKey(p.getUniqueId())) PlayerUtilities.UUID_SETTINGS_CACHE.remove(p.getUniqueId());
    }

    @EventHandler(priority = Byte.MAX_VALUE)
    public void onConnect(ServerConnectEvent e){
        ProxiedPlayer p = e.getPlayer();
        BungeeUser u = BungeeUser.getUser(p);

        String usedIP = CONNECTING.containsKey(p.getName()) ? CONNECTING.get(p.getName()) : null;

        boolean firstJoin = false;
        if(!BungeeChest.ONSERV.contains(p.getName())){
            firstJoin = true;
        }

        if(firstJoin){
            de.dytanic.cloudnet.network.ServerInfo target = null;

            if(usedIP != null && usedIP.equalsIgnoreCase("team.thechest.eu")){
                target = BungeeChest.getInstance().getBestServer("TeamServer");
            } else {
                ServerInfo i = BungeeChest.getInstance().getBestLobby(p);
                target = i != null ? CloudNetAPI.getInstance().getServerInfo(i.getName()) : null;
            }

            if(target != null){
                ServerInfo info = ProxyServer.getInstance().getServerInfo(target.getName());

                if(info != null) e.setTarget(info);
            }
        }

        if(usedIP != null) CONNECTING.remove(p.getName());
    }

    @EventHandler
    public void onConnected(ServerConnectedEvent e){
        ProxiedPlayer p = e.getPlayer();
        BungeeUser u = BungeeUser.getUser(p);

        boolean firstJoin = false;
        if(!BungeeChest.ONSERV.contains(p.getName())){
            firstJoin = true;
            BungeeChest.ONSERV.add(p.getName());
        }

        if(firstJoin){
            String ip = p.getAddress().toString().replace("/","").split(":")[0];
            if(ip != null && !ip.isEmpty() && !ip.equalsIgnoreCase("localhost") && !ip.equalsIgnoreCase("127.0.0.1")) u.country = PlayerUtilities.getCountryCodeFromIP(ip);

            BungeeChest.updatePeak();

            // SEND STAFF JOIN MESSAGE
            if(u.hasPermission(Rank.STAFF)){
                for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff()){
                    if(all == p) continue;
                    BungeeUser a = BungeeUser.getUser(all);

                    all.sendMessage(TextComponent.fromLegacyText(BungeeChest.STAFFNOTIFY_PREFIX + a.getTranslatedMessage("%p is now online.").replace("%p",u.getRank().getColor() + u.getRank().getName() + " " + p.getName() + ChatColor.GREEN)));
                }

                System.out.println("[STAFF JOIN] " + p.getName());
            }

            // SEND FRIENDS JOIN MESSAGE
            for(String s : u.getFriends()){
                if(BungeeChest.getInstance().getProxy().getPlayer(UUID.fromString(s)) != null){
                    ProxiedPlayer p2 = BungeeChest.getInstance().getProxy().getPlayer(UUID.fromString(s));
                    BungeeUser u2 = BungeeUser.getUser(p2);

                    p2.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_GREEN + "+" + ChatColor.DARK_GRAY + "] " + u.getRank().getColor() + p.getName()));
                }
            }

            // SEND FRIEND REQUESTS INFO
            if(u.getFriendRequests().size() > 0){
                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.YELLOW + u.getTranslatedMessage("You have %c open friend requests.").replace("%c",String.valueOf(u.getFriendRequests().size()))));
            }

            // SEND OPEN REPORTS INFO
            if(u.hasPermission(Rank.MOD)){
                try {
                    int openReports = 0;

                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `sn_reports` WHERE `solved.status` = ?");
                    ps.setString(1,"OPEN");
                    ResultSet rs = ps.executeQuery();
                    while(rs.next()) openReports++;
                    MySQLManager.getInstance().closeResources(rs,ps);

                    if(openReports > 0){
                        p.sendMessage(TextComponent.fromLegacyText(" "));

                        if(openReports == 1){
                            p.sendMessage(new ComponentBuilder(u.getTranslatedMessage("There is %r open report. Click here to view it.").replace("%r",String.valueOf(openReports))).color(ChatColor.YELLOW).event(new ClickEvent(ClickEvent.Action.OPEN_URL,"https://admin.thechest.eu/ban-management/reports")).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,TextComponent.fromLegacyText(ChatColor.YELLOW + "https://admin.thechest.eu/ban-management/reports"))).create());
                        } else {
                            p.sendMessage(new ComponentBuilder(u.getTranslatedMessage("There are %r open reports. Click here to view them.").replace("%r",String.valueOf(openReports))).color(ChatColor.YELLOW).event(new ClickEvent(ClickEvent.Action.OPEN_URL,"https://admin.thechest.eu/ban-management/reports")).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,TextComponent.fromLegacyText(ChatColor.YELLOW + "https://admin.thechest.eu/ban-management/reports"))).create());
                        }
                    }
                } catch(Exception e1){
                    e1.printStackTrace();
                }
            }
        }
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent e){
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(e.getData()));

        if(e.getTag().equalsIgnoreCase("WDL|INIT") || (e.getTag().equalsIgnoreCase("PERMISSIONSREPL") && new String(e.getData()).contains("mod.worlddownloader"))){
            e.getSender().disconnect(TextComponent.fromLegacyText(ChatColor.RED + "We do not allow the use of World Downloader."));
        }

        try {
            String subchannel = dis.readUTF();

            if(subchannel.equalsIgnoreCase("globalcommand")){
                String executor = dis.readUTF();
                String command = dis.readUTF();
				/*System.out.println(content);
				String executor = content.split(":")[0];
				String command = content.split(":")[1];

				StringBuilder sb = new StringBuilder("");
				for (int i = 0; i < command.split("|").length; i++) {
					sb.append(command.split("|")[i]).append(" ");
				}
				String s = sb.toString();*/

                if(executor.equalsIgnoreCase("CONSOLE") || executor.equalsIgnoreCase("BungeeConsole") || BungeeChest.getInstance().getProxy().getPlayer(executor) == null){
                    BungeeChest.getInstance().getProxy().getPluginManager().dispatchCommand(BungeeChest.getInstance().getProxy().getConsole(), command);
                } else {
                    BungeeChest.getInstance().getProxy().getPluginManager().dispatchCommand(BungeeChest.getInstance().getProxy().getPlayer(executor), command);
                }
            } else if(subchannel.equalsIgnoreCase("reloadSettings")){
                UUID uuid = UUID.fromString(dis.readUTF());
                ProxiedPlayer p = BungeeChest.getInstance().getProxy().getPlayer(uuid);

                if(p != null){
                    BungeeUser u = BungeeUser.getUser(p);

                    u.updateSettings();
                }
            } else if(subchannel.equalsIgnoreCase("givecrewcoins")){
                String playerName = dis.readUTF();

                try {
                    int coins = Integer.parseInt(dis.readUTF());
                    ProxiedPlayer p = BungeeChest.getInstance().getProxy().getPlayer(playerName);

                    if(p != null){
                        Crew c = BungeeUser.getUser(p).getCrew();

                        if(c != null){
                            c.addCoins(coins);
                        }
                    }
                } catch(NumberFormatException e1){}
            }
        } catch(IOException e1){

        } catch(Exception e1){
            e1.printStackTrace();
        }
    }

    @EventHandler
    public void onCheck(PermissionCheckEvent e){
        if(e.getSender() instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer)e.getSender();
            BungeeUser u = BungeeUser.getUser(p);

            if(!e.getPermission().equalsIgnoreCase("bungeecord.command.end") && u.hasPermission(Rank.ADMIN)){
                e.setHasPermission(true);
                return;
            }

            Rank minRank = null;

            switch(e.getPermission()){
                case "bungeecord.command.list":
                    minRank = Rank.ADMIN;
                    break;
                case "bungeecord.command.server":
                    minRank = Rank.MOD;
                    break;
                case "bungeecord.command.send":
                    minRank = Rank.ADMIN;
                    break;
                case "bungeecord.command.find":
                    minRank = Rank.MOD;
                    break;
            }

            if(minRank == null){
                e.setHasPermission(false);
            } else {
                e.setHasPermission(u.hasPermission(minRank));
            }
        }
    }

    @EventHandler
    public void onSwitch(ServerSwitchEvent e){
        ProxiedPlayer p = e.getPlayer();
        BungeeUser u = BungeeUser.getUser(p);

        //u.updateSettings();

        if(p.getServer().getInfo().getName().startsWith("Lobby") || p.getServer().getInfo().getName().startsWith("PremiumLobby") || p.getServer().getInfo().getName().startsWith("SGDuels") || p.getServer().getInfo().getName().startsWith("SoccerMC")) return;

        if(u.getQueue() != null) u.getQueue().leaveQueue(p,false);

        if(Party.hasParty(p)){
            if(Party.getParty(p).getOwner() == p){
                for(ProxiedPlayer all : Party.getParty(p).getMembers()){
                    if(all != p){
                        all.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.YELLOW + BungeeUser.getUser(all).getTranslatedMessage("The party is connecting to") + ": " + ChatColor.WHITE + p.getServer().getInfo().getName().toUpperCase()));
                        all.connect(p.getServer().getInfo());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onChat(ChatEvent e){
        ProxiedPlayer p = (ProxiedPlayer)e.getSender();
        BungeeUser u = BungeeUser.getUser(p);
        String msg = e.getMessage();

        boolean logged = false;

        if(!msg.startsWith("/")){
            u.getStoredChatMessages().add(new StoredChatMessage(msg,new Timestamp(System.currentTimeMillis()),p.getServer().getInfo().getName().toUpperCase()));
        }

        // CHAT FILTER
        if(!u.hasPermission(Rank.MOD)){
            boolean b = true;
            boolean autoMute = false;

            for(String s : BungeeChest.BADWORDS){
                if(msg.toLowerCase().contains(s.toLowerCase())){
                    b = false;
                }
            }

            if(!u.hasPermission(Rank.ADMIN)){
                String s = msg.split(" ")[0].toLowerCase();

                if(s.equals("/plugins") || s.equals("/bukkit:plugins") || s.equals("/pl") || s.equals("/bukkit:pl") || s.equals("/ver") || s.equals("/version") || s.equals("/icanhasbukkit") || s.equals("/bukkit:icanhasbukkit") || s.equals("/bukkit:ver") || s.equals("/bukkit:version") || s.equals("/bukkit:me") || s.equals("/minecraft:me") || s.equals("/me")){
                    e.setCancelled(true);
                    p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("You aren't permitted to execute this command.")));
                    return;
                }
            }

            // EXACT CHECK
            /*String replaced = msg.replaceAll("[^a-zA-Z0-9]+","");
            for(String word : replaced.split(" ")){
                for(String badword : BungeeChest.BADWORDS){
                    if(word.toLowerCase().contains(badword.toLowerCase())){
                        b = false;
                        autoMute = true;
                    }
                }
            }*/

            if(!b && !msg.startsWith("/register") && !msg.startsWith("/ts") && !msg.startsWith("/discord")){
                e.setCancelled(true);
                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Your message was filtered.")));
                u.filteredMessages++;

                if(u.filteredMessages == 10){
                    BungeeChest.getInstance().getProxy().getPluginManager().dispatchCommand(BungeeChest.getInstance().getProxy().getConsole(),"tempmute " + p.getName() + " 2 h Verbal Abuse (Automatic)");
                }

                if(!logged){
                    BungeeChest.logMessage(p,e.getMessage(),true);
                    logged = true;
                }

                return;
            }
        }

        if(!msg.startsWith("/")){
            if(BanUtilities.getMute(p.getUniqueId()) != null){
                Mute mute = BanUtilities.getMute(p.getUniqueId());

                if(!mute.isExpired()){
                    e.setCancelled(true);

                    if(mute.getReason() == null){
                        if(mute.getEndDate() == null){
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("You were muted.")));
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + u.getTranslatedMessage("No reason has been specified.")));
                            return;
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("You were muted.")));
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + u.getTranslatedMessage("No reason has been specified.")));
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u.getTranslatedMessage("Expiry") + ": " + ChatColor.WHITE + mute.getEndDate().toGMTString()));
                            return;
                        }
                    } else {
                        if(mute.getEndDate() == null){
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("You were muted.")));
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + mute.getReason()));
                            return;
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("You were muted.")));
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + mute.getReason()));
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u.getTranslatedMessage("Expiry") + ": " + ChatColor.WHITE + mute.getEndDate().toGMTString()));
                            return;
                        }
                    }
                } else {
                    mute.markAsExpired();
                }
            }

            if(u.getChatChannel() == ChatChannel.PARTY){
                e.setCancelled(true);

                Party party = Party.getParty(p);
                if(party != null){
                    BungeeChest.getInstance().getProxy().getPluginManager().dispatchCommand(p,"p " + msg);
                } else {
                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + BungeeChest.LINE_SEPERATOR));
                    u.sendCenteredMessage(ChatColor.GOLD + u.getTranslatedMessage("You are not in a party and have been moved back to the ALL channel."));
                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + BungeeChest.LINE_SEPERATOR));

                    u.setChatChannel(ChatChannel.ALL);
                }
            } else if(u.getChatChannel() == ChatChannel.CREW){
                e.setCancelled(true);

                Crew crew = u.getCrew();
                if(crew != null){
                    BungeeChest.getInstance().getProxy().getPluginManager().dispatchCommand(p,"c " + msg);
                } else {
                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + BungeeChest.LINE_SEPERATOR));
                    u.sendCenteredMessage(ChatColor.GOLD + u.getTranslatedMessage("You are not in a crew and have been moved back to the ALL channel."));
                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + BungeeChest.LINE_SEPERATOR));

                    u.setChatChannel(ChatChannel.ALL);
                }
            } else if(u.getChatChannel() == ChatChannel.STAFF){
                e.setCancelled(true);

                if(u.hasPermission(Rank.STAFF)){
                    BungeeChest.getInstance().getProxy().getPluginManager().dispatchCommand(p,"a " + msg);
                } else {
                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + BungeeChest.LINE_SEPERATOR));
                    u.sendCenteredMessage(ChatColor.GOLD + u.getTranslatedMessage("You are not a staff member have been moved back to the ALL channel."));
                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + BungeeChest.LINE_SEPERATOR));

                    u.setChatChannel(ChatChannel.ALL);
                }
            } else if(u.getChatChannel() == ChatChannel.ALL){
                // handle chat through Spigot
            }
        }

        if(!logged){
            BungeeChest.logMessage(p,e.getMessage(),false);
            logged = true;
        }
    }

    @EventHandler
    public void onQuit(PlayerDisconnectEvent e){
        ProxiedPlayer p = e.getPlayer();

        if(BungeeChest.ONSERV.contains(p.getName())){
            BungeeUser u = BungeeUser.getUser(p);
            BungeeChest.ONSERV.remove(p.getName());

            if(Party.getParty(p) != null) Party.getParty(p).leaveParty(p);
            if(u.getQueue() != null) u.getQueue().leaveQueue(p,false);

            // SEND STAFF QUIT MESSAGE
            if(u.hasPermission(Rank.STAFF)){
                for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff()){
                    if(all == p) continue;
                    BungeeUser a = BungeeUser.getUser(all);

                    all.sendMessage(TextComponent.fromLegacyText(BungeeChest.STAFFNOTIFY_PREFIX + a.getTranslatedMessage("%p is now offline.").replace("%p",u.getRank().getColor() + u.getRank().getName() + " " + p.getName() + ChatColor.RED)));
                }

                System.out.println("[STAFF QUIT] " + p.getName());
            }

            // SEND FRIENDS QUIT MESSAGE
            for(String s : u.getFriends()){
                if(BungeeChest.getInstance().getProxy().getPlayer(UUID.fromString(s)) != null){
                    ProxiedPlayer p2 = BungeeChest.getInstance().getProxy().getPlayer(UUID.fromString(s));
                    BungeeUser u2 = BungeeUser.getUser(p2);

                    p2.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "-" + ChatColor.DARK_GRAY + "] " + u.getRank().getColor() + p.getName()));
                }
            }

            BungeeChest.async(() -> {
                try {
                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("DELETE FROM `soccer_queue` WHERE `uuid` = ?");
                    ps.setString(1,p.getUniqueId().toString());
                    ps.executeUpdate();
                    ps.close();

                    ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `active_nicks` SET `active` = ? WHERE `uuid` = ? AND `active` = ?");
                    ps.setBoolean(1,false);
                    ps.setString(2,p.getUniqueId().toString());
                    ps.setBoolean(3,true);
                    ps.executeUpdate();
                    ps.close();

                    ps = MySQLManager.getInstance().getConnection().prepareStatement("DELETE FROM `nicknamer_data_nick` WHERE `_Key` = ?");
                    ps.setString(1,p.getUniqueId().toString());
                    ps.executeUpdate();
                    ps.close();

                    ps = MySQLManager.getInstance().getConnection().prepareStatement("DELETE FROM `nicknamer_data_skin` WHERE `_Key` = ?");
                    ps.setString(1,p.getUniqueId().toString());
                    ps.executeUpdate();
                    ps.close();
                } catch(Exception e1){
                    e1.printStackTrace();
                }
            });

            u.saveData();
        }
    }
}
