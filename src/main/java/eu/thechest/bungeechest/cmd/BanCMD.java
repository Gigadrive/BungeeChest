package eu.thechest.bungeechest.cmd;

import eu.thechest.bungeechest.BungeeChest;
import eu.thechest.bungeechest.ban.BanUtilities;
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
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Created by zeryt on 24.02.2017.
 */
@Deprecated
public class BanCMD extends Command {
    public BanCMD(){
        super("ban", null, new String[]{"minecraft:ban","bukkit:ban","essentials:ban"});
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer p = (ProxiedPlayer) sender;
            BungeeUser u = BungeeUser.getUser(p);

            if (u.hasPermission(Rank.MOD)) {
                if(args.length >= 1){
                    String playerName = args[0];
                    UUID uuid = PlayerUtilities.getUUIDFromName(playerName);

                    if(uuid != null){
                        playerName = PlayerUtilities.getNameFromUUID(uuid);
                        Rank rank = PlayerUtilities.getRankFromUUID(uuid);

                        if(u.getRank().getID() >= rank.getID()){
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

                            String server = null;
                            if(BungeeChest.getInstance().getProxy().getPlayer(playerName) != null) server = BungeeChest.getInstance().getProxy().getPlayer(playerName).getServer().getInfo().getName();

                            if(!BanUtilities.isBanned(uuid)){
                                try {
                                    PreparedStatement second = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `sn_bans` (`bannedPlayer`, `bannedBy`, `reason`, `nameWhenBanned`, `server`) VALUES(?,?,?,?,?)");
                                    second.setString(1, uuid.toString());
                                    second.setString(2, p.getUniqueId().toString());
                                    second.setString(3, reason);
                                    second.setString(4, playerName);
                                    second.setString(5, server);
                                    second.execute();
                                    second.close();
                                } catch(Exception e){
                                    e.printStackTrace();
                                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("An error occured.")));
                                    return;
                                }

                                if(BungeeChest.getInstance().getProxy().getPlayer(playerName) != null){
                                    ProxiedPlayer p2 = BungeeChest.getInstance().getProxy().getPlayer(playerName);
                                    BungeeUser u2 = BungeeUser.getUser(p2);

                                    if(reason == null){
                                        p2.disconnect(TextComponent.fromLegacyText(
                                                ChatColor.GOLD.toString() + ChatColor.BOLD + "TheChest.eu" + "\n" +
                                                        ChatColor.RED + u2.getTranslatedMessage("You were banned from this server.") + "\n" +
                                                        "\n" +
                                                        ChatColor.YELLOW + u2.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + u2.getTranslatedMessage("No reason has been specified.") + "\n\n" +
                                                        ChatColor.GRAY + "Appeal at " + ChatColor.AQUA.toString() + ChatColor.UNDERLINE.toString() + "https://go.thechest.eu/appeal"
                                        ));
                                    } else {
                                        p2.disconnect(TextComponent.fromLegacyText(
                                                ChatColor.GOLD.toString() + ChatColor.BOLD + "TheChest.eu" + "\n" +
                                                        ChatColor.RED + u2.getTranslatedMessage("You were banned from this server.") + "\n" +
                                                        "\n" +
                                                        ChatColor.YELLOW + u2.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + reason + "\n\n" +
                                                        ChatColor.GRAY + "Appeal at " + ChatColor.AQUA.toString() + ChatColor.UNDERLINE.toString() + "https://go.thechest.eu/appeal"
                                        ));
                                    }
                                }

                                if(reason == null){
                                    for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                        BungeeUser a = BungeeUser.getUser(all);
                                        all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p banned %u").replace("%p",u.getRank().getColor() + p.getName() + ChatColor.YELLOW).replace("%u",rank.getColor() + playerName + ChatColor.YELLOW)));
                                    }
                                } else {
                                    for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                        BungeeUser a = BungeeUser.getUser(all);
                                        all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p banned %u with reason %r.").replace("%p",u.getRank().getColor() + p.getName() + ChatColor.YELLOW).replace("%u",rank.getColor() + playerName + ChatColor.YELLOW).replace("%r",ChatColor.WHITE + reason + ChatColor.YELLOW)));
                                    }
                                }
                            } else {
                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("That player is already banned.")));
                            }
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("You can't ban that player.")));
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Unknown UUID.")));
                    }
                } else {
                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "/ban <" + u.getTranslatedMessage("Player") + "> [" + u.getTranslatedMessage("Reason") + "]"));
                }
            } else {
                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("You aren't permitted to execute this command.")));
            }
        } else {
            if(args.length >= 1){
                String playerName = args[0];
                UUID uuid = PlayerUtilities.getUUIDFromName(playerName);

                if(uuid != null){
                    playerName = PlayerUtilities.getNameFromUUID(uuid);
                    Rank rank = PlayerUtilities.getRankFromUUID(uuid);

                    if(true){
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

                        String server = null;
                        if(BungeeChest.getInstance().getProxy().getPlayer(playerName) != null) server = BungeeChest.getInstance().getProxy().getPlayer(playerName).getServer().getInfo().getName();

                        if(!BanUtilities.isBanned(uuid)){
                            try {
                                PreparedStatement second = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `sn_bans` (`bannedPlayer`, `reason`, `nameWhenBanned`, `server`) VALUES(?,?,?,?)");
                                second.setString(1, uuid.toString());
                                second.setString(2, reason);
                                second.setString(3, playerName);
                                second.setString(4, server);
                                second.execute();
                                second.close();
                            } catch(Exception e){
                                e.printStackTrace();
                                sender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "An error occured."));
                                return;
                            }

                            if(BungeeChest.getInstance().getProxy().getPlayer(playerName) != null){
                                ProxiedPlayer p2 = BungeeChest.getInstance().getProxy().getPlayer(playerName);
                                BungeeUser u2 = BungeeUser.getUser(p2);

                                if(reason == null){
                                    p2.disconnect(TextComponent.fromLegacyText(
                                            ChatColor.GOLD.toString() + ChatColor.BOLD + "TheChest.eu" + "\n" +
                                                    ChatColor.RED + u2.getTranslatedMessage("You were banned from this server.") + "\n" +
                                                    "\n" +
                                                    ChatColor.YELLOW + u2.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + u2.getTranslatedMessage("No reason has been specified.")
                                    ));
                                } else {
                                    p2.disconnect(TextComponent.fromLegacyText(
                                            ChatColor.GOLD.toString() + ChatColor.BOLD + "TheChest.eu" + "\n" +
                                                    ChatColor.RED + u2.getTranslatedMessage("You were banned from this server.") + "\n" +
                                                    "\n" +
                                                    ChatColor.YELLOW + u2.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + reason
                                    ));
                                }
                            }

                            if(reason == null){
                                for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                    BungeeUser a = BungeeUser.getUser(all);
                                    all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p banned %u").replace("%p",ChatColor.DARK_PURPLE + "BungeeConsole" + ChatColor.YELLOW).replace("%u",rank.getColor() + playerName + ChatColor.YELLOW)));
                                }
                            } else {
                                for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                    BungeeUser a = BungeeUser.getUser(all);
                                    all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p banned %u with reason %r.").replace("%p",ChatColor.DARK_PURPLE + "BungeeConsole" + ChatColor.YELLOW).replace("%u",rank.getColor() + playerName + ChatColor.YELLOW).replace("%r",ChatColor.WHITE + reason + ChatColor.YELLOW)));
                                }
                            }
                        } else {
                            sender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "That player is already banned."));
                        }
                    } else {
                        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "That player cannot be banned"));
                    }
                } else {
                    sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "Unknown UUID."));
                }
            } else {
                sender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "/ban <Player> [Reason]"));
            }
        }
    }
}
