package eu.thechest.bungeechest.cmd;

import eu.thechest.bungeechest.BungeeChest;
import eu.thechest.bungeechest.user.BungeeUser;
import eu.thechest.bungeechest.user.Rank;
import eu.thechest.bungeechest.util.TaskUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Command;

/**
 * Created by zeryt on 09.07.2017.
 */
public class ReloadCachesCMD extends Command {
    public ReloadCachesCMD(){
        super("reloadcaches");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer)sender;
            BungeeUser u = BungeeUser.getUser(p);

            if(u.hasPermission(Rank.ADMIN)){
                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + u.getTranslatedMessage("Reloading caches..")));
                TaskUtils.reloadCaches(false);
            } else {
                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("You aren't permitted to execute this command.")));
            }
        } else {
            TaskUtils.reloadCaches(false);
            System.out.println("Completed Task ReloadCaches");
        }
    }
}
