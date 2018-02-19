package eu.thechest.bungeechest.cmd;

import eu.thechest.bungeechest.BungeeChest;
import eu.thechest.bungeechest.mysql.MySQLManager;
import eu.thechest.bungeechest.user.BungeeUser;
import eu.thechest.bungeechest.user.PlayerUtilities;
import eu.thechest.bungeechest.user.Rank;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.PreparedStatement;
import java.util.UUID;

/**
 * Created by zeryt on 26.02.2017.
 */
public class GiveCoinsCMD extends Command {
    public GiveCoinsCMD(){
        super("givecoins");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer)sender;
            BungeeUser u = BungeeUser.getUser(p);

            if(u.hasPermission(Rank.ADMIN)){
                if(args.length == 2){
                    String name = args[0];

                    if(BungeeChest.getInstance().isValidInteger(args[1])){
                        int amount = Integer.parseInt(args[1]);
                        UUID uuid = PlayerUtilities.getUUIDFromName(name);

                        if(uuid != null){
                            try {
                                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `users` SET `coins`=`coins`+? WHERE `uuid`=?");
                                ps.setInt(1,amount);
                                ps.setString(2,uuid.toString());
                                int updateAmount = ps.executeUpdate();
                                ps.close();

                                if(updateAmount > 0){
                                    p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + u.getTranslatedMessage("The coins have been credited.")));
                                } else {
                                    p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.YELLOW + u.getTranslatedMessage("The coins may not have been credit as that user was not found in the database.")));
                                }
                            } catch(Exception e){
                                e.printStackTrace();
                            }
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Unknown UUID.")));
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Please enter a valid number.")));
                    }
                } else {
                    p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "/givecoins <" + u.getTranslatedMessage("Player") + "> <" + u.getTranslatedMessage("Amount") + ">"));
                }
            } else {
                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("You aren't permitted to execute this command.")));
            }
        } else {
            if(args.length == 2){
                String name = args[0];

                if(BungeeChest.getInstance().isValidInteger(args[1])){
                    int amount = Integer.parseInt(args[1]);
                    UUID uuid = PlayerUtilities.getUUIDFromName(name);

                    if(uuid != null){
                        try {
                            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `users` SET `coins`=`coins`+? WHERE `uuid`=?");
                            ps.setInt(1,amount);
                            ps.setString(2,uuid.toString());
                            int updateAmount = ps.executeUpdate();
                            ps.close();

                            if(updateAmount > 0){
                                sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + "The coins have been credited."));
                            } else {
                                sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.YELLOW + "The coins may not have been credit as that user was not found in the database."));
                            }
                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    } else {
                        sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "Unknown UUID."));
                    }
                } else {
                    sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "Please enter a valid number."));
                }
            } else {
                sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "/givecoins <Player> <Amount>"));
            }
        }
    }
}
