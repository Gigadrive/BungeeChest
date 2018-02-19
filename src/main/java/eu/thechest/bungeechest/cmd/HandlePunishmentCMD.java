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

public class HandlePunishmentCMD extends Command {
    public HandlePunishmentCMD(){
        super("handlepunishment");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        BungeeChest.async(() -> {
            if(!(sender instanceof ProxiedPlayer)){
                if(args.length == 3){
                    String executor = args[0];
                    String victimName = args[1];
                    BanReason reason = null;

                    for(BanReason r : BanReason.values()) if(r.toString().equalsIgnoreCase(args[2])) reason = r;

                    if(reason != null){
                        ProxiedPlayer p = BungeeChest.getInstance().getProxy().getPlayer(executor);

                        if(p != null){
                            BungeeUser u = BungeeUser.getUser(p);

                            if(u.hasPermission(Rank.MOD)){
                                UUID victim = PlayerUtilities.getUUIDFromName(victimName);

                                if(victim != null){
                                    Rank rank = PlayerUtilities.getRankFromUUID(victim);
                                    String playerName = PlayerUtilities.getNameFromUUID(victim);
                                    UUID uuid = victim;

                                    String server = null;
                                    if(BungeeChest.getInstance().getProxy().getPlayer(playerName) != null) server = BungeeChest.getInstance().getProxy().getPlayer(playerName).getServer().getInfo().getName();

                                    if(reason.punishment == Punishment.BAN){
                                        if(reason.duration == null || reason.duration.isEmpty() || reason.duration.equalsIgnoreCase("permanent")){
                                            if(!BanUtilities.isBanned(uuid)){
                                                try {
                                                    PreparedStatement second = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `sn_bans` (`bannedPlayer`, `bannedBy`, `reason`, `nameWhenBanned`, `server`) VALUES(?,?,?,?,?)");
                                                    second.setString(1, uuid.toString());
                                                    second.setString(2, p.getUniqueId().toString());
                                                    second.setString(3, reason.nicename);
                                                    second.setString(4, playerName);
                                                    second.setString(5, server);
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
                                                                        ChatColor.YELLOW + u2.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + u2.getTranslatedMessage("No reason has been specified.") + "\n\n" +
                                                                        ChatColor.GRAY + "Appeal at " + ChatColor.AQUA.toString() + ChatColor.UNDERLINE.toString() + "https://go.thechest.eu/appeal"
                                                        ));
                                                    } else {
                                                        p2.disconnect(TextComponent.fromLegacyText(
                                                                ChatColor.GOLD.toString() + ChatColor.BOLD + "TheChest.eu" + "\n" +
                                                                        ChatColor.RED + u2.getTranslatedMessage("You were banned from this server.") + "\n" +
                                                                        "\n" +
                                                                        ChatColor.YELLOW + u2.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + reason + "\n\n" +
                                                                        ChatColor.GRAY + "Appeal at " + ChatColor.AQUA.toString() + ChatColor.UNDERLINE.toString() + "https://go.thechest.eu/appeal"
                                                        ));
                                                    }
                                                }

                                                if(reason == null){
                                                    for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                                        BungeeUser a = BungeeUser.getUser(all);
                                                        all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p banned %u").replace("%p",u.getRank().getColor() + p.getName() + ChatColor.YELLOW).replace("%u",rank.getColor() + playerName + ChatColor.YELLOW)));
                                                    }
                                                } else {
                                                    for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                                        BungeeUser a = BungeeUser.getUser(all);
                                                        all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p banned %u with reason %r.").replace("%p",u.getRank().getColor() + p.getName() + ChatColor.YELLOW).replace("%u",rank.getColor() + playerName + ChatColor.YELLOW).replace("%r",ChatColor.WHITE + reason.nicename + ChatColor.YELLOW)));
                                                    }
                                                }
                                            } else {
                                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("That player is already banned.")));
                                            }
                                        } else {
                                            String timeString = reason.duration.split(" ")[0];
                                            String timeUnit = reason.duration.split(" ")[1];

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
                                                        second.setString(3, reason.nicename);
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
                                                            all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p banned %u with reason %r for %t.").replace("%p",u.getRank().getColor() + p.getName() + ChatColor.YELLOW).replace("%u",rank.getColor() + playerName + ChatColor.YELLOW).replace("%r",ChatColor.WHITE + reason.nicename + ChatColor.YELLOW).replace("%t",ChatColor.WHITE.toString() + time + timeUnit + ChatColor.YELLOW)));
                                                        }
                                                    }
                                                } else {
                                                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("That player is already banned.")));
                                                }
                                            } else {
                                                sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Please enter a valid number.")));
                                            }
                                        }
                                    } else if(reason.punishment == Punishment.MUTE){
                                        if(reason.duration == null || reason.duration.isEmpty() || reason.duration.equalsIgnoreCase("permanent")){
                                            if(!BanUtilities.isMuted(uuid)){
                                                try {
                                                    PreparedStatement second = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `sn_mutes` (`mutedPlayer`, `mutedBy`, `reason`, `nameWhenMuted`, `server`, `time`, `expiry`) VALUES(?,?,?,?,?,?,?)");
                                                    second.setString(1, uuid.toString());
                                                    second.setString(2, p.getUniqueId().toString());
                                                    second.setString(3, reason.nicename);
                                                    second.setString(4, playerName);
                                                    second.setString(5, server);
                                                    second.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                                                    second.setTimestamp(7, null);
                                                    second.execute();
                                                    second.close();
                                                } catch(Exception e){
                                                    e.printStackTrace();
                                                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("cmd.anErrorOccured")));
                                                    return;
                                                }

                                                if(BungeeChest.getInstance().getProxy().getPlayer(playerName) != null){
                                                    ProxiedPlayer p2 = BungeeChest.getInstance().getProxy().getPlayer(playerName);
                                                    BungeeUser u2 = BungeeUser.getUser(p2);

                                                    if(reason == null){
                                                        p2.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u2.getTranslatedMessage("You were muted.")));
                                                        p2.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u2.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + u2.getTranslatedMessage("No reason has been specified.")));
                                                    } else {
                                                        p2.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u2.getTranslatedMessage("You were muted.")));
                                                        p2.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u2.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + reason));
                                                    }
                                                }

                                                if(reason == null){
                                                    for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                                        BungeeUser a = BungeeUser.getUser(all);
                                                        all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p muted %u").replace("%p",u.getRank().getColor() + p.getName() + ChatColor.YELLOW).replace("%u",rank.getColor() + playerName + ChatColor.YELLOW)));
                                                    }
                                                } else {
                                                    for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                                        BungeeUser a = BungeeUser.getUser(all);
                                                        all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p muted %u with reason %r.").replace("%p",u.getRank().getColor() + p.getName() + ChatColor.YELLOW).replace("%u",rank.getColor() + playerName + ChatColor.YELLOW).replace("%r",ChatColor.WHITE + reason.nicename + ChatColor.YELLOW)));
                                                    }
                                                }
                                            } else {
                                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("That player is already muted.")));
                                            }
                                        } else {
                                            String timeString = reason.duration.split(" ")[0];
                                            String timeUnit = reason.duration.split(" ")[1];

                                            if(BungeeChest.getInstance().isValidInteger(timeString) && Integer.parseInt(timeString) > 0){
                                                if(!BanUtilities.isMuted(uuid)){
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
                                                        PreparedStatement second = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `sn_mutes` (`mutedPlayer`, `mutedBy`, `reason`, `nameWhenMuted`, `server`, `time`, `expiry`) VALUES(?,?,?,?,?,?,?)");
                                                        second.setString(1, uuid.toString());
                                                        second.setString(2, p.getUniqueId().toString());
                                                        second.setString(3, reason.nicename);
                                                        second.setString(4, playerName);
                                                        second.setString(5, server);
                                                        second.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                                                        second.setTimestamp(7, endDate);
                                                        second.execute();
                                                        second.close();
                                                    } catch(Exception e){
                                                        e.printStackTrace();
                                                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("cmd.anErrorOccured")));
                                                        return;
                                                    }

                                                    if(BungeeChest.getInstance().getProxy().getPlayer(playerName) != null){
                                                        ProxiedPlayer p2 = BungeeChest.getInstance().getProxy().getPlayer(playerName);
                                                        BungeeUser u2 = BungeeUser.getUser(p2);

                                                        if(reason == null){
                                                            p2.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u2.getTranslatedMessage("You were muted.")));
                                                            p2.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u2.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + u2.getTranslatedMessage("No reason has been specified.")));
                                                            p2.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u2.getTranslatedMessage("Expiry") + ": " + ChatColor.WHITE + endDate.toGMTString()));
                                                        } else {
                                                            p2.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u2.getTranslatedMessage("You were muted.")));
                                                            p2.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u2.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + reason.nicename));
                                                            p2.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u2.getTranslatedMessage("Expiry") + ": " + ChatColor.WHITE + endDate.toGMTString()));
                                                        }
                                                    }

                                                    if(reason == null){
                                                        for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                                            BungeeUser a = BungeeUser.getUser(all);
                                                            all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p muted %u for %t.").replace("%p",u.getRank().getColor() + p.getName() + ChatColor.YELLOW).replace("%u",rank.getColor() + playerName + ChatColor.YELLOW).replace("%t",ChatColor.WHITE.toString() + time + timeUnit + ChatColor.YELLOW)));
                                                        }
                                                    } else {
                                                        for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                                            BungeeUser a = BungeeUser.getUser(all);
                                                            all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p muted %u with reason %r for %t.").replace("%p",u.getRank().getColor() + p.getName() + ChatColor.YELLOW).replace("%u",rank.getColor() + playerName + ChatColor.YELLOW).replace("%r",ChatColor.WHITE + reason.nicename + ChatColor.YELLOW).replace("%t",ChatColor.WHITE.toString() + time + timeUnit + ChatColor.YELLOW)));
                                                        }
                                                    }
                                                } else {
                                                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("That player is already muted.")));
                                                }
                                            } else {
                                                sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Please enter a valid number.")));
                                            }
                                        }
                                    } else if(reason.punishment == Punishment.KICK){
                                        ProxiedPlayer p2 = BungeeChest.getInstance().getProxy().getPlayer(victim);
                                        if(p2 == null){
                                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u.getTranslatedMessage("That player is not online.")));

                                            return;
                                        }

                                        BungeeUser u2 = BungeeUser.getUser(p2);

                                        try {
                                            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `sn_kicks` (`kickedPlayer`,`nameWhenKicked`,`reason`,`server`,`kickedBy`) VALUES(?,?,?,?,?)");
                                            ps.setString(1,p2.getUniqueId().toString());
                                            ps.setString(2,p2.getName());
                                            ps.setString(3,reason.nicename);
                                            ps.setString(4,p2.getServer().getInfo().getName());
                                            ps.setString(5,p.getUniqueId().toString());
                                            ps.execute();
                                            ps.close();

                                            if(reason == null){
                                                p2.disconnect(
                                                        TextComponent.fromLegacyText(
                                                                ChatColor.GOLD.toString() + ChatColor.BOLD + "TheChest.eu" + "\n" +
                                                                        ChatColor.RED + u2.getTranslatedMessage("You were kicked.") + "\n" +
                                                                        "\n" +
                                                                        ChatColor.YELLOW + u2.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + u2.getTranslatedMessage("No reason has been specified.")
                                                        )
                                                );

                                                for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                                    BungeeUser a = BungeeUser.getUser(all);
                                                    all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p kicked %u").replace("%p",u.getRank().getColor() + p.getName() + ChatColor.YELLOW).replace("%u",u2.getRank().getColor() + p2.getName() + ChatColor.YELLOW)));
                                                }
                                            } else {
                                                p2.disconnect(
                                                        TextComponent.fromLegacyText(
                                                                ChatColor.GOLD.toString() + ChatColor.BOLD + "TheChest.eu" + "\n" +
                                                                        ChatColor.RED + u2.getTranslatedMessage("You were kicked.") + "\n" +
                                                                        "\n" +
                                                                        ChatColor.YELLOW + u2.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + reason
                                                        )
                                                );

                                                for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                                    BungeeUser a = BungeeUser.getUser(all);
                                                    all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p kicked %u with reason: %r.").replace("%p",u.getRank().getColor() + p.getName() + ChatColor.YELLOW).replace("%u",u2.getRank().getColor() + p2.getName() + ChatColor.YELLOW).replace("%r",ChatColor.WHITE + reason.nicename + ChatColor.YELLOW)));
                                                }
                                            }
                                        } catch (Exception e){
                                            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("An error occured.")));
                                            e.printStackTrace();
                                            return;
                                        }
                                    }
                                }
                            }
                        } else if(executor.equalsIgnoreCase("BungeeConsole")){
                            UUID victim = PlayerUtilities.getUUIDFromName(victimName);

                            if(victim != null){
                                Rank rank = PlayerUtilities.getRankFromUUID(victim);
                                String playerName = PlayerUtilities.getNameFromUUID(victim);
                                UUID uuid = victim;

                                String server = null;
                                if(BungeeChest.getInstance().getProxy().getPlayer(playerName) != null) server = BungeeChest.getInstance().getProxy().getPlayer(playerName).getServer().getInfo().getName();

                                if(reason.punishment == Punishment.BAN){
                                    if(reason.duration == null || reason.duration.isEmpty() || reason.duration.equalsIgnoreCase("permanent")){
                                        if(!BanUtilities.isBanned(uuid)){
                                            try {
                                                PreparedStatement second = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `sn_bans` (`bannedPlayer`, `bannedBy`, `reason`, `nameWhenBanned`, `server`) VALUES(?,?,?,?,?)");
                                                second.setString(1, uuid.toString());
                                                second.setString(2, p.getUniqueId().toString());
                                                second.setString(3, reason.nicename + " [A]");
                                                second.setString(4, playerName);
                                                second.setString(5, server);
                                                second.execute();
                                                second.close();
                                            } catch(Exception e){
                                                e.printStackTrace();
                                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "An error occured."));
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
                                                                    ChatColor.YELLOW + u2.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + u2.getTranslatedMessage("No reason has been specified.") + "\n\n" +
                                                                    ChatColor.GRAY + "Appeal at " + ChatColor.AQUA.toString() + ChatColor.UNDERLINE.toString() + "https://go.thechest.eu/appeal"
                                                    ));
                                                } else {
                                                    p2.disconnect(TextComponent.fromLegacyText(
                                                            ChatColor.GOLD.toString() + ChatColor.BOLD + "TheChest.eu" + "\n" +
                                                                    ChatColor.RED + u2.getTranslatedMessage("You were banned from this server.") + "\n" +
                                                                    "\n" +
                                                                    ChatColor.YELLOW + u2.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + reason + "\n\n" +
                                                                    ChatColor.GRAY + "Appeal at " + ChatColor.AQUA.toString() + ChatColor.UNDERLINE.toString() + "https://go.thechest.eu/appeal"
                                                    ));
                                                }
                                            }

                                            if(reason == null){
                                                for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                                    BungeeUser a = BungeeUser.getUser(all);
                                                    all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p banned %u").replace("%p",ChatColor.DARK_PURPLE + "BungeeConsole" + ChatColor.YELLOW).replace("%u",rank.getColor() + playerName + ChatColor.YELLOW)));
                                                }
                                            } else {
                                                for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                                    BungeeUser a = BungeeUser.getUser(all);
                                                    all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p banned %u with reason %r.").replace("%p",ChatColor.DARK_PURPLE + "BungeeConsole" + ChatColor.YELLOW).replace("%u",rank.getColor() + playerName + ChatColor.YELLOW).replace("%r",ChatColor.WHITE + reason.nicename + ChatColor.YELLOW)));
                                                }
                                            }
                                        } else {
                                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "That player is already banned."));
                                        }
                                    } else {
                                        String timeString = reason.duration.split(" ")[0];
                                        String timeUnit = reason.duration.split(" ")[1];

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
                                                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "bans.invalidTimeUnit"));
                                                    return;
                                                }

                                                Timestamp endDate = new Timestamp(cal.getTime().getTime());

                                                try {
                                                    PreparedStatement second = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `sn_bans` (`bannedPlayer`, `bannedBy`, `reason`, `nameWhenBanned`, `server`, `time`, `expiry`) VALUES(?,?,?,?,?,?,?)");
                                                    second.setString(1, uuid.toString());
                                                    second.setString(2, p.getUniqueId().toString());
                                                    second.setString(3, reason.nicename + " [A]");
                                                    second.setString(4, playerName);
                                                    second.setString(5, server);
                                                    second.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                                                    second.setTimestamp(7, endDate);
                                                    second.execute();
                                                    second.close();
                                                } catch(Exception e){
                                                    e.printStackTrace();
                                                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "An error occured."));
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
                                                        all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p banned %u for %t.").replace("%p",ChatColor.DARK_PURPLE + "BungeeConsole" + ChatColor.YELLOW).replace("%u",rank.getColor() + playerName + ChatColor.YELLOW).replace("%t",ChatColor.WHITE.toString() + time + timeUnit + ChatColor.YELLOW)));
                                                    }
                                                } else {
                                                    for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                                        BungeeUser a = BungeeUser.getUser(all);
                                                        all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p banned %u with reason %r for %t.").replace("%p",ChatColor.DARK_PURPLE + "BungeeConsole" + ChatColor.YELLOW).replace("%u",rank.getColor() + playerName + ChatColor.YELLOW).replace("%r",ChatColor.WHITE + reason.nicename + ChatColor.YELLOW).replace("%t",ChatColor.WHITE.toString() + time + timeUnit + ChatColor.YELLOW)));
                                                    }
                                                }
                                            } else {
                                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "That player is already banned."));
                                            }
                                        } else {
                                            sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "Please enter a valid number."));
                                        }
                                    }
                                } else if(reason.punishment == Punishment.MUTE){
                                    if(reason.duration == null || reason.duration.isEmpty() || reason.duration.equalsIgnoreCase("permanent")){
                                        if(!BanUtilities.isMuted(uuid)){
                                            try {
                                                PreparedStatement second = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `sn_mutes` (`mutedPlayer`, `mutedBy`, `reason`, `nameWhenMuted`, `server`, `time`, `expiry`) VALUES(?,?,?,?,?,?,?)");
                                                second.setString(1, uuid.toString());
                                                second.setString(2, p.getUniqueId().toString());
                                                second.setString(3, reason.nicename + " [A]");
                                                second.setString(4, playerName);
                                                second.setString(5, server);
                                                second.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                                                second.setTimestamp(7, null);
                                                second.execute();
                                                second.close();
                                            } catch(Exception e){
                                                e.printStackTrace();
                                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "An error occured."));
                                                return;
                                            }

                                            if(BungeeChest.getInstance().getProxy().getPlayer(playerName) != null){
                                                ProxiedPlayer p2 = BungeeChest.getInstance().getProxy().getPlayer(playerName);
                                                BungeeUser u2 = BungeeUser.getUser(p2);

                                                if(reason == null){
                                                    p2.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u2.getTranslatedMessage("You were muted.")));
                                                    p2.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u2.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + u2.getTranslatedMessage("No reason has been specified.")));
                                                } else {
                                                    p2.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u2.getTranslatedMessage("You were muted.")));
                                                    p2.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u2.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + reason.nicename));
                                                }
                                            }

                                            if(reason == null){
                                                for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                                    BungeeUser a = BungeeUser.getUser(all);
                                                    all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p muted %u").replace("%p",ChatColor.DARK_PURPLE + "BungeeConsole" + ChatColor.YELLOW).replace("%u",rank.getColor() + playerName + ChatColor.YELLOW)));
                                                }
                                            } else {
                                                for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                                    BungeeUser a = BungeeUser.getUser(all);
                                                    all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p muted %u with reason %r.").replace("%p",ChatColor.DARK_PURPLE + "BungeeConsole" + ChatColor.YELLOW).replace("%u",rank.getColor() + playerName + ChatColor.YELLOW).replace("%r",ChatColor.WHITE + reason.nicename + ChatColor.YELLOW)));
                                                }
                                            }
                                        } else {
                                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "That player is already muted."));
                                        }
                                    } else {
                                        String timeString = reason.duration.split(" ")[0];
                                        String timeUnit = reason.duration.split(" ")[1];

                                        if(BungeeChest.getInstance().isValidInteger(timeString) && Integer.parseInt(timeString) > 0){
                                            if(!BanUtilities.isMuted(uuid)){
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
                                                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "bans.invalidTimeUnit"));
                                                    return;
                                                }

                                                Timestamp endDate = new Timestamp(cal.getTime().getTime());

                                                try {
                                                    PreparedStatement second = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `sn_mutes` (`mutedPlayer`, `mutedBy`, `reason`, `nameWhenMuted`, `server`, `time`, `expiry`) VALUES(?,?,?,?,?,?,?)");
                                                    second.setString(1, uuid.toString());
                                                    second.setString(2, p.getUniqueId().toString());
                                                    second.setString(3, reason.nicename + " [A]");
                                                    second.setString(4, playerName);
                                                    second.setString(5, server);
                                                    second.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                                                    second.setTimestamp(7, endDate);
                                                    second.execute();
                                                    second.close();
                                                } catch(Exception e){
                                                    e.printStackTrace();
                                                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "An error occured"));
                                                    return;
                                                }

                                                if(BungeeChest.getInstance().getProxy().getPlayer(playerName) != null){
                                                    ProxiedPlayer p2 = BungeeChest.getInstance().getProxy().getPlayer(playerName);
                                                    BungeeUser u2 = BungeeUser.getUser(p2);

                                                    if(reason == null){
                                                        p2.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u2.getTranslatedMessage("You were muted.")));
                                                        p2.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u2.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + u2.getTranslatedMessage("No reason has been specified.")));
                                                        p2.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u2.getTranslatedMessage("Expiry") + ": " + ChatColor.WHITE + endDate.toGMTString()));
                                                    } else {
                                                        p2.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + u2.getTranslatedMessage("You were muted.")));
                                                        p2.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u2.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + reason));
                                                        p2.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.YELLOW + u2.getTranslatedMessage("Expiry") + ": " + ChatColor.WHITE + endDate.toGMTString()));
                                                    }
                                                }

                                                if(reason == null){
                                                    for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                                        BungeeUser a = BungeeUser.getUser(all);
                                                        all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p muted %u for %t.").replace("%p",ChatColor.DARK_PURPLE + "BungeeConsole" + ChatColor.YELLOW).replace("%u",rank.getColor() + playerName + ChatColor.YELLOW).replace("%t",ChatColor.WHITE.toString() + time + timeUnit + ChatColor.YELLOW)));
                                                    }
                                                } else {
                                                    for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                                        BungeeUser a = BungeeUser.getUser(all);
                                                        all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p muted %u with reason %r for %t.").replace("%p",ChatColor.DARK_PURPLE + "BungeeConsole" + ChatColor.YELLOW).replace("%u",rank.getColor() + playerName + ChatColor.YELLOW).replace("%r",ChatColor.WHITE + reason.nicename + ChatColor.YELLOW).replace("%t",ChatColor.WHITE.toString() + time + timeUnit + ChatColor.YELLOW)));
                                                    }
                                                }
                                            } else {
                                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "That player is already muted."));
                                            }
                                        } else {
                                            sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "Please enter a valid number."));
                                        }
                                    }
                                } else if(reason.punishment == Punishment.KICK){
                                    ProxiedPlayer p2 = BungeeChest.getInstance().getProxy().getPlayer(victim);
                                    if(p2 == null){
                                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + ChatColor.RED + "That player is not online."));

                                        return;
                                    }

                                    BungeeUser u2 = BungeeUser.getUser(p2);

                                    try {
                                        PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `sn_kicks` (`kickedPlayer`,`nameWhenKicked`,`reason`,`server`,`kickedBy`) VALUES(?,?,?,?,?)");
                                        ps.setString(1,p2.getUniqueId().toString());
                                        ps.setString(2,p2.getName());
                                        ps.setString(3,reason.nicename + " [A]");
                                        ps.setString(4,p2.getServer().getInfo().getName());
                                        ps.setString(5,p.getUniqueId().toString());
                                        ps.execute();
                                        ps.close();

                                        if(reason == null){
                                            p2.disconnect(
                                                    TextComponent.fromLegacyText(
                                                            ChatColor.GOLD.toString() + ChatColor.BOLD + "TheChest.eu" + "\n" +
                                                                    ChatColor.RED + u2.getTranslatedMessage("You were kicked.") + "\n" +
                                                                    "\n" +
                                                                    ChatColor.YELLOW + u2.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + u2.getTranslatedMessage("No reason has been specified.")
                                                    )
                                            );

                                            for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                                BungeeUser a = BungeeUser.getUser(all);
                                                all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p kicked %u").replace("%p",ChatColor.DARK_PURPLE + "BungeeConsole" + ChatColor.YELLOW).replace("%u",u2.getRank().getColor() + p2.getName() + ChatColor.YELLOW)));
                                            }
                                        } else {
                                            p2.disconnect(
                                                    TextComponent.fromLegacyText(
                                                            ChatColor.GOLD.toString() + ChatColor.BOLD + "TheChest.eu" + "\n" +
                                                                    ChatColor.RED + u2.getTranslatedMessage("You were kicked.") + "\n" +
                                                                    "\n" +
                                                                    ChatColor.YELLOW + u2.getTranslatedMessage("Reason") + ": " + ChatColor.WHITE + reason
                                                    )
                                            );

                                            for(ProxiedPlayer all : PlayerUtilities.getOnlineStaff(Rank.MOD)){
                                                BungeeUser a = BungeeUser.getUser(all);
                                                all.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_RED + "[BANS] " + a.getTranslatedMessage("%p kicked %u with reason: %r.").replace("%p",ChatColor.DARK_PURPLE + "BungeeConsole" + ChatColor.YELLOW).replace("%u",u2.getRank().getColor() + p2.getName() + ChatColor.YELLOW).replace("%r",ChatColor.WHITE + reason.nicename + ChatColor.YELLOW)));
                                            }
                                        }
                                    } catch (Exception e){
                                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "An error occured."));
                                        e.printStackTrace();
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }
    
    public enum Punishment {
        BAN,KICK,MUTE
    }
    
    public enum BanReason {
        BANNEDMODS(Punishment.BAN,"2 mon","Banned Modifications"),
        TEAMING(Punishment.BAN,"2 w","Teaming"),
        EXCEEDINGTEAM(Punishment.BAN,"2 w","Exceeding Team Limit"),
        COMPROMISED(Punishment.BAN,null,"Compromised Account"),
        MULTIACCOUNTING(Punishment.BAN,null,"Multiaccounting"),
        INAPPROPRIATENAME(Punishment.BAN,null,"Inappropriate Name"),
        INAPPROPRIATESKIN(Punishment.BAN,null,"Inappropriate Skin/Cape"),
        SCAM(Punishment.BAN,null,"Scamming/Phishing"),
        TEAMGRIEFING(Punishment.BAN,"1 w","Team Griefing"),
        INAPPROPRIATEBUILDING(Punishment.BAN,"3 w","Inappropriate Building"),
        EXPLOITING(Punishment.BAN,"3 mon","Exploiting"),
        KILLAURA(Punishment.BAN,null,"Hacking (Killaura)"),
        AIMBOT(Punishment.BAN,null,"Hacking (Aimbot)"),
        XRAY(Punishment.BAN,null,"Hacking (X-Ray)"),
        SPEED(Punishment.BAN,null,"Hacking (Speed)"),
        NOSLOWDOWN(Punishment.BAN,null,"Hacking (NoSlowdown)"),
        WATERWALKING(Punishment.BAN,null,"Hacking (Water Walking)"),
        NOSWING(Punishment.BAN,null,"Hacking (NoSwing)"),
        FLY(Punishment.BAN,null,"Hacking (Fly)"),
        OTHERHACKS(Punishment.BAN,null,"Hacking"),
        FOULLANGUAGE(Punishment.MUTE,"6 h","Foul Language"),
        DISRESPECTFUL(Punishment.MUTE,"12 h","Disrespectful Behavior"),
        NEGATIVE(Punishment.MUTE,"2 w","Negative Behavior"),
        ADVERTISING(Punishment.MUTE,"1 mon","Advertising"),
        VERBALABUSE(Punishment.MUTE,"1 w","Verbal Abuse"),
        SPAM(Punishment.MUTE,"3 d","Spam"),
        DISCOURAGEMENT(Punishment.MUTE,"1 mon","Player Discouragement"),
        HACKUSATION(Punishment.MUTE,"1 h","Hackusation"),
        OTHERCHAT(Punishment.MUTE,null,"Bad Chat Behavior");
        
        Punishment punishment;
        String duration;
        String nicename;
        
        BanReason(Punishment punishment, String duration, String nicename){
            this.punishment = punishment;
            this.duration = duration;
            this.nicename = nicename;
        }

        // leave duration for permanent punishment or kicks
        BanReason(Punishment punishment, String nicename){
            this.punishment = punishment;
            this.duration = null;
            this.nicename = nicename;
        }
    }
}
