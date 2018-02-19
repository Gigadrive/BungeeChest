package eu.thechest.bungeechest.cmd;

import eu.thechest.bungeechest.BungeeChest;
import eu.thechest.bungeechest.mysql.MySQLManager;
import eu.thechest.bungeechest.user.BungeeUser;
import eu.thechest.bungeechest.user.Rank;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by zeryt on 04.03.2017.
 */
public class WebInterfaceCMD extends Command {
    public WebInterfaceCMD(){
        super("wiaccess");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer p = (ProxiedPlayer)sender;
            BungeeUser u = BungeeUser.getUser(p);

            if(u.hasPermission(Rank.MOD)){
                BungeeChest.async(() -> {
                    try {
                        PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM webinterface_users WHERE mcUUID=?");
                        ps.setString(1, p.getUniqueId().toString());
                        ResultSet rs = ps.executeQuery();
                        if(rs.first()) {
                            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("You already have an account.")));
                        } else {
                            // create new webinterface account
                            String password = UUID.randomUUID().toString();
                            PreparedStatement ps2 = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO webinterface_users (mcUUID, passwordHash) VALUES (?, ?)");
                            ps2.setString(1, p.getUniqueId().toString());
                            ps2.setString(2, getHashCodeFromString("SHA-512", password));
                            ps2.executeUpdate();
                            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + u.getTranslatedMessage("Password") + ": " + ChatColor.YELLOW + password));
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("You aren't permitted to execute this command.")));
            }
        } else {
            sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "You must be a player to do this!"));
        }
    }

    private String getHashCodeFromString(String algorithm, String str) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(str.getBytes());
            byte byteData[] = md.digest();

            //convert the byte to hex format method 1
            StringBuffer hashCodeBuffer = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                hashCodeBuffer.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }
            return hashCodeBuffer.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
