package eu.thechest.bungeechest.cmd;

import com.google.gson.Gson;
import eu.thechest.bungeechest.BungeeChest;
import eu.thechest.bungeechest.mysql.MySQLManager;
import eu.thechest.bungeechest.user.BungeeUser;
import eu.thechest.bungeechest.user.PlayerUtilities;
import eu.thechest.bungeechest.user.Rank;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by zeryt on 14.07.2017.
 */
public class ReportCMD extends Command {
    public ReportCMD(){
        super("report");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer)sender;
            BungeeUser u = BungeeUser.getUser(p);

            if(args.length == 2){
                String name = args[0];
                String reason = args[1];

                String[] validReasons = new String[]{"HACKING","TEAMING","BOOSTING","CHATABUSE"};

                ProxiedPlayer p2 = BungeeChest.getInstance().getProxy().getPlayer(name);

                if(p2 != null){
                    BungeeUser u2 = BungeeUser.getUser(p2);
                    UUID uuid = p2.getUniqueId();
                    boolean b = false;

                    for(String s : validReasons) if(s.equalsIgnoreCase(reason)) b = true;

                    if(b){
                        reason = reason.toUpperCase();

                        if(!u.reported.contains(p2.getUniqueId().toString())){
                            if(p != p2){
                                if((!reason.equals("CHATABUSE")) || (reason.equals("CHATABUSE") && u2.getStoredChatMessages() != null && u2.getStoredChatMessages().size() > 0)){
                                    boolean noStaffOnline = PlayerUtilities.getOnlineStaff(Rank.MOD).size()==0;

                                    final String finalReason = reason;

                                    BungeeChest.async(() -> {
                                        try {
                                            int chatLogID = 0;
                                            if(finalReason.equals("CHATABUSE")){
                                                Gson gson = new Gson();
                                                String messagesJson = gson.toJson(u2.getStoredChatMessages());

                                                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `chatlogs` (`uuid`,`createdBy`,`messages`) VALUES(?,?,?);", Statement.RETURN_GENERATED_KEYS);
                                                ps.setString(1,p2.getUniqueId().toString());
                                                ps.setString(2,p.getUniqueId().toString());
                                                ps.setString(3,messagesJson);
                                                ps.executeUpdate();

                                                ResultSet rs = ps.getGeneratedKeys();
                                                if(rs.first()) {
                                                    chatLogID = rs.getInt(1);
                                                }

                                                MySQLManager.getInstance().closeResources(rs,ps);
                                            }

                                            if(finalReason.equals("CHATABUSE") && chatLogID == 0){
                                                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("An error occured.")));
                                                return;
                                            }

                                            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `sn_reports` (`reportedPlayer`,`reason`,`reportedBy`,`nameWhenReported`,`server`,`chatLogID`) VALUES(?,?,?,?,?,?);");
                                            ps.setString(1,uuid.toString());
                                            ps.setString(2,finalReason);
                                            ps.setString(3,p.getUniqueId().toString());
                                            ps.setString(4,p2.getName());
                                            ps.setString(5,p2.getServer().getInfo().getName().toUpperCase());
                                            ps.setInt(6,chatLogID);
                                            ps.executeUpdate();
                                            ps.close();

                                            u.reported.add(p2.getUniqueId().toString());

                                            if(noStaffOnline){
                                                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + u.getTranslatedMessage("You have sucessfully reported the player %p with reason %r!").replace("%p",ChatColor.YELLOW + p2.getName() + ChatColor.GREEN).replace("%r",ChatColor.YELLOW + finalReason + ChatColor.GREEN)));
                                                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("There are currently no staff members online. Your report has been logged.")));
                                            } else {
                                                for(ProxiedPlayer s : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                                    BungeeUser ss = BungeeUser.getUser(s);

                                                    if(chatLogID == 0){
                                                        s.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "[REPORTS] " + ChatColor.YELLOW + ss.getTranslatedMessage("%p reported %c with reason %r. (%s)").replace("%p",u.getRank().getColor() + p.getName() + ChatColor.YELLOW).replace("%c",u2.getRank().getColor() + p2.getName() + ChatColor.YELLOW).replace("%r",ChatColor.WHITE + finalReason + ChatColor.YELLOW).replace("%s",ChatColor.WHITE + p2.getServer().getInfo().getName().toUpperCase() + ChatColor.YELLOW)));
                                                    } else {
                                                        s.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "[REPORTS] " + ChatColor.YELLOW + ss.getTranslatedMessage("%p reported %c with reason %r. (%s)").replace("%p",u.getRank().getColor() + p.getName() + ChatColor.YELLOW).replace("%c",u2.getRank().getColor() + p2.getName() + ChatColor.YELLOW).replace("%r",ChatColor.WHITE + finalReason + ChatColor.YELLOW).replace("%s",ChatColor.WHITE + p2.getServer().getInfo().getName().toUpperCase() + ChatColor.YELLOW)));
                                                        s.sendMessage(new ComponentBuilder(ChatColor.RED + "[REPORTS] " + ChatColor.YELLOW).append(u.getTranslatedMessage("Click to view chatlog.")).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,TextComponent.fromLegacyText(ChatColor.GREEN + "https://admin.thechest.eu/chatlog/?id=" + chatLogID))).event(new ClickEvent(ClickEvent.Action.OPEN_URL,"https://admin.thechest.eu/chatlog/?id=" + chatLogID)).create());
                                                    }
                                                }

                                                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + u.getTranslatedMessage("You have sucessfully reported the player %p with reason %r!").replace("%p",ChatColor.YELLOW + p2.getName() + ChatColor.GREEN).replace("%r",ChatColor.YELLOW + finalReason + ChatColor.GREEN)));
                                            }
                                        } catch(Exception e){
                                            e.printStackTrace();
                                            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("An error occured.")));
                                        }
                                    });
                                } else {
                                    p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("That player hasn't sent any messages yet.")));
                                }
                            } else {
                                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("You can't report yourself.")));
                            }
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("You have already reported that player.")));
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("That's not a valid reason. Valid reasons are:")));
                        for(String s : validReasons){
                            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "- " + ChatColor.YELLOW + s));
                        }
                    }
                } else {
                    p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("That player is not online.")));
                }
            } else {
                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "/report <" + u.getTranslatedMessage("Player") + "> <" + u.getTranslatedMessage("Reason") + ">"));
            }
        } else {

        }
    }
}
