package eu.thechest.bungeechest.cmd;

import eu.thechest.bungeechest.BungeeChest;
import eu.thechest.bungeechest.TeamSpeakManager;
import eu.thechest.bungeechest.mysql.MySQLManager;
import eu.thechest.bungeechest.user.BungeeUser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by zeryt on 02.03.2017.
 */
public class TSVerifyCMD extends Command {
    public TSVerifyCMD(){
        super("ts");
    }

    private ArrayList<ProxiedPlayer> COOLDOWN = new ArrayList<ProxiedPlayer>();

    @Override
    public void execute(CommandSender sender, String[] args) {
        int cooldown = 2;

        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer)sender;
            BungeeUser u = BungeeUser.getUser(p);

            if(COOLDOWN.contains(p)){
                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Please wait a little bit before executing this command again.")));
                return;
            }

            BungeeChest.async(() -> {
                if(args.length == 0){
                    List<String> tsIdentities = TeamSpeakManager.getInstance().getTeamspeakIdentities(p.getUniqueId());
                    if(tsIdentities.size() > 0){
                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + u.getTranslatedMessage("Your account is currently linked with %i.").replace("%i",ChatColor.YELLOW + tsIdentities.get(0) + ChatColor.GREEN)));
                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Use /ts unlink to unlink your account.")));
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Your account is currently not linked with a teamspeak identity.")));
                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Use /ts <identity> to link your account.")));
                    }
                } else {
                    if(args[0].equalsIgnoreCase("unlink")){
                        COOLDOWN.add(p);
                        BungeeChest.getInstance().getProxy().getScheduler().schedule(BungeeChest.getInstance(), new Runnable(){
                            @Override
                            public void run(){
                                COOLDOWN.remove(p);
                            }
                        }, cooldown, TimeUnit.SECONDS);

                        List<String> tsIdentities = TeamSpeakManager.getInstance().getTeamspeakIdentities(p.getUniqueId());
                        if(tsIdentities.size() > 0){
                            String tsIdentity = tsIdentities.get(0);

                            if(TeamSpeakManager.getInstance().isVerified(p.getUniqueId(),tsIdentity)){
                                if(TeamSpeakManager.getInstance().isOnline(tsIdentity)){
                                    try {
                                        PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("DELETE FROM teamspeak_verify WHERE minecraft_uuid=? AND teamspeak_uuid=?");
                                        ps.setString(1, p.getUniqueId().toString());
                                        ps.setString(2, tsIdentity);
                                        ps.executeUpdate();
                                        ps.close();

                                        TeamSpeakManager.getInstance().takeRanks(tsIdentity);
                                        TeamSpeakManager.getInstance().takeIcon(tsIdentity);

                                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + u.getTranslatedMessage("Your account has been unlinked.")));
                                    } catch (Exception e){
                                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("An error occured.")));
                                        e.printStackTrace();
                                        return;
                                    }
                                } else {
                                    p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Please join the TeamSpeak server with that identity to unlink it.")));
                                }
                            } else {
                                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("That identity is not linked with your account.")));
                            }
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Your account is currently not linked with a teamspeak identity.")));
                            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Use /ts <identity> to link your account.")));
                        }
                    } else {
                        COOLDOWN.add(p);
                        BungeeChest.getInstance().getProxy().getScheduler().schedule(BungeeChest.getInstance(), new Runnable(){
                            @Override
                            public void run(){
                                COOLDOWN.remove(p);
                            }
                        }, cooldown, TimeUnit.SECONDS);

                        if(TeamSpeakManager.getInstance().getTeamspeakIdentities(p.getUniqueId()).size() > 0){
                            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Your account is already linked with a teamspeak identity.")));
                            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Use /ts unlink to unlink your account.")));
                        } else {
                            String tsIdentity = args[0];

                            if(!TeamSpeakManager.getInstance().isInVerifyProcess(p.getUniqueId())){
                                if(!TeamSpeakManager.getInstance().isVerified(tsIdentity)){
                                    if(TeamSpeakManager.getInstance().isOnline(tsIdentity)){
                                        try {
                                            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO teamspeak_verify (minecraft_uuid, teamspeak_uuid) VALUES (?, ?)");
                                            ps.setString(1, p.getUniqueId().toString());
                                            ps.setString(2, tsIdentity);
                                            ps.executeUpdate();
                                            ps.close();

                                            TeamSpeakManager.getInstance().sendMessage(tsIdentity,"Please write your Minecraft name in the chat to get verified.");
                                            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + "Your identity has been added. To continue please look at your Teamspeak window."));
                                            return;
                                        } catch (SQLException e) {
                                            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("An error occured.")));
                                            e.printStackTrace();
                                            return;
                                        }
                                    } else {
                                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Please join the TeamSpeak server with that identity to verify it.")));
                                    }
                                } else {
                                    p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("That identity is already linked with another account.")));
                                }
                            } else {
                                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("That identity is currently being linked.")));
                            }
                        }
                    }
                }
            });
        }
    }
}
