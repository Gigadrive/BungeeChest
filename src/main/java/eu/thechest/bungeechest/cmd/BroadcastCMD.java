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
 * Created by zeryt on 11.03.2017.
 */
public class BroadcastCMD extends Command {
    public BroadcastCMD(){
        super("broadcast",null,new String[]{"shout","alert","bc","sys","alram","alarm","albert","ramall"});
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer)sender;
            BungeeUser u = BungeeUser.getUser(p);

            if(u.hasPermission(Rank.CM)){
                if(args.length == 0){
                    p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "/broadcast <" + u.getTranslatedMessage("Message") + ">"));
                } else {
                    StringBuilder sb = new StringBuilder("");
                    for (int i = 0; i < args.length; i++) {
                        sb.append(args[i]).append(" ");
                    }
                    String s = sb.toString();

                    System.out.println("[BROADCAST by " + p.getName() + "] " + s);
                    for(ProxiedPlayer a : BungeeChest.getInstance().getProxy().getPlayers()){
                        if(BungeeUser.getUser(a).hasPermission(Rank.CM)){
                            a.sendMessage(TextComponent.fromLegacyText(ChatColor.YELLOW + "[BROADCAST] " + u.getRank().getColor() + p.getName() + ChatColor.GRAY + ": " + ChatColor.GREEN + s));
                        } else {
                            a.sendMessage(TextComponent.fromLegacyText(ChatColor.YELLOW + "[BROADCAST] " + ChatColor.GREEN + s));
                        }
                    }
                }
            } else {
                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("You aren't permitted to execute this command.")));
            }
        } else {
            if(args.length == 0){
                sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "/broadcast <Message>"));
            } else {
                StringBuilder sb = new StringBuilder("");
                for (int i = 0; i < args.length; i++) {
                    sb.append(args[i]).append(" ");
                }
                String s = sb.toString();

                System.out.println("[BROADCAST by " + "BungeeConsole" + "] " + s);
                for(ProxiedPlayer a : BungeeChest.getInstance().getProxy().getPlayers()){
                    if(BungeeUser.getUser(a).hasPermission(Rank.CM)){
                        a.sendMessage(TextComponent.fromLegacyText(ChatColor.YELLOW + "[BROADCAST] " + ChatColor.DARK_PURPLE + "BungeeConsole" + ChatColor.GRAY + ": " + ChatColor.GREEN + s));
                    } else {
                        a.sendMessage(TextComponent.fromLegacyText(ChatColor.YELLOW + "[BROADCAST] " + ChatColor.GREEN + s));
                    }
                }
            }
        }
    }
}
