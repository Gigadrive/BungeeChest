package eu.thechest.bungeechest.cmd;

import eu.thechest.bungeechest.BungeeChest;
import eu.thechest.bungeechest.mysql.MySQLManager;
import eu.thechest.bungeechest.user.BungeeUser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Command;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by zeryt on 26.05.2017.
 */
public class DiscordCMD extends Command {
    public static ArrayList<ProxiedPlayer> CACHE = new ArrayList<ProxiedPlayer>();

    public DiscordCMD(){
        super("discord");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer)sender;
            BungeeUser u = BungeeUser.getUser(p);

            if(CACHE.contains(p)){
                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Please wait a little bit before executing this command again.")));
                return;
            }

            CACHE.add(p);
            BungeeChest.getInstance().getProxy().getScheduler().schedule(BungeeChest.getInstance(), new Runnable(){
                @Override
                public void run(){
                    CACHE.remove(p);
                }
            }, 5, TimeUnit.SECONDS);

            if(args.length == 1){
                String code = args[0];

                try {
                    // CHECK IF USER IS ALREADY LINKED
                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `discord_connections` WHERE `minecraft_uuid` = ?");
                    ps.setString(1,p.getUniqueId().toString());

                    ResultSet rs = ps.executeQuery();

                    if(rs.first()){
                        MySQLManager.getInstance().closeResources(rs,ps);
                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("You've already linked your discord account.")));
                        return;
                    }

                    MySQLManager.getInstance().closeResources(rs,ps);

                    // CHECK IF CODE IS PRE-SAVED BY BOT

                    ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `discord_authCodes` WHERE `code` = ?");
                    ps.setString(1,code);

                    rs = ps.executeQuery();

                    if(!rs.first()){
                        MySQLManager.getInstance().closeResources(rs,ps);
                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Please join our discord server and message our bot with !register.")));
                        return;
                    }

                    String discordID = rs.getString("discord_user");

                    MySQLManager.getInstance().closeResources(rs,ps);

                    // FILL UUID COLUMN
                    ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `discord_authCodes` SET `minecraftUUID` = ? WHERE `discord_user` = ?");
                    ps.setString(1,p.getUniqueId().toString());
                    ps.setString(2,discordID);
                    ps.executeUpdate();
                    ps.close();

                    p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + u.getTranslatedMessage("Success! To complete the registration, please message our bot with %s again.").replace("%s",ChatColor.YELLOW.toString() + ChatColor.BOLD.toString() + "!register" + ChatColor.GREEN)));
                } catch(Exception e){
                    e.printStackTrace();
                    p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("An error occured.")));
                }
            } else {
                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "/discord <Auth-Code>"));
                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + u.getTranslatedMessage("Join our discord server") + ": " + ChatColor.YELLOW + "https://discord.gg/CE38j3h"));
            }
        }
    }
}
