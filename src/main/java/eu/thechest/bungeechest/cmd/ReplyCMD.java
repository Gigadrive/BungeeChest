package eu.thechest.bungeechest.cmd;

import eu.thechest.bungeechest.BungeeChest;
import eu.thechest.bungeechest.user.BungeeUser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * Created by zeryt on 08.03.2017.
 */
public class ReplyCMD extends Command {
    public ReplyCMD(){
        super("reply",null,new String[]{"r"});
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer)sender;
            BungeeUser u = BungeeUser.getUser(p);

            if(u.lastMsg != null && BungeeChest.getInstance().getProxy().getPlayer(u.lastMsg.getName()) != null){
                StringBuilder sb = new StringBuilder("");
                for (int i = 0; i < args.length; i++) {
                    sb.append(args[i]).append(" ");
                }
                String msg = sb.toString();
                BungeeChest.getInstance().getProxy().getPluginManager().dispatchCommand(p,"msg " + u.lastMsg.getName() + " " + msg);
            } else {
                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("You have to message someone before using this command.")));
            }
        } else {
            sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "You have to be a player!"));
        }
    }
}
