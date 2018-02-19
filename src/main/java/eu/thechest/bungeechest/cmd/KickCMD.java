package eu.thechest.bungeechest.cmd;

import eu.thechest.bungeechest.BungeeChest;
import eu.thechest.bungeechest.mysql.MySQLManager;
import eu.thechest.bungeechest.user.BungeeUser;
import eu.thechest.bungeechest.user.PlayerUtilities;
import eu.thechest.bungeechest.user.Rank;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.PreparedStatement;

/**
 * Created by zeryt on 24.02.2017.
 */
@Deprecated
public class KickCMD extends Command {
    public KickCMD(){
        super("kick", null, new String[]{"minecraft:kick","bukkit:kick","essentials:kick"});
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer p = (ProxiedPlayer) sender;
            BungeeUser u = BungeeUser.getUser(p);

            if (u.hasPermission(Rank.MOD)) {
                if(args.length >= 1){
                    String playerName = args[0];

                    if(BungeeChest.getInstance().getProxy().getPlayer(playerName) != null){
                        ProxiedPlayer p2 = BungeeChest.getInstance().getProxy().getPlayer(playerName);
                        BungeeUser u2 = BungeeUser.getUser(p2);

                        if(u.getRank().getID() >= u2.getRank().getID()){
                            String reason;

                            if(args.length == 1){
                                reason = null;
                            } else {
                                StringBuilder sb = new StringBuilder("");
                                for (int i = 1; i < args.length; i++) {
                                    sb.append(args[i]).append(" ");
                                }
                                reason = sb.toString().trim();
                            }

                            try {
                                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `sn_kicks` (`kickedPlayer`,`nameWhenKicked`,`reason`,`server`,`kickedBy`) VALUES(?,?,?,?,?)");
                                ps.setString(1,p2.getUniqueId().toString());
                                ps.setString(2,p2.getName());
                                ps.setString(3,reason);
                                ps.setString(4,p2.getServer().getInfo().getName());
                                ps.setString(5,p.getUniqueId().toString());
                                ps.execute();
                                ps.close();

                                if(reason == null){
                                    p2.disconnect(
                                            TextComponent.fromLegacyText(
                                                    ChatColor.GOLD.toString() + ChatColor.BOLD + "TheChest.eu" + "\n" +
                                                            ChatColor.RED + u2.getTranslatedMessage("You were kicked.") + "\n" +
                                                            "\n" +
                                                            ChatColor.YELLOW + u2.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + u2.getTranslatedMessage("No reason has been specified.")
                                            )
                                    );

                                    for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                        BungeeUser a = BungeeUser.getUser(all);
                                        all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p kicked %u").replace("%p",u.getRank().getColor() + p.getName() + ChatColor.YELLOW).replace("%u",u2.getRank().getColor() + p2.getName() + ChatColor.YELLOW)));
                                    }
                                } else {
                                    p2.disconnect(
                                            TextComponent.fromLegacyText(
                                                    ChatColor.GOLD.toString() + ChatColor.BOLD + "TheChest.eu" + "\n" +
                                                            ChatColor.RED + u2.getTranslatedMessage("You were kicked.") + "\n" +
                                                            "\n" +
                                                            ChatColor.YELLOW + u2.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + reason
                                            )
                                    );

                                    for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                        BungeeUser a = BungeeUser.getUser(all);
                                        all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p kicked %u with reason: %r.").replace("%p",u.getRank().getColor() + p.getName() + ChatColor.YELLOW).replace("%u",u2.getRank().getColor() + p2.getName() + ChatColor.YELLOW).replace("%r",ChatColor.WHITE + reason + ChatColor.YELLOW)));
                                    }
                                }
                            } catch (Exception e){
                                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("An error occured.")));
                                e.printStackTrace();
                                return;
                            }
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("You can't kick that player.")));
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("That player is not online.")));
                    }
                } else {
                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "/kick <Player> [Reason]"));
                }
            } else {
                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("You aren't permitted to execute this command.")));
            }
        } else {
            if(args.length >= 1){
                String playerName = args[0];

                if(BungeeChest.getInstance().getProxy().getPlayer(playerName) != null){
                    ProxiedPlayer p2 = BungeeChest.getInstance().getProxy().getPlayer(playerName);
                    BungeeUser u2 = BungeeUser.getUser(p2);

                    String reason;

                    if(args.length == 1){
                        reason = null;
                    } else {
                        StringBuilder sb = new StringBuilder("");
                        for (int i = 1; i < args.length; i++) {
                            sb.append(args[i]).append(" ");
                        }
                        reason = sb.toString();
                    }

                    try {
                        PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `kicks` (`uuid`,`nameWhenKicked`,`server`) VALUES(?,?,?)");
                        ps.setString(1,p2.getUniqueId().toString());
                        ps.setString(2,p2.getName());
                        ps.setString(3,p2.getServer().getInfo().getName());
                        ps.execute();
                        ps.close();

                        if(reason == null){
                            p2.disconnect(
                                    TextComponent.fromLegacyText(
                                            ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "Heavy-War.net" + "\n" +
                                                    ChatColor.RED + u2.getTranslatedMessage("bans.youWereKicked") + "\n" +
                                                    "\n" +
                                                    ChatColor.YELLOW + u2.getTranslatedMessage("bans.reason") + ": " + ChatColor.WHITE + u2.getTranslatedMessage("bans.reason.notSpecified")
                                    )
                            );

                            for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                BungeeUser a = BungeeUser.getUser(all);
                                all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p kicked %u").replace("%p",ChatColor.DARK_PURPLE + "BungeeConsole" + ChatColor.YELLOW).replace("%u",u2.getRank().getColor() + p2.getName() + ChatColor.YELLOW)));
                            }
                        } else {
                            p2.disconnect(
                                    TextComponent.fromLegacyText(
                                            ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "Heavy-War.net" + "\n" +
                                                    ChatColor.RED + u2.getTranslatedMessage("bans.youWereKicked") + "\n" +
                                                    "\n" +
                                                    ChatColor.YELLOW + u2.getTranslatedMessage("bans.reason") + ": " + ChatColor.WHITE + reason
                                    )
                            );

                            for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                BungeeUser a = BungeeUser.getUser(all);
                                all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p kicked %u with reason: %r.").replace("%p",ChatColor.DARK_PURPLE + "BungeeConsole" + ChatColor.YELLOW).replace("%u",u2.getRank().getColor() + p2.getName() + ChatColor.YELLOW).replace("%r",ChatColor.WHITE + reason + ChatColor.YELLOW)));
                            }
                        }
                    } catch (Exception e){
                        sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "An error occured."));
                        e.printStackTrace();
                        return;
                    }
                } else {
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "That player is not online."));
                }
            } else {
                sender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "/kick <Player> [Reason]"));
            }
        }
    }
}
