package eu.thechest.bungeechest.cmd;

import eu.thechest.bungeechest.BungeeChest;
import eu.thechest.bungeechest.user.BungeeUser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * Created by zeryt on 24.02.2017.
 */
public class HubCMD extends Command {
    public HubCMD(){
        super("hub",null,new String[]{"lobby","leave","quit","q"});
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer)sender;
            BungeeUser u = BungeeUser.getUser(p);
            ServerInfo lobby = BungeeChest.getInstance().getBestLobby(p);

            if(lobby != null){
                p.connect(lobby);
            } else {
                p.disconnect(TextComponent.fromLegacyText(ChatColor.RED + u.getTranslatedMessage("Could not find a suitable lobby server.")));
            }
        }
    }
}
