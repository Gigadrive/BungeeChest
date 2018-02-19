package eu.thechest.bungeechest;

import com.google.common.io.ByteStreams;
import de.dytanic.cloudnet.CloudNetwork;
import de.dytanic.cloudnet.api.CloudNetAPI;
import de.dytanic.cloudnet.bukkitproxy.api.CloudProxy;
import de.dytanic.cloudnet.network.CNSInfo;
import eu.thechest.bungeechest.cmd.*;
import eu.thechest.bungeechest.crews.Crew;
import eu.thechest.bungeechest.mysql.MySQLManager;
import eu.thechest.bungeechest.party.Party;
import eu.thechest.bungeechest.user.BungeeUser;
import eu.thechest.bungeechest.user.Rank;
import eu.thechest.bungeechest.util.TaskUtils;
import eu.thechest.bungeechest.util.VPNCheckUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.net.InetSocketAddress;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by zeryt on 18.02.2017.
 */
public class BungeeChest extends Plugin {
    private static BungeeChest instance;
    private Configuration config;
    public static final ArrayList<String> BADWORDS = new ArrayList<String>();
    public static final String PREFIX = ChatColor.GOLD + "[TheChestEU] " + ChatColor.GREEN;
    public static final String STAFFNOTIFY_PREFIX = ChatColor.GRAY + "[" + ChatColor.RED + "StaffNotify" + ChatColor.GRAY + "] " + ChatColor.YELLOW;
    public static final String LINE_SEPERATOR = ChatColor.STRIKETHROUGH.toString() + "----------------------------------------------------";
    public static ArrayList<String> ONSERV = new ArrayList<String>();
    public static ExecutorService EXECUTOR;

    public void onEnable(){
        instance = this;
        saveDefaultConfig();

        try {
            reloadConfig();
        } catch(Exception e){
            e.printStackTrace();
        }

        EXECUTOR = Executors.newFixedThreadPool(1);

        // REGISTER LISTENERS
        getProxy().getPluginManager().registerListener(this, new MainListener());

        // REGISTER COMMANDS
        getProxy().getPluginManager().registerCommand(this,new Friend());
        getProxy().getPluginManager().registerCommand(this,new Ping());
        getProxy().getPluginManager().registerCommand(this,new RankCMD());
        //getProxy().getPluginManager().registerCommand(this,new BanCMD());
        //getProxy().getPluginManager().registerCommand(this,new MuteCMD());
        //getProxy().getPluginManager().registerCommand(this,new TempBanCMD());
        //getProxy().getPluginManager().registerCommand(this,new TempMuteCMD());
        getProxy().getPluginManager().registerCommand(this,new UnbanCMD());
        //getProxy().getPluginManager().registerCommand(this,new KickCMD());
        getProxy().getPluginManager().registerCommand(this,new UnmuteCMD());
        getProxy().getPluginManager().registerCommand(this,new HubCMD());
        getProxy().getPluginManager().registerCommand(this,new HelpCMD());
        getProxy().getPluginManager().registerCommand(this,new GiveResetTokensCMD());
        getProxy().getPluginManager().registerCommand(this,new GiveCoinsCMD());
        getProxy().getPluginManager().registerCommand(this,new GiveKeysCMD());
        getProxy().getPluginManager().registerCommand(this,new GiveItemCMD());
        getProxy().getPluginManager().registerCommand(this,new LogBuycraftActionCMD());
        getProxy().getPluginManager().registerCommand(this,new TSVerifyCMD());
        getProxy().getPluginManager().registerCommand(this,new StaffChat());
        getProxy().getPluginManager().registerCommand(this,new WebInterfaceCMD());
        getProxy().getPluginManager().registerCommand(this,new PrivateMSG());
        getProxy().getPluginManager().registerCommand(this,new ReplyCMD());
        getProxy().getPluginManager().registerCommand(this,new BroadcastCMD());
        //getProxy().getPluginManager().registerCommand(this,new GiveFameTitleCMD());
        getProxy().getPluginManager().registerCommand(this,new PartyCMD());
        getProxy().getPluginManager().registerCommand(this,new CrewCMD());
        getProxy().getPluginManager().registerCommand(this,new CrewChatCMD());
        //getProxy().getPluginManager().registerCommand(this,new ChatLogCMD());
        getProxy().getPluginManager().registerCommand(this,new DiscordCMD());
        getProxy().getPluginManager().registerCommand(this,new PartyChatCMD());
        getProxy().getPluginManager().registerCommand(this,new ReloadCachesCMD());
        getProxy().getPluginManager().registerCommand(this,new UUIDCMD());
        getProxy().getPluginManager().registerCommand(this,new ReportCMD());
        getProxy().getPluginManager().registerCommand(this,new ChatChannelCMD());
        getProxy().getPluginManager().registerCommand(this,new HandlePunishmentCMD());
        getProxy().getPluginManager().registerCommand(this,new BlockCommands());
        getProxy().getPluginManager().registerCommand(this,new SendCMD());
        getProxy().getPluginManager().registerCommand(this,new ServerCMD());
        getProxy().getPluginManager().registerCommand(this,new QueueManager());

        // REGISTER SERVERS
        //reloadServers();

        // REGISTER PLUGIN MESSAGING CHANNELS
        getProxy().registerChannel("WDL|INIT");
        getProxy().registerChannel("PERMISSIONREPL");
        getProxy().registerChannel("TheChest");
        getProxy().registerChannel("BungeeCord");

        // REGISTER BADWORDS
        reloadBadwords();

        // REGISTER RELOAD TASKS
        TaskUtils.init();
        VPNCheckUtil.init();

        // REGISTER TEAMSPEAK BOT
        TeamSpeakManager.getInstance().connect();
        TeamSpeakManager.getInstance().startListeners();

        // REGISTER QUEUE
        QueueManager.registerQueue("SGDuels");
        QueueManager.registerQueue("SoccerMC1");
        QueueManager.registerQueue("SoccerMC2",2);
        QueueManager.registerQueue("SoccerMC3",3);
        QueueManager.registerQueue("SoccerMC4",4);

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `parties` SET `status` = ?");
            ps.setString(1,"DISBANDED");
            ps.executeUpdate();
            ps.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void async(Runnable runnable){
        BungeeChest.getInstance().getProxy().getScheduler().runAsync(BungeeChest.getInstance(),runnable);
    }

    public static void logMessage(ProxiedPlayer player, String message){
        logMessage(player,message,false);
    }

    public static void logMessage(ProxiedPlayer player, String message, boolean filtered){
        async(() -> {
            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `chatMessages` (`uuid`,`message`,`server`,`filtered`) VALUES(?,?,?,?);");
                ps.setString(1,player.getUniqueId().toString());
                ps.setString(2,message);
                ps.setString(3,player.getServer().getInfo().getName());
                ps.setBoolean(4,filtered);
                ps.executeUpdate();
                ps.close();
            } catch(Exception e){
                e.printStackTrace();
            }
        });
    }

    public static void updatePeak(){
        BungeeChest.async(() -> {
            int playerCount = getInstance().getProxy().getOnlineCount();
            int currentPeak = 0;
            boolean doInsert = false;

            Timestamp today = new Timestamp(System.currentTimeMillis());

            try {
                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `playerCountPeaks` WHERE `date` = ?");
                ps.setDate(1,new Date(today.getTime()));
                ResultSet rs = ps.executeQuery();

                if(rs.first()){
                    currentPeak = rs.getInt("count");
                } else {
                    doInsert = true;
                }

                MySQLManager.getInstance().closeResources(rs,ps);

                if(playerCount > currentPeak){
                    if(doInsert){
                        PreparedStatement insert = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `playerCountPeaks` (`date`,`count`,`time_reached`) VALUES(?,?,CURRENT_TIMESTAMP);");
                        insert.setDate(1,new Date(today.getTime()));
                        insert.setInt(2,playerCount);
                        insert.executeUpdate();
                        insert.close();
                    } else {
                        PreparedStatement update = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `playerCountPeaks` SET `count` = ?, `time_reached` = CURRENT_TIMESTAMP WHERE `date` = ?");
                        update.setInt(1,playerCount);
                        update.setDate(2,new Date(today.getTime()));
                        update.executeUpdate();
                        update.close();
                    }
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        });
    }

    public void onDisable(){
        for(Crew c : Crew.STORAGE.values()){
            c.saveData();
        }

        for(Party p : Party.STORAGE){
            p.saveData();
            p.disband();
        }

        MySQLManager.getInstance().unload();
        EXECUTOR.shutdown();
    }

    public static BungeeChest getInstance(){
        return instance;
    }

    public Configuration getConfig(){
        return this.config;
    }

    public void reloadConfig() throws IOException {
        this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        MySQLManager.getInstance().loadDataFromConfig();
    }

    public void saveConfig(){
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(getConfig(), new File(getDataFolder() + "config.yml"));
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void saveDefaultConfig(){
        if(!getDataFolder().exists()){
            getDataFolder().mkdir();
        }

        File configFile = new File(getDataFolder(), "config.yml");
        if(!configFile.exists()){
            try {
                configFile.createNewFile();
                try (InputStream is = getResourceAsStream("config.yml");
                     OutputStream os = new FileOutputStream(configFile)) {
                    ByteStreams.copy(is, os);
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void executeConsoleCommand(String cmd){
        BungeeChest.getInstance().getProxy().getPluginManager().dispatchCommand(BungeeChest.getInstance().getProxy().getConsole(),cmd);
    }

    public static String executeShellCommand(String command) {

        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output.toString();

    }

    public static int randomInteger(int min, int max){
        Random rdm = new Random();
        int rdmNm = rdm.nextInt((max - min) + 1) + min;

        return rdmNm;
    }

    public static String limitString(String s, int limit){
        if(s.length() > limit){
            return s.substring(0,limit-1);
        } else {
            return s;
        }
    }

    @Deprecated
    public void reloadServers(){
        ArrayList<String> s = new ArrayList<String>();

        for(ServerInfo i : ProxyServer.getInstance().getServers().values()){
            if(!i.getName().equals("LOBBY01")) s.add(i.getName().toLowerCase());
        }

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `servers`");
            ResultSet rs = ps.executeQuery();

            rs.beforeFirst();
            while(rs.next()){
                String name = rs.getString("name");
                String ip = rs.getString("ip");
                int port = rs.getInt("port");

                if(getProxy().getServerInfo(name) == null){
                    ServerInfo serverInfo = ProxyServer.getInstance().constructServerInfo(name, new InetSocketAddress(ip, port), "CHEST-"+ name, false);
                    ProxyServer.getInstance().getServers().put(name, serverInfo);

                    if(s.contains(name.toLowerCase())) s.remove(name.toLowerCase());
                }
            }

            ps.close();
            rs.close();

            /*if(s.size() > 0){
                for(String server : s){
                    if(ProxyServer.getInstance().getServerInfo(server) != null){
                        String actualName = ProxyServer.getInstance().getServerInfo(server).getName();

                        if(ProxyServer.getInstance().getServerInfo(server).getPlayers().size() > 0){
                            for(ProxiedPlayer all : ProxyServer.getInstance().getServerInfo(server).getPlayers()){
                                ServerInfo bestLobby = getBestLobby();

                                if(!bestLobby.getName().equalsIgnoreCase(server)){
                                    all.connect(bestLobby);
                                } else {
                                    all.disconnect(TextComponent.fromLegacyText(ChatColor.RED + BungeeUser.getUser(all).getTranslatedMessage("An error occured.")));
                                }
                            }

                            BungeeChest.getInstance().getProxy().getScheduler().schedule(BungeeChest.getInstance(), new Runnable(){
                                @Override
                                public void run(){
                                    ProxyServer.getInstance().getServers().remove(actualName);
                                    executeShellCommand("screen -X -S " + actualName + " kill"); // TODO: stop server before killing screen
                                }
                            }, 5, TimeUnit.SECONDS);
                        }
                    }
                }
            }*/
        } catch(Exception e){
            e.printStackTrace();
            System.err.println("(IMPORTANT) Failed to load servers from database!!");
        }
    }

    public void reloadBadwords(){
        BADWORDS.clear();

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `badwords`");
            ResultSet rs = ps.executeQuery();

            rs.beforeFirst();
            while(rs.next()){
                String word = rs.getString("word");

                if(word != null){
                    if(!BADWORDS.contains(word)){
                        BADWORDS.add(word);
                    }
                }
            }

            rs.close();
            ps.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public boolean isValidInteger(String s){
        try {
            Integer.parseInt(s);
            return true;
        } catch(Exception e){
            return false;
        }
    }

    public boolean convertIntegerToBoolean(int i){
        if(i == 0){
            return false;
        } else {
            return true;
        }
    }

    public int convertBooleanToInteger(boolean b){
        if(b){
            return 1;
        } else {
            return 0;
        }
    }

    public ServerInfo getBestLobby(ProxiedPlayer p){
        return getBestLobby(p,false);
    }

    public ServerInfo getBestLobby(ProxiedPlayer p, boolean ignoreRank){
        BungeeUser u = BungeeUser.getUser(p);
        ArrayList<de.dytanic.cloudnet.network.ServerInfo> a = new ArrayList<de.dytanic.cloudnet.network.ServerInfo>();

        if(ignoreRank || u.hasPermission(Rank.PRO)){
            for(de.dytanic.cloudnet.network.ServerInfo info : CloudNetAPI.getInstance().getCloudNetwork().getServers().values()){
                if(info.getGroup().equalsIgnoreCase("PremiumLobby") && info.getMaxPlayers() > info.getOnlineCount()){
                    a.add(info);
                }
            }

            if(a.size() == 0){
                return getBestLobby(p,true);
            } else if(a.size() == 1){
                return ProxyServer.getInstance().getServerInfo(a.get(0).getName());
            } else {
                Collections.sort(a, new Comparator<de.dytanic.cloudnet.network.ServerInfo>() {
                    public int compare(de.dytanic.cloudnet.network.ServerInfo p1, de.dytanic.cloudnet.network.ServerInfo p2) {
                        return ((Integer)p1.getOnlineCount()).compareTo(((Integer)p2.getOnlineCount()));
                    }
                });

                return ProxyServer.getInstance().getServerInfo(a.get(0).getName());
            }
        } else {
            de.dytanic.cloudnet.network.ServerInfo fallback = CloudProxy.getInstance().calcFallback(CloudNetAPI.getInstance().getOnlinePlayer(p.getUniqueId()));
            if(fallback != null){
                return ProxyServer.getInstance().getServerInfo(fallback.getName());
            } else {
                return null;
            }
        }
    }

    public static de.dytanic.cloudnet.network.ServerInfo getBestServer(String group){
        return getBestServer(group,0);
    }

    public static de.dytanic.cloudnet.network.ServerInfo getBestServer(String group, int minFreeSlots){
        ArrayList<de.dytanic.cloudnet.network.ServerInfo> potentials = new ArrayList<de.dytanic.cloudnet.network.ServerInfo>();

        for(de.dytanic.cloudnet.network.ServerInfo info : CloudNetAPI.getInstance().getCloudNetwork().getServers().values()){
            int freeSlots = info.getMaxPlayers()-info.getOnlineCount();

            if(info.getGroup().equalsIgnoreCase(group) && freeSlots >= minFreeSlots){
                potentials.add(info);
            }
        }

        if(potentials.size() == 0){
            return null;
        } else if(potentials.size() == 1){
            return potentials.get(0);
        } else {
            Collections.sort(potentials, new Comparator<de.dytanic.cloudnet.network.ServerInfo>() {
                @Override
                public int compare(de.dytanic.cloudnet.network.ServerInfo o1, de.dytanic.cloudnet.network.ServerInfo o2) {
                    return ((Integer)o1.getOnlineCount()).compareTo(((Integer)o2.getOnlineCount()));
                }
            });

            return potentials.get(0);
        }
    }
}
