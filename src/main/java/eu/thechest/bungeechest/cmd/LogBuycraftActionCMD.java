package eu.thechest.bungeechest.cmd;

import eu.thechest.bungeechest.mysql.MySQLManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.PreparedStatement;

/**
 * Created by zeryt on 26.02.2017.
 */
public class LogBuycraftActionCMD extends Command {
    public LogBuycraftActionCMD(){
        super("logbuycraftaction");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof ProxiedPlayer)){
            String name = args[0];
            String uuid = args[1];
            String transactionID = args[2];
            String price = args[3];
            String currency = args[4];
            String time = args[5];
            String date = args[6];
            String email = args[7];
            String ip = args[8];
            String packageId = args[9];
            String packagePrice = args[10];
            String packageExpiry = args[11];

            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `purchase_history` (`transactionID`,`name`,`uuid`,`price`,`currency`,`time`,`date`,`email`,`ip`,`packageId`,`packagePrice`,`packageExpiry`) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)");
                ps.setString(1,name);
                ps.setString(2,uuid);
                ps.setString(3,transactionID);
                ps.setString(4,price);
                ps.setString(5,currency);
                ps.setString(6,time);
                ps.setString(7,date);
                ps.setString(8,email);
                ps.setString(9,ip);
                ps.setString(10,packageId);
                ps.setString(11,packagePrice);
                ps.setString(12,packageExpiry);
                ps.execute();
                ps.close();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
