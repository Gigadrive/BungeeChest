package eu.thechest.bungeechest.cmd;

import eu.thechest.bungeechest.BungeeChest;
import eu.thechest.bungeechest.mysql.MySQLManager;
import eu.thechest.bungeechest.user.BungeeUser;
import eu.thechest.bungeechest.user.PlayerUtilities;
import eu.thechest.bungeechest.user.Rank;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by zeryt on 23.02.2017.
 */
public class Friend extends Command {
    public Friend(){
        super("friends",null,new String[]{"friend","f","buddy"});
    }

    private void sendUsage(ProxiedPlayer p){
        BungeeUser u = BungeeUser.getUser(p);

        p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.YELLOW + "/friend add <" + u.getTranslatedMessage("Player") + ">"));
        p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.YELLOW + "/friend remove <" + u.getTranslatedMessage("Player") + ">"));
        p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.YELLOW + "/friend follow <" + u.getTranslatedMessage("Player") + ">"));
        p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.YELLOW + "/friend accept <" + u.getTranslatedMessage("Player") + ">"));
        p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.YELLOW + "/friend deny <" + u.getTranslatedMessage("Player") + ">"));
        p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.YELLOW + "/friend list [" + u.getTranslatedMessage("Page") + "]"));
        p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.YELLOW + "/friend requests"));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        //BaseComponent[] usage = TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "/friend <add|remove|list|requests|info|follow|accept|deny> [arguments]");

        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer)sender;
            BungeeUser u = BungeeUser.getUser(p);

            if(args.length == 0){
                sendUsage(p);
            } else if(args.length == 1){
                if(args[0].equalsIgnoreCase("requests")){
                    BungeeChest.getInstance().getProxy().getPluginManager().dispatchCommand(p,"friend requests 1");
                } else if(args[0].equalsIgnoreCase("list")){
                    BungeeChest.getInstance().getProxy().getPluginManager().dispatchCommand(p,"friend list 1");
                } else {
                    sendUsage(p);
                }
            } else if(args.length == 2){
                if(args[0].equalsIgnoreCase("requests")){
                    if(u.getFriendRequests().size() > 0){
                        int sizePerPage = 7;
                        int page = 0;

                        if(BungeeChest.getInstance().isValidInteger(args[1])){
                            page = Integer.parseInt(args[1]);

                            ArrayList<String> requests = u.getFriendRequests();

                            double d = ((double)requests.size())/((double)sizePerPage);
                            Integer maxPages = ((Double)d).intValue();
                            if(d > maxPages) maxPages = ((Double)d).intValue()+1;
                            if(maxPages == 0) maxPages = 1;

                            page--;

                            int from = Math.max(0,page*sizePerPage);
                            int to = Math.min(requests.size(),(page+1)*sizePerPage);

                            page++;

                            if(page > maxPages || page < 1){
                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("Please enter a valid number.")));
                            } else {
                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + BungeeChest.LINE_SEPERATOR));
                                u.sendCenteredMessage(ChatColor.YELLOW + u.getTranslatedMessage("Friend requests") + " (" + page + "/" + maxPages + ")");
                                try {
                                    for(String s : requests.subList(from,to)){
                                        String name = PlayerUtilities.getNameFromUUID(s);
                                        Rank rank = PlayerUtilities.getRankFromUUID(s);

                                        p.sendMessage(new ComponentBuilder(name).color(rank.getColor()).append(" ").append("- ").color(ChatColor.AQUA).append(" ").append("[" + u.getTranslatedMessage("ACCEPT") + "]").bold(true).color(ChatColor.GREEN).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/friend accept " + name)).append(" ").append("[" + u.getTranslatedMessage("DENY") + "]").color(ChatColor.RED).bold(true).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/friend deny " + name)).create());
                                    }
                                } catch(IllegalArgumentException e){}
                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + BungeeChest.LINE_SEPERATOR));
                            }
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("Please enter a valid number.")));
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("You don't have any open friend requests.")));
                    }
                } else if(args[0].equalsIgnoreCase("list")){
                    if(u.getFriends().size() > 0){
                        int sizePerPage = 7;
                        int page = 0;

                        if(BungeeChest.getInstance().isValidInteger(args[1])){
                            page = Integer.parseInt(args[1]);

                            ArrayList<String> friends = u.getFriends();
                            Collections.sort(friends, new Comparator<String>() {
                                public int compare(String p1, String p2) {
                                    return BungeeChest.getInstance().convertBooleanToInteger(BungeeChest.getInstance().getProxy().getPlayer(UUID.fromString(p1)) == null) - BungeeChest.getInstance().convertBooleanToInteger(BungeeChest.getInstance().getProxy().getPlayer(UUID.fromString(p2)) == null);
                                }
                            });

                            double d = ((double)friends.size())/((double)sizePerPage);
                            Integer maxPages = ((Double)d).intValue();
                            if(d > maxPages) maxPages++;
                            if(maxPages == 0) maxPages = 1;

                            page--;

                            int from = Math.max(0,page*sizePerPage);
                            int to = Math.min(friends.size(),(page+1)*sizePerPage);

                            page++;

                            if(page > maxPages || page < 1){
                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("Please enter a valid number.")));
                            } else {
                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + BungeeChest.LINE_SEPERATOR));
                                u.sendCenteredMessage(ChatColor.YELLOW + u.getTranslatedMessage("Friends") + " (" + page + "/" + maxPages + ")");
                                try {
                                    for(String s : friends.subList(from,to)){
                                        if(BungeeChest.getInstance().getProxy().getPlayer(UUID.fromString(s)) != null){
                                            ProxiedPlayer friend = BungeeChest.getInstance().getProxy().getPlayer(UUID.fromString(s));
                                            BungeeUser u2 = BungeeUser.getUser(friend);

                                            p.sendMessage(new ComponentBuilder("  " + friend.getName()).color(u2.getRank().getColor()).append(">").color(ChatColor.GRAY).append(" " + u.getTranslatedMessage("Online on") + " ").color(ChatColor.GREEN).append(friend.getServer().getInfo().getName().toUpperCase()).color(ChatColor.YELLOW).create());
                                        } else {
                                            String friend = PlayerUtilities.getNameFromUUID(s);
                                            p.sendMessage(new ComponentBuilder("  " + friend).color(PlayerUtilities.getRankFromUUID(UUID.fromString(s)).getColor()).append(">").color(ChatColor.GRAY).append(" OFFLINE").color(ChatColor.RED).create());
                                        }
                                    }
                                } catch(IllegalArgumentException e){}
                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + BungeeChest.LINE_SEPERATOR));
                            }
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("Please enter a valid number.")));
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("You don't have any friends.") + " :("));
                    }
                } else if(args[0].equalsIgnoreCase("add")){
                    String name = args[1];
                    UUID uuid = PlayerUtilities.getUUIDFromName(name);

                    if(uuid != null){
                        if(!name.equalsIgnoreCase(p.getName()) && uuid != p.getUniqueId()){
                            if(!u.getFriends().contains(uuid.toString())){
                                if(u.getFriendRequests().contains(uuid.toString())){
                                    BungeeChest.getInstance().getProxy().getPluginManager().dispatchCommand(p,"friend accept " + name);
                                } else {
                                    ProxiedPlayer p2 = null;
                                    BungeeUser u2 = null;

                                    if(BungeeChest.getInstance().getProxy().getPlayer(name) != null){
                                        p2 = BungeeChest.getInstance().getProxy().getPlayer(name);
                                        u2 = BungeeUser.getUser(p2);

                                        if(u2.getFriendRequests().contains(p.getUniqueId().toString())){
                                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("You've already sent a friend request to that player.")));
                                            return;
                                        }
                                    } else {
                                        try {
                                            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `friend_requests` WHERE `fromUUID` = ? AND `toUUID` = ?");
                                            ps.setString(1,p.getUniqueId().toString());
                                            ps.setString(2,uuid.toString());

                                            ResultSet rs = ps.executeQuery();

                                            if(rs.first()){
                                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("You've already sent a friend request to that player.")));
                                                MySQLManager.getInstance().closeResources(rs,ps);
                                                return;
                                            }

                                            MySQLManager.getInstance().closeResources(rs,ps);
                                        } catch (Exception e){
                                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("An error occured.")));
                                            e.printStackTrace();
                                            return;
                                        }
                                    }

                                    if(u.hasPermission(Rank.SR_MOD) || (!u.hasPermission(Rank.SR_MOD) && PlayerUtilities.allowsFriendRequests(uuid))){
                                        try {
                                            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `friend_requests` (`fromUUID`,`toUUID`) VALUES(?,?)");
                                            ps.setString(1,p.getUniqueId().toString());
                                            ps.setString(2,uuid.toString());
                                            ps.execute();
                                            ps.close();
                                        } catch(Exception e){
                                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("An error occured.")));
                                            e.printStackTrace();
                                            return;
                                        }

                                        if(p2 != null && u2 != null){
                                            name = p2.getName();
                                            u2.getFriendRequests().add(p.getUniqueId().toString());

                                /*p2.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + BungeeChest.LINE_SEPERATOR));
                                p2.sendMessage(TextComponent.fromLegacyText(" "));
                                if(u.getRank().getPrefix() != null){
                                    u2.sendCenteredMessage(ChatColor.YELLOW + u2.getTranslatedMessage("Friend request from") + " " + u.getRank().getColor() + "[" + u.getRank().getPrefix() + "] " + p.getName());
                                } else {
                                    u2.sendCenteredMessage(ChatColor.YELLOW + u2.getTranslatedMessage("Friend request from") + " " + u.getRank().getColor() + p.getName());
                                }
                                p2.sendMessage(TextComponent.fromLegacyText(" "));
                                p2.sendMessage(new ComponentBuilder("   " + "[" + u2.getTranslatedMessage("ACCEPT") + "]  ").color(ChatColor.DARK_GREEN).bold(true).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend accept " + p.getName())).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GREEN + u2.getTranslatedMessage("Accept") + ""))).append("[" + u2.getTranslatedMessage("DENY") + "]").color(ChatColor.DARK_RED).bold(true).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend deny " + p.getName())).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.RED + u2.getTranslatedMessage("DENY") + ""))).create());
                                p2.sendMessage(TextComponent.fromLegacyText(" "));
                                p2.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + BungeeChest.LINE_SEPERATOR));*/
                                            /*if(u.getRank().getPrefix() != null){
                                                p2.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.YELLOW + u.getTranslatedMessage("%p wants to be your friend.").replace("%p",u.getRank().getColor() + "[" + u.getRank().getPrefix() + "] " + p.getName() + ChatColor.YELLOW)));
                                            } else {
                                                p2.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.YELLOW + u.getTranslatedMessage("%p wants to be your friend.").replace("%p",u.getRank().getColor() + p.getName() + ChatColor.YELLOW)));
                                            }
                                            p2.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.GREEN + u.getTranslatedMessage("Accept with: /friend accept %p").replace("%p",u.getRank().getColor() + p.getName() + ChatColor.YELLOW)));
                                            p2.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("Deny with: /friend deny %p").replace("%p",u.getRank().getColor() + p.getName() + ChatColor.YELLOW)));*/
                                            p2.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.YELLOW + u2.getTranslatedMessage("%p wants to be your friend.").replace("%p", u.getRank().getColor() + p.getName() + ChatColor.YELLOW)));
                                            p2.sendMessage(new ComponentBuilder("[FRIENDS] ").color(ChatColor.GOLD).append("[" + u2.getTranslatedMessage("ACCEPT") + "]  ").color(ChatColor.DARK_GREEN).bold(true).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend accept " + p.getName())).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GREEN + u2.getTranslatedMessage("ACCEPT") + ""))).append("[" + u2.getTranslatedMessage("DENY") + "]").color(ChatColor.DARK_RED).bold(true).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend deny " + p.getName())).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.RED + u2.getTranslatedMessage("DENY") + ""))).create());
                                        }

                                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.GREEN + u.getTranslatedMessage("You've successfully sent a friend request to %p.").replace("%p",ChatColor.YELLOW + name + ChatColor.GREEN)));
                                    } else {
                                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("That player does not allow friend requests.")));
                                    }
                                }
                            } else {
                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("You're already friends with that player.")));
                            }
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("You can't add yourself as a friend.")));
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("Unknown UUID.")));
                    }
                } else if(args[0].equalsIgnoreCase("remove")){
                    String friendName = args[1];
                    UUID uuid = PlayerUtilities.getUUIDFromName(friendName);

                    if(uuid != null){
                        if(u.getFriends().contains(uuid.toString())){
                            try {
                                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("DELETE FROM `friendships` WHERE (`player` = ? AND `friend` = ?) OR (`player` = ? AND `friend` = ?)");
                                ps.setString(1,p.getUniqueId().toString());
                                ps.setString(2,uuid.toString());
                                ps.setString(3,uuid.toString());
                                ps.setString(4,p.getUniqueId().toString());
                                ps.execute();
                                ps.close();
                            } catch(Exception e){
                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("An error occured.")));
                                e.printStackTrace();
                                return;
                            }

                            u.getFriends().remove(uuid.toString());
                            ProxiedPlayer p2 = BungeeChest.getInstance().getProxy().getPlayer(uuid);

                            if(p2 != null){
                                friendName = p2.getName();
                                BungeeUser u2 = BungeeUser.getUser(p2);

                                u2.getFriends().remove(p.getUniqueId().toString());
                                p2.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("%p has removed you from their friends list.").replace("%p",ChatColor.YELLOW + p.getName() + ChatColor.RED)));
                            }

                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("You have removed %p from your friends list.").replace("%p",ChatColor.YELLOW + friendName + ChatColor.RED)));
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("You aren't friends with that player.")));
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("Unknown UUID.")));
                    }
                } else if(args[0].equalsIgnoreCase("follow")||args[0].equalsIgnoreCase("jump")){
                    String friendName = args[1];
                    UUID uuid = PlayerUtilities.getUUIDFromName(friendName);

                    if(uuid != null){
                        if(u.getFriends().contains(uuid.toString())){
                            if(BungeeChest.getInstance().getProxy().getPlayer(uuid) != null){
                                p.connect(BungeeChest.getInstance().getProxy().getPlayer(uuid).getServer().getInfo());

                                BungeeChest.getInstance().getProxy().getScheduler().schedule(BungeeChest.getInstance(), new Runnable(){
                                    @Override
                                    public void run(){
                                        u.achieve(33);
                                    }
                                }, 2, TimeUnit.SECONDS);
                            } else {
                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("That player is not online.")));
                            }
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("You aren't friends with that player.")));
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("Unknown UUID.")));
                    }
                } else if(args[0].equalsIgnoreCase("accept")){
                    String friendName = args[1];
                    UUID uuid = PlayerUtilities.getUUIDFromName(friendName);

                    if(uuid != null){
                        if(!u.getFriends().contains(uuid.toString())){
                            if(u.getFriendRequests().contains(uuid.toString())){
                                try {
                                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `friendships` (`player`,`friend`) VALUES(?,?)");
                                    ps.setString(1,p.getUniqueId().toString());
                                    ps.setString(2,uuid.toString());
                                    ps.execute();
                                    ps.close();
                                } catch(Exception e){
                                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("An error occured.")));
                                    e.printStackTrace();
                                    return;
                                }

                                try {
                                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("DELETE FROM `friend_requests` WHERE `fromUUID` = ? AND `toUUID` = ?");
                                    ps.setString(1,uuid.toString());
                                    ps.setString(2,p.getUniqueId().toString());
                                    ps.execute();
                                    ps.close();
                                } catch(Exception e){
                                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("An error occured.")));
                                    e.printStackTrace();
                                    return;
                                }

                                u.getFriends().add(uuid.toString());
                                u.getFriendRequests().remove(uuid.toString());

                                ProxiedPlayer p2 = BungeeChest.getInstance().getProxy().getPlayer(uuid);

                                if(p2 != null){
                                    BungeeUser u2 = BungeeUser.getUser(p2);
                                    friendName = p2.getName();

                                    u2.getFriends().add(p.getUniqueId().toString());
                                    p2.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.GREEN + u.getTranslatedMessage("%p has accepted your friend request.").replace("%p",ChatColor.YELLOW + p.getName() + ChatColor.GREEN)));
                                }

                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.GREEN + u.getTranslatedMessage("You are now friends with %p.").replace("%p",ChatColor.YELLOW + friendName + ChatColor.GREEN)));
                            } else {
                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("You don't have a friend request from that player.")));
                            }
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("You are already friends with that player.")));
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("Unknown UUID.")));
                    }
                } else if(args[0].equalsIgnoreCase("deny")){
                    String friendName = args[1];
                    UUID uuid = PlayerUtilities.getUUIDFromName(friendName);

                    if(uuid != null){
                        if(!u.getFriends().contains(uuid.toString())){
                            if(u.getFriendRequests().contains(uuid.toString())){
                                try {
                                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("DELETE FROM `friend_requests` WHERE `fromUUID` = ? AND `toUUID` = ?");
                                    ps.setString(1,uuid.toString());
                                    ps.setString(2,p.getUniqueId().toString());
                                    ps.execute();
                                    ps.close();
                                } catch(Exception e){
                                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("An error occured.")));
                                    e.printStackTrace();
                                    return;
                                }

                                u.getFriendRequests().remove(uuid.toString());
                                ProxiedPlayer p2 = BungeeChest.getInstance().getProxy().getPlayer(uuid);

                                if(p2 != null){
                                    BungeeChest.getInstance().getProxy().getPlayer(uuid);
                                    friendName = BungeeChest.getInstance().getProxy().getPlayer(uuid).getName();
                                    BungeeUser u2 = BungeeUser.getUser(p2);

                                    p2.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("%p has denied your friend request.").replace("%p",ChatColor.YELLOW + p.getName() + ChatColor.RED)));
                                }

                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("You have denied %p's friend request.").replace("%p",ChatColor.YELLOW + friendName + ChatColor.RED)));
                                u.achieve(32);
                            } else {
                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("You don't have a friend request from that player.")));
                            }
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("You are already friends with that player.")));
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + u.getTranslatedMessage("Unknown UUID.")));
                    }
                } else {
                    sendUsage(p);
                }
            } else {
                sendUsage(p);
            }
        } else {
            sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[FRIENDS] " + ChatColor.RED + "You must be a player to do this!"));
        }
    }
}
