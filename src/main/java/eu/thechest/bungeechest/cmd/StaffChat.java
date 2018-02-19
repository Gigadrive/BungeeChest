package eu.thechest.bungeechest.cmd;

import eu.thechest.bungeechest.BungeeChest;
import eu.thechest.bungeechest.user.BungeeUser;
import eu.thechest.bungeechest.user.PlayerUtilities;
import eu.thechest.bungeechest.user.Rank;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * Created by zeryt on 04.03.2017.
 */
public class StaffChat extends Command {
    public StaffChat(){
        super("staffchat",null,new String[]{"a","ac","sc","tc","t","teamchat","adminchat"});
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer)sender;
            BungeeUser u = BungeeUser.getUser(p);

            if(u.hasPermission(Rank.STAFF)){
                if(args.length == 0){
                    p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "/a <" + u.getTranslatedMessage("Message") + ">"));
                } else {
                    StringBuilder sb = new StringBuilder("");
                    for (int i = 0; i < args.length; i++) {
                        sb.append(args[i]).append(" ");
                    }
                    String s = sb.toString().trim();

                    for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff()){
                        BungeeUser a = BungeeUser.getUser(all);

                        all.sendMessage(TextComponent.fromLegacyText(ChatColor.AQUA + "[STAFFCHAT] " + u.getRank().getColor() + p.getName() + " " + ChatColor.YELLOW + "(" + p.getServer().getInfo().getName() + ")" + ChatColor.GRAY + ": " + s));
                    }

                    BungeeChest.getInstance().getProxy().getConsole().sendMessage(TextComponent.fromLegacyText(ChatColor.AQUA + "[STAFFCHAT] " + u.getRank().getColor() + p.getName() + " " + ChatColor.YELLOW + "(" + p.getServer().getInfo().getName() + ")" + ChatColor.GRAY + ": " + s));
                }
            } else {
                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("You aren't permitted to execute this command.")));
            }
        } else {
            if(args.length == 0){
                sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "/a <Message>"));
            } else {
                StringBuilder sb = new StringBuilder("");
                for (int i = 0; i < args.length; i++) {
                    sb.append(args[i]).append(" ");
                }
                String s = sb.toString();

                for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff()){
                    BungeeUser a = BungeeUser.getUser(all);

                    all.sendMessage(TextComponent.fromLegacyText(ChatColor.AQUA + "[STAFFCHAT] " + ChatColor.DARK_PURPLE + "BungeeConsole" + ChatColor.GRAY + ": " + s));
                }

                BungeeChest.getInstance().getProxy().getConsole().sendMessage(TextComponent.fromLegacyText(ChatColor.AQUA + "[STAFFCHAT] " + ChatColor.DARK_PURPLE + "BungeeConsole" + ChatColor.GRAY + ": " + s));
            }
        }
    }
}
