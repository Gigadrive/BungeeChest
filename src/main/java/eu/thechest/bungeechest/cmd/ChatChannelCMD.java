package eu.thechest.bungeechest.cmd;

import eu.thechest.bungeechest.BungeeChest;
import eu.thechest.bungeechest.user.BungeeUser;
import eu.thechest.bungeechest.user.Rank;
import eu.thechest.bungeechest.util.ChatChannel;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * Created by zeryt on 20.07.2017.
 */
public class ChatChannelCMD extends Command {
    public ChatChannelCMD(){
        super("chat",null);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer)sender;
            BungeeUser u = BungeeUser.getUser(p);

            if(args.length == 0){
                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.YELLOW + u.getTranslatedMessage("Your current channel: %c").replace("%c",ChatColor.WHITE + u.getChatChannel().toString())));
                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.YELLOW + u.getTranslatedMessage("Change your chat channel with %c").replace("%c",ChatColor.WHITE + "/chat <Channel>")));
            } else {
                String n = args[0];
                String[] v = null;
                if(u.hasPermission(Rank.STAFF)){
                    v = new String[]{"ALL","PARTY","CREW","STAFF"};
                } else {
                    v = new String[]{"ALL","PARTY","CREW"};
                }

                boolean b = false;
                for(String s : v) if(s.equalsIgnoreCase(n)) b = true;

                if(b){
                    ChatChannel channel = ChatChannel.valueOf(n.toUpperCase());
                    u.setChatChannel(channel);

                    p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + u.getTranslatedMessage("You are now speaking in: %c").replace("%c",ChatColor.YELLOW + channel.toString())));
                } else {
                    p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Invalid chat channel! Valid channels are:")));
                    for(String s : v){
                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.YELLOW + "- " + ChatColor.RED + s));
                    }
                }
            }
        } else {
            sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "You have to be a player!"));
        }
    }
}
