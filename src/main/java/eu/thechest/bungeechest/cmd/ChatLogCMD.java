package eu.thechest.bungeechest.cmd;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import eu.thechest.bungeechest.BungeeChest;
import eu.thechest.bungeechest.mysql.MySQLManager;
import eu.thechest.bungeechest.user.BungeeUser;
import eu.thechest.bungeechest.user.Rank;
import eu.thechest.bungeechest.util.StoredChatMessage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by zeryt on 29.03.2017.
 */
public class ChatLogCMD extends Command {
    public ChatLogCMD(){
        super("chatlog",null,new String[]{"chatreport"});
    }

    private ArrayList<String> cooldown = new ArrayList<String>();

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer)sender;
            BungeeUser u = BungeeUser.getUser(p);

            if(!cooldown.contains(p.getName())){
                if(args.length == 1){
                    String name = args[0];

                    if(!name.equalsIgnoreCase(p.getName())){
                        ProxiedPlayer p2 = BungeeChest.getInstance().getProxy().getPlayer(name);

                        if(p2 != null){
                            BungeeUser u2 = BungeeUser.getUser(p2);

                            if(u2.getStoredChatMessages().size() > 0){
                                if((!u.hasPermission(Rank.ADMIN) && !u2.hasPermission(Rank.ADMIN)) || (u.hasPermission(Rank.ADMIN))){
                                    if(p.getServer().getInfo().getName().equals(p2.getServer().getInfo().getName())){
                                        try {
                                            Gson gson = new Gson();
                                            String messagesJson = gson.toJson(u2.getStoredChatMessages());

                                            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `chatlogs` (`uuid`,`createdBy`,`messages`) VALUES(?,?,?);", Statement.RETURN_GENERATED_KEYS);
                                            ps.setString(1,p2.getUniqueId().toString());
                                            ps.setString(2,p.getUniqueId().toString());
                                            ps.setString(3,messagesJson);
                                            ps.executeUpdate();

                                            ResultSet rs = ps.getGeneratedKeys();
                                            if(rs.first()) {
                                                int logID = rs.getInt(1);

                                                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + u.getTranslatedMessage("The chatlog has been created!")));
                                                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.YELLOW + "https://thechest.eu/chatlog/?id=" + logID));

                                                cooldown.add(p.getName());

                                                BungeeChest.getInstance().getProxy().getScheduler().schedule(BungeeChest.getInstance(), new Runnable(){
                                                    @Override
                                                    public void run(){
                                                        cooldown.remove(p.getName());
                                                    }
                                                }, 1, TimeUnit.MINUTES);
                                            }

                                            MySQLManager.getInstance().closeResources(rs,ps);
                                        } catch (Exception e){
                                            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("An error occured.")));
                                            e.printStackTrace();
                                        }
                                    } else {
                                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("You have to be on the same server as that player.")));
                                    }
                                } else {
                                    p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("You can't create a chatlog of that player.")));
                                }
                            } else {
                                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("That player hasn't sent any messages yet.")));
                            }
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("That player is not online.")));
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("You can't create a chatlog of yourself.")));
                    }
                } else {
                    p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "/chatlog <" + u.getTranslatedMessage("Player") + ">"));
                }
            } else {
                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Please wait a little bit before executing this command again.")));
            }
        } else {
            sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "You must be a player to do this!"));
        }
    }
}
