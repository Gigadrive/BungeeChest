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
 * Created by zeryt on 12.04.2017.
 */
public class GiveItemCMD extends Command {
    public GiveItemCMD(){
        super("giveitem");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer)sender;
            BungeeUser u = BungeeUser.getUser(p);

            if(u.hasPermission(Rank.ADMIN)){
                if(args.length == 2){
                    String name = args[0];
                    UUID uuid = PlayerUtilities.getUUIDFromName(name);

                    if(uuid != null){
                        if(BungeeChest.getInstance().isValidInteger(args[1])){
                            int itemID = Integer.parseInt(args[1]);

                            try {
                                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `lobbyShop_boughtItems` WHERE `uuid` = ? AND `lobbyShopItem` = ?");
                                ps.setString(1,uuid.toString());
                                ps.setInt(2,itemID);
                                ResultSet rs = ps.executeQuery();

                                if(!rs.first()){
                                    PreparedStatement insert = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `lobbyShop_boughtItems` (`uuid`,`lobbyShopItem`,`active`) VALUES(?,?,?)");
                                    insert.setString(1,uuid.toString());
                                    insert.setInt(2,itemID);
                                    insert.setBoolean(3,false);
                                    insert.executeUpdate();

                                    p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + u.getTranslatedMessage("The item has been credited.")));

                                    if(BungeeChest.getInstance().getProxy().getPlayer(uuid) != null){
                                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.YELLOW + u.getTranslatedMessage("A reconnect might be necessary.")));
                                    }
                                } else {
                                    p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("That player already has that item.")));
                                }

                                MySQLManager.getInstance().closeResources(rs,ps);
                            } catch(Exception e){
                                e.printStackTrace();
                                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("An error occured.")));
                            }
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Please enter a valid number.")));
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Unknown UUID.")));
                    }
                } else {
                    p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "/giveitem <" + u.getTranslatedMessage("Player") + "> <Item-ID>"));
                }
            } else {
                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("You aren't permitted to execute this command.")));
            }
        } else {
            if(args.length == 2){
                String name = args[0];
                UUID uuid = PlayerUtilities.getUUIDFromName(name);

                if(uuid != null){
                    if(BungeeChest.getInstance().isValidInteger(args[1])){
                        int itemID = Integer.parseInt(args[1]);

                        try {
                            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `lobbyShop_boughtItems` WHERE `uuid` = ? AND `lobbyShopItem` = ?");
                            ps.setString(1,uuid.toString());
                            ps.setInt(2,itemID);
                            ResultSet rs = ps.executeQuery();

                            if(!rs.first()){
                                PreparedStatement insert = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `lobbyShop_boughtItems` (`uuid`,`lobbyShopItem`,`active`) VALUES(?,?,?)");
                                insert.setString(1,uuid.toString());
                                insert.setInt(2,itemID);
                                insert.setBoolean(3,false);
                                insert.executeUpdate();

                                sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + "The item has been credited."));

                                if(BungeeChest.getInstance().getProxy().getPlayer(uuid) != null){
                                    sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.YELLOW + "A reconnect might be necessary."));
                                }
                            } else {
                                sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "That player already has that item."));
                            }

                            MySQLManager.getInstance().closeResources(rs,ps);
                        } catch(Exception e){
                            e.printStackTrace();
                            sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "An error occured."));
                        }
                    } else {
                        sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "Please enter a valid number."));
                    }
                } else {
                    sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "Unknown UUID."));
                }
            } else {
                sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "/giveitem <Player> <Item-ID>"));
            }
        }
    }
}
