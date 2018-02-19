package eu.thechest.bungeechest.cmd;

import eu.thechest.bungeechest.BungeeChest;
import eu.thechest.bungeechest.ban.BanUtilities;
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
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.UUID;

/**
 * Created by zeryt on 24.02.2017.
 */
@Deprecated
public class TempBanCMD extends Command {
    public TempBanCMD(){
        super("tempban", null, new String[]{"minecraft:tempban","bukkit:tempban","essentials:tempban"});
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer p = (ProxiedPlayer) sender;
            BungeeUser u = BungeeUser.getUser(p);

            if (u.hasPermission(Rank.MOD)) {
                if(args.length >= 3){
                    String playerName = args[0];
                    UUID uuid = PlayerUtilities.getUUIDFromName(playerName);

                    if(uuid != null){
                        playerName = PlayerUtilities.getNameFromUUID(uuid);
                        Rank rank = PlayerUtilities.getRankFromUUID(uuid);

                        if(u.getRank().getID() >= rank.getID()){
                            String reason;

                            if(args.length == 3){
                                reason = null;
                            } else {
                                StringBuilder sb = new StringBuilder("");
                                for (int i = 3; i < args.length; i++) {
                                    sb.append(args[i]).append(" ");
                                }
                                reason = sb.toString();
                            }

                            String server = null;
                            if(BungeeChest.getInstance().getProxy().getPlayer(playerName) != null) server = BungeeChest.getInstance().getProxy().getPlayer(playerName).getServer().getInfo().getName();

                            String timeString = args[1];
                            String timeUnit = args[2];

                            if(BungeeChest.getInstance().isValidInteger(timeString) && Integer.parseInt(timeString) > 0){
                                if(!BanUtilities.isBanned(uuid)){
                                    int time = Integer.parseInt(timeString);
                                    Timestamp now = new Timestamp(System.currentTimeMillis());
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTimeInMillis(now.getTime());

                                    if(timeUnit.equalsIgnoreCase("s") || timeUnit.equalsIgnoreCase("sec") || timeUnit.equalsIgnoreCase("sek") || timeUnit.equalsIgnoreCase("seconds") || timeUnit.equalsIgnoreCase("sekunden") || timeUnit.equalsIgnoreCase("second") || timeUnit.equalsIgnoreCase("sekunde")){
                                        // SECOND

                                        cal.add(Calendar.SECOND, time);
                                    } else if(timeUnit.equalsIgnoreCase("m") || timeUnit.equalsIgnoreCase("min") || timeUnit.equalsIgnoreCase("minute") || timeUnit.equalsIgnoreCase("minutes")){
                                        // MINUTE

                                        cal.add(Calendar.MINUTE, time);
                                    } else if(timeUnit.equalsIgnoreCase("h") || timeUnit.equalsIgnoreCase("hour") || timeUnit.equalsIgnoreCase("hours")){
                                        // HOUR

                                        cal.add(Calendar.HOUR, time);
                                    } else if(timeUnit.equalsIgnoreCase("d") || timeUnit.equalsIgnoreCase("day") || timeUnit.equalsIgnoreCase("days")){
                                        // DAY

                                        cal.add(Calendar.HOUR, time*24);
                                    } else if(timeUnit.equalsIgnoreCase("w") || timeUnit.equalsIgnoreCase("week") || timeUnit.equalsIgnoreCase("weeks")){
                                        // WEEK

                                        cal.add(Calendar.HOUR, time*24*7);
                                    } else if(timeUnit.equalsIgnoreCase("mo") || timeUnit.equalsIgnoreCase("mon") || timeUnit.equalsIgnoreCase("month") || timeUnit.equalsIgnoreCase("months")){
                                        // MONTH

                                        cal.add(Calendar.MONTH, time);
                                    } else if(timeUnit.equalsIgnoreCase("y") || timeUnit.equalsIgnoreCase("year") || timeUnit.equalsIgnoreCase("years")){
                                        // YEAR

                                        cal.add(Calendar.YEAR, time);
                                    } else {
                                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("bans.invalidTimeUnit")));
                                        return;
                                    }

                                    Timestamp endDate = new Timestamp(cal.getTime().getTime());

                                    try {
                                        PreparedStatement second = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `sn_bans` (`bannedPlayer`, `bannedBy`, `reason`, `nameWhenBanned`, `server`, `time`, `expiry`) VALUES(?,?,?,?,?,?,?)");
                                        second.setString(1, uuid.toString());
                                        second.setString(2, p.getUniqueId().toString());
                                        second.setString(3, reason);
                                        second.setString(4, playerName);
                                        second.setString(5, server);
                                        second.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                                        second.setTimestamp(7, endDate);
                                        second.execute();
                                        second.close();
                                    } catch(Exception e){
                                        e.printStackTrace();
                                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("An error occured.")));
                                        return;
                                    }

                                    if(BungeeChest.getInstance().getProxy().getPlayer(playerName) != null){
                                        ProxiedPlayer p2 = BungeeChest.getInstance().getProxy().getPlayer(playerName);
                                        BungeeUser u2 = BungeeUser.getUser(p2);

                                        if(reason == null){
                                            p2.disconnect(TextComponent.fromLegacyText(
                                                    ChatColor.GOLD.toString() + ChatColor.BOLD + "TheChest.eu" + "\n" +
                                                            ChatColor.RED + u2.getTranslatedMessage("You were banned from this server.") + "\n" +
                                                            "\n" +
                                                            ChatColor.YELLOW + u2.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + u2.getTranslatedMessage("No reason has been specified.") + "\n" +
                                                            ChatColor.YELLOW + u2.getTranslatedMessage("Expiry") + ": " + ChatColor.WHITE + endDate.toGMTString() + "\n\n" +
                                                            ChatColor.GRAY + "Appeal at " + ChatColor.AQUA.toString() + ChatColor.UNDERLINE.toString() + "https://go.thechest.eu/appeal"
                                            ));
                                        } else {
                                            p2.disconnect(TextComponent.fromLegacyText(
                                                    ChatColor.GOLD.toString() + ChatColor.BOLD + "TheChest.eu" + "\n" +
                                                            ChatColor.RED + u2.getTranslatedMessage("You were banned from this server.") + "\n" +
                                                            "\n" +
                                                            ChatColor.YELLOW + u2.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + reason + "\n" +
                                                            ChatColor.YELLOW + u2.getTranslatedMessage("Expiry") + ": " + ChatColor.WHITE + endDate.toGMTString() + "\n\n" +
                                                            ChatColor.GRAY + "Appeal at " + ChatColor.AQUA.toString() + ChatColor.UNDERLINE.toString() + "https://go.thechest.eu/appeal"
                                            ));


                                        }
                                    }

                                    if(reason == null){
                                        for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                            BungeeUser a = BungeeUser.getUser(all);
                                            all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p banned %u for %t.").replace("%p",u.getRank().getColor() + p.getName() + ChatColor.YELLOW).replace("%u",rank.getColor() + playerName + ChatColor.YELLOW).replace("%t",ChatColor.WHITE.toString() + time + timeUnit + ChatColor.YELLOW)));
                                        }
                                    } else {
                                        for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                            BungeeUser a = BungeeUser.getUser(all);
                                            all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p banned %u with reason %r for %t.").replace("%p",u.getRank().getColor() + p.getName() + ChatColor.YELLOW).replace("%u",rank.getColor() + playerName + ChatColor.YELLOW).replace("%r",ChatColor.WHITE + reason + ChatColor.YELLOW).replace("%t",ChatColor.WHITE.toString() + time + timeUnit + ChatColor.YELLOW)));
                                        }
                                    }
                                } else {
                                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("That player is already banned.")));
                                }
                            } else {
                                sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Please enter a valid number.")));
                            }
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("You can't ban that player.")));
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Unknown UUID.")));
                    }
                } else {
                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "/tempban <Player> <Time> <TimeUnit> [Reason]"));
                }
            } else {
                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("You aren't permitted to execute this command.")));
            }
        } else {
            if(args.length >= 3){
                String playerName = args[0];
                UUID uuid = PlayerUtilities.getUUIDFromName(playerName);

                if(uuid != null){
                    playerName = PlayerUtilities.getNameFromUUID(uuid);
                    Rank rank = PlayerUtilities.getRankFromUUID(uuid);

                    if(true){
                        String reason;

                        if(args.length == 3){
                            reason = null;
                        } else {
                            StringBuilder sb = new StringBuilder("");
                            for (int i = 3; i < args.length; i++) {
                                sb.append(args[i]).append(" ");
                            }
                            reason = sb.toString().trim();
                        }

                        String server = null;
                        if(BungeeChest.getInstance().getProxy().getPlayer(playerName) != null) server = BungeeChest.getInstance().getProxy().getPlayer(playerName).getServer().getInfo().getName();

                        String timeString = args[1];
                        String timeUnit = args[2];

                        if(BungeeChest.getInstance().isValidInteger(timeString) && Integer.parseInt(timeString) > 0){
                            if(!BanUtilities.isBanned(uuid)){
                                int time = Integer.parseInt(timeString);
                                Timestamp now = new Timestamp(System.currentTimeMillis());
                                Calendar cal = Calendar.getInstance();
                                cal.setTimeInMillis(now.getTime());

                                if(timeUnit.equalsIgnoreCase("s") || timeUnit.equalsIgnoreCase("sec") || timeUnit.equalsIgnoreCase("sek") || timeUnit.equalsIgnoreCase("seconds") || timeUnit.equalsIgnoreCase("sekunden") || timeUnit.equalsIgnoreCase("second") || timeUnit.equalsIgnoreCase("sekunde")){
                                    // SECOND

                                    cal.add(Calendar.SECOND, time);
                                } else if(timeUnit.equalsIgnoreCase("m") || timeUnit.equalsIgnoreCase("min") || timeUnit.equalsIgnoreCase("minute") || timeUnit.equalsIgnoreCase("minutes")){
                                    // MINUTE

                                    cal.add(Calendar.MINUTE, time);
                                } else if(timeUnit.equalsIgnoreCase("h") || timeUnit.equalsIgnoreCase("hour") || timeUnit.equalsIgnoreCase("hours")){
                                    // HOUR

                                    cal.add(Calendar.HOUR, time);
                                } else if(timeUnit.equalsIgnoreCase("d") || timeUnit.equalsIgnoreCase("day") || timeUnit.equalsIgnoreCase("days")){
                                    // DAY

                                    cal.add(Calendar.HOUR, time*24);
                                } else if(timeUnit.equalsIgnoreCase("w") || timeUnit.equalsIgnoreCase("week") || timeUnit.equalsIgnoreCase("weeks")){
                                    // WEEK

                                    cal.add(Calendar.HOUR, time*24*7);
                                } else if(timeUnit.equalsIgnoreCase("mo") || timeUnit.equalsIgnoreCase("mon") || timeUnit.equalsIgnoreCase("month") || timeUnit.equalsIgnoreCase("months")){
                                    // MONTH

                                    cal.add(Calendar.MONTH, time);
                                } else if(timeUnit.equalsIgnoreCase("y") || timeUnit.equalsIgnoreCase("year") || timeUnit.equalsIgnoreCase("years")){
                                    // YEAR

                                    cal.add(Calendar.YEAR, time);
                                } else {
                                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "Invalid Time Unit."));
                                    return;
                                }

                                Timestamp endDate = new Timestamp(cal.getTime().getTime());

                                try {
                                    PreparedStatement second = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `sn_bans` (`bannedPlayer`, `bannedBy`, `reason`, `nameWhenBanned`, `server`, `time`, `expiry`) VALUES(?,?,?,?,?,?,?)");
                                    second.setString(1, uuid.toString());
                                    second.setString(2, null);
                                    second.setString(3, reason);
                                    second.setString(4, playerName);
                                    second.setString(5, server);
                                    second.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                                    second.setTimestamp(7, endDate);
                                    second.execute();
                                    second.close();
                                } catch(Exception e){
                                    e.printStackTrace();
                                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "An error occured."));
                                    return;
                                }

                                if(BungeeChest.getInstance().getProxy().getPlayer(playerName) != null){
                                    ProxiedPlayer p2 = BungeeChest.getInstance().getProxy().getPlayer(playerName);
                                    BungeeUser u2 = BungeeUser.getUser(p2);

                                    if(reason == null){
                                        p2.disconnect(TextComponent.fromLegacyText(
                                                ChatColor.GOLD.toString() + ChatColor.BOLD + "TheChest.eu" + "\n" +
                                                        ChatColor.RED + u2.getTranslatedMessage("You were banned from this server.") + "\n" +
                                                        "\n" +
                                                        ChatColor.YELLOW + u2.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + u2.getTranslatedMessage("No reason has been specified.") + "\n" +
                                                        ChatColor.YELLOW + u2.getTranslatedMessage("Expiry") + ": " + ChatColor.WHITE + endDate.toGMTString() + "\n"
                                        ));
                                    } else {
                                        p2.disconnect(TextComponent.fromLegacyText(
                                                ChatColor.GOLD.toString() + ChatColor.BOLD + "TheChest.eu" + "\n" +
                                                        ChatColor.RED + u2.getTranslatedMessage("You were banned from this server.") + "\n" +
                                                        "\n" +
                                                        ChatColor.YELLOW + u2.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + reason + "\n" +
                                                        ChatColor.YELLOW + u2.getTranslatedMessage("Expiry") + ": " + ChatColor.WHITE + endDate.toGMTString() + "\n"
                                        ));


                                    }
                                }

                                if(reason == null){
                                    for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                        BungeeUser a = BungeeUser.getUser(all);
                                        all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p banned %u for %t.").replace("%p",ChatColor.DARK_PURPLE + "BungeeConsole" + ChatColor.YELLOW).replace("%u",rank.getColor() + playerName + ChatColor.YELLOW).replace("%t",ChatColor.WHITE.toString() + time + timeUnit + ChatColor.YELLOW)));
                                    }
                                } else {
                                    for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                        BungeeUser a = BungeeUser.getUser(all);
                                        all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p banned %u with reason %r for %t.").replace("%p",ChatColor.DARK_PURPLE + "BungeeConsole" + ChatColor.YELLOW).replace("%u",rank.getColor() + playerName + ChatColor.YELLOW).replace("%r",ChatColor.WHITE + reason + ChatColor.YELLOW).replace("%t",ChatColor.WHITE.toString() + time + timeUnit + ChatColor.YELLOW)));
                                    }
                                }
                            } else {
                                sender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "That player is already banned."));
                            }
                        } else {
                            sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "Invalid integer."));
                        }
                    } else {
                        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "That player cannot be muted."));
                    }
                } else {
                    sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "Unknown UUID."));
                }
            } else {
                sender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "/tempmute <Player> <Time> <TimeUnit> [Reason]"));
            }
        }
    }
}
