package eu.thechest.bungeechest.cmd;

import eu.thechest.bungeechest.BungeeChest;
import eu.thechest.bungeechest.user.BungeeUser;
import eu.thechest.bungeechest.user.Rank;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * Created by zeryt on 24.02.2017.
 */
public class Ping extends Command {
    public Ping(){
        super("ping",null,new String[]{"connection","versatel"});
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer)sender;
            BungeeUser u = BungeeUser.getUser(p);

            if(args.length == 0){
                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + u.getTranslatedMessage("Your ping is %p").replace("%p",ChatColor.YELLOW.toString() + p.getPing() + "ms" + ChatColor.GREEN)));
            } else if(args.length == 1) {
                if(u.hasPermission(Rank.STAFF)){
                    String player = args[0];

                    if(BungeeChest.getInstance().getProxy().getPlayer(player) != null){
                        ProxiedPlayer p2 = BungeeChest.getInstance().getProxy().getPlayer(player);
                        BungeeUser u2 = BungeeUser.getUser(p2);

                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + u.getTranslatedMessage("%p's ping is %c").replace("%c",ChatColor.YELLOW.toString() + p2.getPing() + "ms" + ChatColor.GREEN).replace("%p",u2.getRank().getColor() + p2.getName() + ChatColor.GREEN)));
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("That player is not online.")));
                    }
                } else {
                    BungeeChest.getInstance().getProxy().getPluginManager().dispatchCommand(p,"ping");
                }
            } else {
                BungeeChest.getInstance().getProxy().getPluginManager().dispatchCommand(p,"ping");
            }
        } else {
            if(args.length == 1){
                String player = args[0];

                if(BungeeChest.getInstance().getProxy().getPlayer(player) != null){
                    ProxiedPlayer p2 = BungeeChest.getInstance().getProxy().getPlayer(player);
                    BungeeUser u2 = BungeeUser.getUser(p2);

                    sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + "%p's ping is %c".replace("%c",ChatColor.YELLOW.toString() + p2.getPing() + "ms" + ChatColor.GREEN).replace("%p",u2.getRank().getColor() + p2.getName() + ChatColor.GREEN)));
                } else {
                    sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "That player is not online."));
                }
            } else {
                sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "/ping <Player>"));
            }
        }
    }
}
