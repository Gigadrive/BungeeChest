package eu.thechest.bungeechest.cmd;

import eu.thechest.bungeechest.BungeeChest;
import eu.thechest.bungeechest.ban.BanUtilities;
import eu.thechest.bungeechest.user.BungeeUser;
import eu.thechest.bungeechest.user.PlayerUtilities;
import eu.thechest.bungeechest.user.Rank;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;

/**
 * Created by zeryt on 24.02.2017.
 */
public class UnbanCMD extends Command {
    public UnbanCMD() {
        super("unban", null, new String[]{"minecraft:unban", "bukkit:unban", "essentials:unban", "minecraft:pardon", "bukkit:pardon", "essentials:pardon"});
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer p = (ProxiedPlayer) sender;
            BungeeUser u = BungeeUser.getUser(p);

            if (u.hasPermission(Rank.MOD)) {
                if (args.length == 1) {
                    String playerName = args[0];

                    if(PlayerUtilities.getUUIDFromName(playerName) != null){
                        UUID uuid = PlayerUtilities.getUUIDFromName(playerName);
                        Rank rank = PlayerUtilities.getRankFromUUID(uuid);

                        if(BanUtilities.isBanned(uuid)){
                            BanUtilities.getBan(uuid).markAsUnbanned(p.getUniqueId().toString());

                            for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + u.getTranslatedMessage("%p unbanned %u").replace("%p",u.getRank().getColor() + p.getName() + ChatColor.YELLOW).replace("%u",rank.getColor() + playerName)));
                            }
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("That player is not banned.")));
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Unknown UUID.")));
                    }
                } else {
                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "/unban <Player>"));
                }
            } else {
                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("You aren't permitted to execute this command.")));
            }
        } else {
            if (args.length == 1) {
                String playerName = args[0];

                if(PlayerUtilities.getUUIDFromName(playerName) != null){
                    UUID uuid = PlayerUtilities.getUUIDFromName(playerName);
                    Rank rank = PlayerUtilities.getRankFromUUID(uuid);

                    if(BanUtilities.isBanned(uuid)){
                        BanUtilities.getBan(uuid).markAsUnbanned(null);

                        for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                            all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.DARK_PURPLE + "BungeeConsole" + ChatColor.YELLOW + " unbanned " + rank.getColor() + playerName));
                        }
                    } else {
                        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "That player is not banned."));
                    }
                } else {
                    sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "Unknown UUID."));
                }
            } else {
                sender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "/unban <Player>"));
            }
        }
    }
}
