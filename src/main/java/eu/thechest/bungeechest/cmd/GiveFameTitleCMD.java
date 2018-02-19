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
import java.sql.ResultSet;
import java.util.UUID;

/**
 * Created by zeryt on 12.03.2017.
 */
@Deprecated
public class GiveFameTitleCMD extends Command {
     public GiveFameTitleCMD(){
         super("givefametitle");
     }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer)sender;
            BungeeUser u = BungeeUser.getUser(p);

            if(u.hasPermission(Rank.CM)){
                if(args.length == 2){
                    String name = args[0];

                    if(BungeeChest.getInstance().isValidInteger(args[1])){
                        int fameTitleID = Integer.parseInt(args[1]);
                        UUID uuid = PlayerUtilities.getUUIDFromName(name);

                        if(uuid != null){
                            try {
                                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `unlocked_fameTitles` WHERE `uuid` = ? AND `title` = ?");
                                ps.setString(1,uuid.toString());
                                ps.setInt(2,fameTitleID);
                                ResultSet rs = ps.executeQuery();

                                if(!rs.first()){
                                    PreparedStatement insert = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `unlocked_fameTitles` (`uuid`,`title`) VALUES(?,?)");
                                    insert.setString(1,uuid.toString());
                                    insert.setInt(2,fameTitleID);
                                    insert.execute();
                                    insert.close();

                                    p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + u.getTranslatedMessage("The fame title has been given.")));
                                } else {
                                    p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("That user already has that fame title.")));
                                }

                                MySQLManager.getInstance().closeResources(rs,ps);
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
                    p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "/givefametitle <" + u.getTranslatedMessage("Player") + "> <" + u.getTranslatedMessage("Title ID") + ">"));
                }
            } else {
                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("You aren't permitted to execute this command.")));
            }
        } else {

        }
    }
}
