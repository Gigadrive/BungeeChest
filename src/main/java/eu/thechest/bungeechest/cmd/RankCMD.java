package eu.thechest.bungeechest.cmd;

import eu.thechest.bungeechest.BungeeChest;
import eu.thechest.bungeechest.user.BungeeUser;
import eu.thechest.bungeechest.user.PlayerUtilities;
import eu.thechest.bungeechest.user.Rank;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import javax.xml.soap.Text;
import java.util.UUID;

/**
 * Created by zeryt on 24.02.2017.
 */
public class RankCMD extends Command {
    public RankCMD(){
        super("rank");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer)sender;
            BungeeUser u = BungeeUser.getUser(p);
            BaseComponent[] usage = TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "/rank <" + u.getTranslatedMessage("Player") + "|list> [" + u.getTranslatedMessage("Rank") + "]");

            if(u.hasPermission(Rank.ADMIN)){
                if(args.length == 0){
                    p.sendMessage(usage);
                } else if(args.length == 1){
                    if(args[0].equalsIgnoreCase("list")){
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + BungeeChest.LINE_SEPERATOR));
                        u.sendCenteredMessage(ChatColor.YELLOW + u.getTranslatedMessage("List of TheChest ranks"));
                        for(Rank r : Rank.values()){
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GRAY + "[" + r.getColor() + r.getID() + ChatColor.DARK_GRAY + "] " + r.getColor() + r.getName()));
                        }
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + BungeeChest.LINE_SEPERATOR));
                    } else {
                        p.sendMessage(usage);
                    }
                } else if(args.length == 2){
                    if(BungeeChest.getInstance().isValidInteger(args[1])){
                        String player = args[0];
                        int rankID = Integer.parseInt(args[1]);

                        UUID uuid = PlayerUtilities.getUUIDFromName(player);

                        if(uuid != null){
                            Rank rank = Rank.fromID(rankID);

                            if(rank != null){
                                if(PlayerUtilities.setRank(uuid,rank)){
                                    PlayerUtilities.logRankUpdate(p.getUniqueId(),uuid,rank);
                                    p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + u.getTranslatedMessage("The rank has been updated!")));
                                } else {
                                    p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("An error occured.")));
                                }
                            } else {
                                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Invalid Rank ID!")));
                            }
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Unknown UUID.")));
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Please use the rank ID!")));
                    }
                } else {
                    p.sendMessage(usage);
                }
            } else {
                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("You aren't permitted to execute this command.")));
            }
        } else {
            BaseComponent[] usage = TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "/rank <Player|list> [Rank]");

            if(args.length == 0){
                sender.sendMessage(usage);
            } else if(args.length == 1){
                if(args[0].equalsIgnoreCase("list")){
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + BungeeChest.LINE_SEPERATOR));
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.YELLOW + "List of TheChest ranks"));
                    for(Rank r : Rank.values()){
                        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GRAY + "[" + r.getColor() + r.getID() + ChatColor.DARK_GRAY + "] " + r.getColor() + r.getName()));
                    }
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + BungeeChest.LINE_SEPERATOR));
                } else {
                    sender.sendMessage(usage);
                }
            } else if(args.length == 2){
                if(BungeeChest.getInstance().isValidInteger(args[1])){
                    String player = args[0];
                    int rankID = Integer.parseInt(args[1]);

                    UUID uuid = PlayerUtilities.getUUIDFromName(player);

                    if(uuid != null){
                        Rank rank = Rank.fromID(rankID);

                        if(rank != null){
                            if(PlayerUtilities.setRank(uuid,rank)){
                                PlayerUtilities.logRankUpdate(null,uuid,rank);
                                sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + "The rank has been updated!"));
                            } else {
                                sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "An error occured."));
                            }
                        } else {
                            sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "Invalid Rank ID!"));
                        }
                    } else {
                        sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "Unknown UUID."));
                    }
                } else {
                    sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "Please use the rank ID!"));
                }
            } else {
                sender.sendMessage(usage);
            }
        }
    }
}
