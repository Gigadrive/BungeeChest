package eu.thechest.bungeechest.cmd;

import eu.thechest.bungeechest.BungeeChest;
import eu.thechest.bungeechest.ban.BanUtilities;
import eu.thechest.bungeechest.ban.Mute;
import eu.thechest.bungeechest.user.BungeeUser;
import eu.thechest.bungeechest.user.PlayerUtilities;
import eu.thechest.bungeechest.user.Rank;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * Created by zeryt on 08.03.2017.
 */
public class PrivateMSG extends Command {
    public PrivateMSG(){
        super("msg",null,new String[]{"say","tell","whisper","w","minecraft:tell","bukkit:tell","minecraft:w","bukkit:w","minecraft:whisper","bukkit:whisper","minecraft:say","bukkit:say"});
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer)sender;
            BungeeUser u = BungeeUser.getUser(p);

            if(BanUtilities.getMute(p.getUniqueId()) != null){
                Mute mute = BanUtilities.getMute(p.getUniqueId());

                if(!mute.isExpired()){
                    if(mute.getReason() == null){
                        if(mute.getEndDate() == null){
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("You were muted.")));
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + u.getTranslatedMessage("No reason has been specified.")));
                            return;
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("You were muted.")));
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + u.getTranslatedMessage("No reason has been specified.")));
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u.getTranslatedMessage("Expiry") + ": " + ChatColor.WHITE + mute.getEndDate().toGMTString()));
                            return;
                        }
                    } else {
                        if(mute.getEndDate() == null){
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("You were muted.")));
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + mute.getReason()));
                            return;
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("You were muted.")));
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + mute.getReason()));
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u.getTranslatedMessage("Expiry") + ": " + ChatColor.WHITE + mute.getEndDate().toGMTString()));
                            return;
                        }
                    }
                } else {
                    mute.markAsExpired();
                }
            }

            if(args.length >= 2){
                String receiver = args[0];
                StringBuilder sb = new StringBuilder("");
                for (int i = 1; i < args.length; i++) {
                    sb.append(args[i]).append(" ");
                }
                String msg = sb.toString();

                if(BungeeChest.getInstance().getProxy().getPlayer(receiver) != null){
                    ProxiedPlayer p2 = BungeeChest.getInstance().getProxy().getPlayer(receiver);
                    BungeeUser u2 = BungeeUser.getUser(p2);

                    if(PlayerUtilities.allowsPrivateMessages(p2.getUniqueId()) || u.hasPermission(Rank.MOD) || u.getFriends().contains(p2.getUniqueId().toString())){
                        u.lastMsg = p2;
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[" + u.getRank().getColor() + p.getName() + ChatColor.GOLD + "->" + u2.getRank().getColor() + p2.getName() + ChatColor.GOLD + "]" + ChatColor.WHITE + " " + msg));
                        p2.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[" + u.getRank().getColor() + p.getName() + ChatColor.GOLD + "->" + u2.getRank().getColor() + p2.getName() + ChatColor.GOLD + "]" + ChatColor.WHITE + " " + msg));
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("That player does not allow private messages.")));
                    }
                } else {
                    p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("That player is not online.")));
                }
            } else {
                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "/msg <" + u.getTranslatedMessage("Player") + "> <" + u.getTranslatedMessage("Message") + ">"));
            }
        } else {
            if(args.length >= 2){
                String receiver = args[0];
                StringBuilder sb = new StringBuilder("");
                for (int i = 1; i < args.length; i++) {
                    sb.append(args[i]).append(" ");
                }
                String msg = sb.toString();

                if(BungeeChest.getInstance().getProxy().getPlayer(receiver) != null){
                    ProxiedPlayer p2 = BungeeChest.getInstance().getProxy().getPlayer(receiver);
                    BungeeUser u2 = BungeeUser.getUser(p2);

                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "BungeeConsole" + ChatColor.GOLD + "->" + u2.getRank().getColor() + p2.getName() + ChatColor.GOLD + "]" + ChatColor.WHITE + " " + msg));
                    p2.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "BungeeConsole" + ChatColor.GOLD + "->" + u2.getRank().getColor() + p2.getName() + ChatColor.GOLD + "]" + ChatColor.WHITE + " " + msg));
                } else {
                    sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "That player is not online."));
                }
            } else {
                sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "/msg <Player> <Message>"));
            }
        }
    }
}
