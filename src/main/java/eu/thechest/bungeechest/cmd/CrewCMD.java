package eu.thechest.bungeechest.cmd;

import eu.thechest.bungeechest.BungeeChest;
import eu.thechest.bungeechest.crews.Crew;
import eu.thechest.bungeechest.crews.CrewMember;
import eu.thechest.bungeechest.crews.CrewRank;
import eu.thechest.bungeechest.mysql.MySQLManager;
import eu.thechest.bungeechest.user.BungeeUser;
import eu.thechest.bungeechest.user.PlayerUtilities;
import eu.thechest.bungeechest.user.Rank;
import jdk.nashorn.internal.runtime.ECMAException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.lang.ref.PhantomReference;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;
import java.util.regex.Pattern;

import static eu.thechest.bungeechest.util.DefaultFontInfo.s;

/**
 * Created by zeryt on 19.03.2017.
 */
public class CrewCMD extends Command {
    public CrewCMD(){
        super("crew");
    }

    private void sendUsage(BungeeUser u){
        ProxiedPlayer p = u.getProxiedPlayer();

        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.YELLOW + "/crew create <" + u.getTranslatedMessage("Name") + ">"));
        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.YELLOW + "/crew invite <" + u.getTranslatedMessage("Player") + ">"));
        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.YELLOW + "/crew accept <Crew ID>"));
        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.YELLOW + "/crew leave"));
        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.YELLOW + "/crew kick <" + u.getTranslatedMessage("Player") + ">"));
        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.YELLOW + "/crew tag <" + u.getTranslatedMessage("New Tag") + ">"));
        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.YELLOW + "/crew info"));
        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.YELLOW + "/crew members"));
        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.YELLOW + "/crew party"));
        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.YELLOW + "/c <" + u.getTranslatedMessage("Message") + ">"));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer)sender;
            BungeeUser u = BungeeUser.getUser(p);

            if(args.length == 0){
                sendUsage(u);
            } else if(args.length == 1) {
                if(args[0].equalsIgnoreCase("leave")){
                    if(u.getCrew() != null){
                        u.getCrew().leaveCrew(p.getUniqueId());
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("You are not in a crew.")));
                    }
                } else if(args[0].equalsIgnoreCase("info")){
                    if(u.getCrew() != null){
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + BungeeChest.LINE_SEPERATOR));
                        u.sendCenteredMessage(ChatColor.GREEN + u.getCrew().getName());
                        p.sendMessage(TextComponent.fromLegacyText(" "));
                        p.sendMessage(TextComponent.fromLegacyText("   " + ChatColor.YELLOW + u.getTranslatedMessage("Coins") + ": " + ChatColor.WHITE + u.getCrew().getCoins()));
                        p.sendMessage(TextComponent.fromLegacyText("   " + ChatColor.YELLOW + u.getTranslatedMessage("Creator") + ": " + ChatColor.WHITE + PlayerUtilities.getRankFromUUID(u.getCrew().getCreator()).getColor() + PlayerUtilities.getNameFromUUID(u.getCrew().getCreator())));
                        p.sendMessage(TextComponent.fromLegacyText("   " + ChatColor.YELLOW + u.getTranslatedMessage("Creation date") + ": " + ChatColor.WHITE + u.getCrew().getTimeCreated().toGMTString()));
                        p.sendMessage(new ComponentBuilder("   ").append("[" + u.getTranslatedMessage("Click to list members") + "]").color(ChatColor.GRAY).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/crew members")).create());
                        p.sendMessage(TextComponent.fromLegacyText(" "));
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + BungeeChest.LINE_SEPERATOR));
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("You are not in a crew.")));
                    }
                } else if(args[0].equalsIgnoreCase("members")){
                    BungeeChest.getInstance().getProxy().getPluginManager().dispatchCommand(p,"crew members 1");
                } else if(args[0].equalsIgnoreCase("party")){
                    if(u.getCrew() != null){
                        if(u.getCrew().isCrewLeader(p.getUniqueId())){
                            if(u.hasPermission(Rank.TITAN)){
                                for(ProxiedPlayer a : u.getCrew().getOnlineMembers()){
                                    if(a != p) BungeeChest.getInstance().getProxy().getPluginManager().dispatchCommand(p,"party invite " + a.getName());
                                }
                            } else {
                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("Purchase %r at %l to be able to do this.").replace("%r",Rank.TITAN.getColor() + Rank.TITAN.getName()).replace("%l",ChatColor.YELLOW + "https://store.thechest.eu" + ChatColor.RED)));
                            }
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("Only the crew leader can do that.")));
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("You are not in a crew.")));
                    }
                } else {
                    sendUsage(u);
                }
            } else if(args.length == 2){
                if(args[0].equalsIgnoreCase("invite")){
                    if(u.getCrew() != null){
                        if(u.getCrew().isCrewLeader(p.getUniqueId())){
                            ProxiedPlayer p2 = BungeeChest.getInstance().getProxy().getPlayer(args[1]);

                            if(p2 != null){
                                BungeeUser u2 = BungeeUser.getUser(p2);

                                if(u2.getCrew() == null){
                                    if(!u.getCrew().getInvitedPlayers().contains(p2.getName())){
                                        if(true){ // TODO: Check for crew invitation setting
                                            u.getCrew().getInvitedPlayers().add(p2.getName());

                                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.GREEN + u.getTranslatedMessage("The request has been sent.")));

                                            /*p2.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + BungeeChest.LINE_SEPERATOR));
                                            u2.sendCenteredMessage(ChatColor.YELLOW + u2.getTranslatedMessage("Crew invitation"));
                                            u2.sendCenteredMessage(ChatColor.WHITE + u.getCrew().getName());
                                            p2.sendMessage(TextComponent.fromLegacyText(""));
                                            p2.sendMessage(new ComponentBuilder("  ").append(u2.getTranslatedMessage("Click") + ": ").color(ChatColor.AQUA).append("[" + u2.getTranslatedMessage("ACCEPT") + "]").color(ChatColor.DARK_GREEN).bold(true).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/crew accept " + u.getCrew().getID())).create());
                                            p2.sendMessage(TextComponent.fromLegacyText(""));
                                            p2.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + BungeeChest.LINE_SEPERATOR));*/
                                            p2.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " +  ChatColor.AQUA + u2.getTranslatedMessage("You've been invited to %c!").replace("%c",ChatColor.YELLOW + u.getCrew().getName() + ChatColor.GREEN)));
                                            p2.sendMessage(new ComponentBuilder("[CREW] ").color(ChatColor.DARK_GREEN).append(u2.getTranslatedMessage("Click") + ": ").color(ChatColor.AQUA).append("[" + u2.getTranslatedMessage("ACCEPT") + "]").color(ChatColor.DARK_GREEN).bold(true).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/crew accept " + u.getCrew().getID())).create());
                                        } else {
                                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("That player does not allow crew requests.")));
                                        }
                                    } else {
                                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("You've already invited that player.")));
                                    }
                                } else {
                                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("That player is already in a crew.")));
                                }
                            } else {
                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("That player is not online.")));
                            }
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("Only the crew leader can do that.")));
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("You are not in a crew.")));
                    }
                } else if(args[0].equalsIgnoreCase("accept")){
                    if(u.getCrew() == null){
                        if(BungeeChest.getInstance().isValidInteger(args[1])){
                            int id = Integer.parseInt(args[1]);

                            Crew c = Crew.getCrew(id);

                            if(c != null){
                                if(c.getInvitedPlayers().contains(p.getName())){
                                    c.getInvitedPlayers().remove(p.getName());
                                    c.joinCrew(p);
                                } else {
                                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("That crew hasn't invited you.")));
                                }
                            } else {
                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("Unknown crew.")));
                            }
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("Please enter a valid integer.")));
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("You are already in a crew.")));
                    }
                } else if(args[0].equalsIgnoreCase("kick")){
                    if(u.getCrew() != null) {
                        if(u.getCrew().isCrewLeader(p.getUniqueId())){
                            UUID uuid = PlayerUtilities.getUUIDFromName(args[1]);

                            if(uuid != null){
                                if(u.getCrew().isMember(uuid)){
                                    for(ProxiedPlayer all : u.getCrew().getOnlineMembers()){
                                        BungeeUser a = BungeeUser.getUser(all);
                                        all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("%p was kicked from the crew.").replace("%p",PlayerUtilities.getRankFromUUID(uuid).getColor() + PlayerUtilities.getNameFromUUID(uuid) + ChatColor.RED)));
                                    }
                                    u.getCrew().leaveCrew(uuid);
                                } else {
                                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("That player is not in your crew.")));
                                }
                            } else {
                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("Unknown UUID.")));
                            }
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("Only the crew leader can do that.")));
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("You are not in a crew.")));
                    }
                } else if(args[0].equalsIgnoreCase("tag")){
                    if(u.getCrew() != null){
                        if(u.getCrew().isCrewLeader(p.getUniqueId())){
                            BungeeChest.async(() -> {
                                String newTag = args[1].toUpperCase();
                                if(newTag.length() > 5) newTag = newTag.substring(0,4);

                                Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
                                boolean hasSpecialChar = pattern.matcher(newTag).find();

                                if(!hasSpecialChar){
                                    if(u.getCrew().getTag() == null || (u.getCrew().getTag() != null && !u.getCrew().getTag().equalsIgnoreCase(newTag))){
                                        boolean tagAvailable = true;

                                        try {
                                            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `crews` WHERE `tag` = ?");
                                            ps.setString(1,newTag);
                                            ResultSet rs = ps.executeQuery();
                                            tagAvailable = !rs.first();
                                            MySQLManager.getInstance().closeResources(rs,ps);

                                            for(Crew c : Crew.STORAGE.values()){
                                                if(c != u.getCrew() && c.getTag() != null){
                                                    if(c.getTag().equalsIgnoreCase(newTag)){
                                                        tagAvailable = false;
                                                    }
                                                }
                                            }

                                            if(tagAvailable){
                                                u.getCrew().setTag(newTag);
                                                u.getCrew().saveData();

                                                for(ProxiedPlayer all : u.getCrew().getOnlineMembers()){
                                                    BungeeUser a = BungeeUser.getUser(all);

                                                    all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.GREEN + a.getTranslatedMessage("The crew tag was changed to %t.").replace("%t",ChatColor.GRAY + newTag + ChatColor.GREEN)));
                                                }
                                            } else {
                                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("That tag is already in use.")));
                                            }
                                        } catch (Exception e){
                                            e.printStackTrace();
                                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("An error occured.")));
                                        }
                                    } else {
                                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("You already have that tag equipped.")));
                                    }
                                } else {
                                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("That tag contains invalid characters.")));
                                }
                            });
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("Only the crew leader can do that.")));
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("You are not in a crew.")));
                    }
                } else if(args[0].equalsIgnoreCase("members")){
                    if(u.getCrew() != null){
                        if(u.getCrew().getMembers().size() > 0){
                            int sizePerPage = 7;
                            int page = 0;

                            if(BungeeChest.getInstance().isValidInteger(args[1])){
                                page = Integer.parseInt(args[1]);
                                page -= 1;

                                ArrayList<CrewMember> members = u.getCrew().getMembers();
                                Collections.sort(members, new Comparator<CrewMember>() {
                                    public int compare(CrewMember p1, CrewMember p2) {
                                        return BungeeChest.getInstance().convertBooleanToInteger(BungeeChest.getInstance().getProxy().getPlayer(p1.uuid) == null) - BungeeChest.getInstance().convertBooleanToInteger(BungeeChest.getInstance().getProxy().getPlayer(p2.uuid) == null);
                                    }
                                });

                                double d = members.size()/sizePerPage;
                                Integer maxPages = ((Double)d).intValue();
                                if(maxPages == 0) maxPages = 1;

                                int from = Math.max(0,page*sizePerPage);
                                int to = Math.min(members.size(),(page+1)*sizePerPage);

                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + BungeeChest.LINE_SEPERATOR));
                                u.sendCenteredMessage(ChatColor.GREEN + u.getTranslatedMessage("Crew members") + " (" + (page+1) + "/" + maxPages + ")");
                                try {
                                    for(CrewMember s : members.subList(from,to)){
                                        if(BungeeChest.getInstance().getProxy().getPlayer(s.uuid) != null){
                                            ProxiedPlayer friend = BungeeChest.getInstance().getProxy().getPlayer(s.uuid);
                                            BungeeUser u2 = BungeeUser.getUser(friend);

                                            p.sendMessage(new ComponentBuilder("  " + friend.getName()).color(u2.getRank().getColor()).append(">").color(ChatColor.GRAY).append(" " + u.getTranslatedMessage("Online on") + " ").color(ChatColor.GREEN).append(friend.getServer().getInfo().getName().toUpperCase()).color(ChatColor.YELLOW).create());
                                        } else {
                                            String friend = PlayerUtilities.getNameFromUUID(s.uuid);
                                            p.sendMessage(new ComponentBuilder("  " + friend).color(PlayerUtilities.getRankFromUUID(s.uuid).getColor()).append(">").color(ChatColor.GRAY).append(" OFFLINE").color(ChatColor.RED).create());
                                        }
                                    }
                                } catch(IllegalArgumentException e){}
                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + BungeeChest.LINE_SEPERATOR));
                            } else {
                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("Please enter a valid number.")));
                            }
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("An error occured.")));
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("You are not in a crew.")));
                    }
                } else {
                    if(args[0].equalsIgnoreCase("create")){
                        if(u.getCrew() == null){
                            if(u.hasPermission(Rank.PRO)){
                                BungeeChest.async(() -> {
                                    StringBuilder sb = new StringBuilder("");
                                    for (int i = 1; i < args.length; i++) {
                                        sb.append(args[i]).append(" ");
                                    }
                                    String name = sb.toString().trim();

                                    if(name.length() > 16) name = name.substring(0,15);

                                    Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
                                    boolean hasSpecialChar = pattern.matcher(name).find();

                                    if(!hasSpecialChar){
                                        boolean nameAvailable = true;

                                        try {
                                            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `crews` WHERE `name` = ?");
                                            ps.setString(1,name);
                                            ResultSet rs = ps.executeQuery();
                                            nameAvailable = !rs.first();
                                            MySQLManager.getInstance().closeResources(rs,ps);
                                        } catch(Exception e){
                                            e.printStackTrace();
                                        }

                                        for(Crew cr : Crew.STORAGE.values()){
                                            if(cr.getName().equalsIgnoreCase(name)){
                                                nameAvailable = false;
                                            }
                                        }

                                        if(nameAvailable){
                                            int insertID = -1;

                                            try {
                                                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `crews` (`name`,`creator`) VALUES(?,?);", Statement.RETURN_GENERATED_KEYS);
                                                ps.setString(1,name);
                                                ps.setString(2,p.getUniqueId().toString());
                                                ps.execute();
                                                ResultSet rs = ps.getGeneratedKeys();
                                                if(rs.next()) insertID = rs.getInt(1);
                                                MySQLManager.getInstance().closeResources(rs,ps);

                                                if(insertID != -1){
                                                    ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `crew_members` (`uuid`,`crewID`,`rank`) VALUES(?,?,?);");
                                                    ps.setString(1,p.getUniqueId().toString());
                                                    ps.setInt(2,insertID);
                                                    ps.setString(3,"LEADER");
                                                    ps.execute();
                                                    ps.close();

                                                    Crew finalCrew = Crew.getCrew(insertID);
                                                    u.updateCrew(finalCrew);

                                                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.GREEN + u.getTranslatedMessage("The crew has been created.")));
                                                } else {
                                                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("An error occured.")));
                                                }
                                            } catch(Exception e){
                                                e.printStackTrace();
                                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("An error occured.")));
                                            }
                                        } else {
                                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("That name is already in use.")));
                                        }
                                    } else {
                                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("That name contains invalid characters.")));
                                    }
                                });
                            } else {
                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("Purchase %r at %l to be able to do this.").replace("%r",Rank.PRO.getColor() + Rank.PRO.getName() + ChatColor.RED).replace("%l",ChatColor.YELLOW + "https://store.thechest.eu" + ChatColor.RED)));
                            }
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "[CREW] " + ChatColor.RED + u.getTranslatedMessage("You are already in a crew.")));
                        }
                    } else {
                        sendUsage(u);
                    }
                }
            } else {
                sendUsage(u);
            }
        } else {
            sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "You must be a player to do this!"));
        }
    }
}
