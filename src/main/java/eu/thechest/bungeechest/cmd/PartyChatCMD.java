package eu.thechest.bungeechest.cmd;

import eu.thechest.bungeechest.BungeeChest;
import eu.thechest.bungeechest.ban.BanUtilities;
import eu.thechest.bungeechest.ban.Mute;
import eu.thechest.bungeechest.party.Party;
import eu.thechest.bungeechest.user.BungeeUser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * Created by zeryt on 09.07.2017.
 */
public class PartyChatCMD extends Command {
    public PartyChatCMD(){
        super("p",null,new String[]{"pchat","partychat"});
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer p = (ProxiedPlayer) sender;
            BungeeUser u = BungeeUser.getUser(p);

            if(BanUtilities.getMute(p.getUniqueId()) != null){
                Mute mute = BanUtilities.getMute(p.getUniqueId());

                if(!mute.isExpired()){
                    if(mute.getReason() == null){
                        if(mute.getEndDate() == null){
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("You were muted.")));
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + u.getTranslatedMessage("No reason has been specified.")));
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("You were muted.")));
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + u.getTranslatedMessage("No reason has been specified.")));
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u.getTranslatedMessage("Expiry") + ": " + ChatColor.WHITE + mute.getEndDate().toGMTString()));
                        }
                    } else {
                        if(mute.getEndDate() == null){
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("You were muted.")));
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + mute.getReason()));
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("You were muted.")));
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + mute.getReason()));
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u.getTranslatedMessage("Expiry") + ": " + ChatColor.WHITE + mute.getEndDate().toGMTString()));
                        }
                    }

                    return;
                } else {
                    mute.markAsExpired();
                }
            }

            if(Party.hasParty(p)){
                if(args.length == 0){
                    sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "/c <" + u.getTranslatedMessage("Message") + ">"));
                } else {
                    StringBuilder sb = new StringBuilder("");
                    for (int i = 0; i < args.length; i++) {
                        sb.append(args[i]).append(" ");
                    }
                    String s = sb.toString().trim();

                    for(ProxiedPlayer all : Party.getParty(p).getMembers()){
                        all.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + u.getRank().getColor() + p.getName() + ChatColor.GRAY + ": " + ChatColor.WHITE + s));
                    }

                    u.achieve(31);
                }
            } else {
                p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.RED + u.getTranslatedMessage("You are not in a party.")));
            }
        } else {
            sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "You must be a player to do this!"));
        }
    }
}
