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
public class UnmuteCMD extends Command {
    public UnmuteCMD() {
        super("unmute", null, new String[]{"minecraft:unmute", "bukkit:unmute", "essentials:unmute"});
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

                        if(BanUtilities.isMuted(uuid)){
                            BanUtilities.getMute(uuid).markAsUnmuted(p.getUniqueId().toString());

                            for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                BungeeUser a = BungeeUser.getUser(all);

                                all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p unmuted %u.").replace("%p",u.getRank().getColor() + p.getName() + ChatColor.YELLOW).replace("%u",rank.getColor() + playerName + ChatColor.YELLOW)));
                            }
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("That player isn't muted.")));
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Unknown UUID.")));
                    }
                } else {
                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "/unmute <" + u.getTranslatedMessage("Player") + ">"));
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

                    if(BanUtilities.isMuted(uuid)){
                        BanUtilities.getMute(uuid).markAsUnmuted(null);

                        for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                            BungeeUser a = BungeeUser.getUser(all);

                            all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p unmuted %u.").replace("%p",ChatColor.DARK_PURPLE + "BungeeConsole" + ChatColor.YELLOW).replace("%u",rank.getColor() + playerName + ChatColor.YELLOW)));
                        }
                    } else {
                        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "That player is not muted."));
                    }
                } else {
                    sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "Unknown UUID."));
                }
            } else {
                sender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "/unmute <Player>"));
            }
        }
    }
}
