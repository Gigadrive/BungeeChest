package eu.thechest.bungeechest.cmd;

import eu.thechest.bungeechest.BungeeChest;
import eu.thechest.bungeechest.user.BungeeUser;
import eu.thechest.bungeechest.user.Rank;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class SendCMD extends Command {
    public SendCMD(){
        super("send");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer)sender;
            BungeeUser u = BungeeUser.getUser(p);

            if(u.hasPermission(Rank.ADMIN)){
                if(args.length == 2){
                    String send = args[0];
                    String target = args[1];
                    net.md_5.bungee.api.config.ServerInfo info = ProxyServer.getInstance().getServerInfo(target);

                    if(info != null){
                        if(send.equalsIgnoreCase("current")){
                            for(ProxiedPlayer a : p.getServer().getInfo().getPlayers()){
                                a.connect(info);
                            }

                            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + u.getTranslatedMessage("Sending players to %server%.").replace("%server%",ChatColor.YELLOW + info.getName() + ChatColor.GREEN)));
                        } else if(send.equalsIgnoreCase("all")){
                            for(ProxiedPlayer a : ProxyServer.getInstance().getPlayers()){
                                a.connect(info);
                            }

                            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + u.getTranslatedMessage("Sending players to %server%.").replace("%server%",ChatColor.YELLOW + info.getName() + ChatColor.GREEN)));
                        } else if(ProxyServer.getInstance().getServerInfo(send) != null){
                            for(ProxiedPlayer a : ProxyServer.getInstance().getServerInfo(send).getPlayers()){
                                a.connect(info);
                            }

                            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + u.getTranslatedMessage("Sending players to %server%.").replace("%server%",ChatColor.YELLOW + info.getName() + ChatColor.GREEN)));
                        } else {
                            ProxiedPlayer p2 = ProxyServer.getInstance().getPlayer(send);

                            if(p2 != null){
                                p2.connect(info);
                                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + u.getTranslatedMessage("Sending players to %server%.").replace("%server%",ChatColor.YELLOW + info.getName() + ChatColor.GREEN)));
                            } else {
                                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("That player is not online.")));
                            }
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("That server could not be found.")));
                    }
                } else {
                    p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "/server <current|all|" + u.getTranslatedMessage("Player") + "|" + u.getTranslatedMessage("Server") + "> <" + u.getTranslatedMessage("Server") + ">"));
                }
            } else {
                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("You aren't permitted to execute this command.")));
            }
        } else {

        }
    }
}
