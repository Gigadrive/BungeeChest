package eu.thechest.bungeechest;

import de.dytanic.cloudnet.network.ServerInfo;
import eu.thechest.bungeechest.mysql.MySQLManager;
import eu.thechest.bungeechest.party.Party;
import eu.thechest.bungeechest.user.BungeeUser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class Queue {
    private String name;
    private ArrayList<QueueBlock> blocks;
    private int partyLimit;

    public Queue(String name){
        this.name = name;
        this.blocks = new ArrayList<QueueBlock>();
        this.partyLimit = 1;

        doTask();
    }

    public Queue(String name,int partyLimit){
        this.name = name;
        this.blocks = new ArrayList<QueueBlock>();
        this.partyLimit = partyLimit;

        doTask();
    }

    public String getName() {
        return name;
    }

    public ArrayList<QueueBlock> getBlocks() {
        return blocks;
    }

    public int getPartyLimit() {
        return partyLimit;
    }

    public void doTask(){
        doTask(true);
    }

    public int getTotalBlockSize(){
        int i = 0;

        for(QueueBlock b : getBlocks()) i += b.p.size();

        return i;
    }

    public void doTask(boolean delayNewTask){
        BungeeChest.async(() -> {
            if(getBlocks().size() > 0){
                Collections.shuffle(getBlocks());

                if(name.equalsIgnoreCase("SGDuels")){
                    int teamSize = 1;

                    while(teamSize*2 <= getTotalBlockSize()){
                        ArrayList<ProxiedPlayer> team1 = new ArrayList<ProxiedPlayer>();
                        ArrayList<ProxiedPlayer> team2 = new ArrayList<ProxiedPlayer>();

                        ArrayList<QueueBlock> usedBlocks = new ArrayList<QueueBlock>();

                        for(QueueBlock b : getBlocks()){
                            if(!usedBlocks.contains(b)){
                                if((team1.size() <= teamSize) && (b.p.size() <= (teamSize-team1.size()))){
                                    usedBlocks.add(b);
                                    team1.addAll(b.p);
                                } else if((team2.size() <= teamSize) && (b.p.size() <= (teamSize-team2.size()))){
                                    usedBlocks.add(b);
                                    team2.addAll(b.p);
                                }
                            }
                        }

                        if(team1.size() == teamSize && team2.size() == teamSize){
                            try {
                                ServerInfo bestServer = BungeeChest.getBestServer("SGDuels",team1.size()+team2.size());
                                if(bestServer != null){
                                    String t1 = null;
                                    for(ProxiedPlayer p : team1){
                                        if(t1 != null){
                                            t1 += "," + p.getUniqueId().toString();
                                        } else {
                                            t1 = p.getUniqueId().toString();
                                        }
                                    }

                                    String t2 = null;
                                    for(ProxiedPlayer p : team2){
                                        if(t2 != null){
                                            t2 += "," + p.getUniqueId().toString();
                                        } else {
                                            t2 = p.getUniqueId().toString();
                                        }
                                    }

                                    PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `sgduels_upcomingGames` (`team1`,`team2`,`ranked`) VALUES(?,?,?);");
                                    ps.setString(1,t1);
                                    ps.setString(2,t2);
                                    ps.setBoolean(3,true);
                                    ps.executeUpdate();
                                    ps.close();

                                    for(ProxiedPlayer p : team1){
                                        leaveQueue(p,false);
                                        p.connect(ProxyServer.getInstance().getServerInfo(bestServer.getName()));
                                    }

                                    for(ProxiedPlayer p : team2){
                                        leaveQueue(p,false);
                                        p.connect(ProxyServer.getInstance().getServerInfo(bestServer.getName()));
                                    }
                                } else {
                                    System.err.println("QUEUE " + getName() + " WAS INTERRUPTED DUE TO NO GAME SERVER FOUND");

                                    for(ProxiedPlayer p : team1){
                                        BungeeUser u = BungeeUser.getUser(p);
                                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Could not find an appropriate game server!")));
                                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Please try again later.")));
                                    }

                                    for(ProxiedPlayer p : team2){
                                        BungeeUser u = BungeeUser.getUser(p);
                                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Could not find an appropriate game server!")));
                                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Please try again later.")));
                                    }

                                    break;
                                }
                            } catch(Exception e){
                                System.err.println("QUEUE " + getName() + " WAS INTERRUPTED DUE TO EXCEPTION");
                                e.printStackTrace();
                                break;
                            }
                        } else {
                            System.err.println("QUEUE " + getName() + " WAS INTERRUPTED DUE TO INVALID TEAM SIZE - (t1:" + team1.size() + " t2:" + team2.size() + ")");
                            break;
                        }
                    }
                } else if(name.startsWith("SoccerMC")) {
                    String c = name.replace("SoccerMC","");
                    if(BungeeChest.getInstance().isValidInteger(c)){
                        int teamSize = Integer.parseInt(c);

                        while(teamSize*2 <= getTotalBlockSize()){
                            ArrayList<ProxiedPlayer> team1 = new ArrayList<ProxiedPlayer>();
                            ArrayList<ProxiedPlayer> team2 = new ArrayList<ProxiedPlayer>();

                            ArrayList<QueueBlock> usedBlocks = new ArrayList<QueueBlock>();

                            for(QueueBlock b : getBlocks()){
                                if(!usedBlocks.contains(b)){
                                    if((team1.size() <= teamSize) && (b.p.size() <= (teamSize-team1.size()))){
                                        usedBlocks.add(b);
                                        team1.addAll(b.p);
                                    } else if((team2.size() <= teamSize) && (b.p.size() <= (teamSize-team2.size()))){
                                        usedBlocks.add(b);
                                        team2.addAll(b.p);
                                    }
                                }
                            }

                            if(team1.size() == teamSize && team2.size() == teamSize){
                                try {
                                    ServerInfo bestServer = BungeeChest.getBestServer("SoccerMC",team1.size()+team2.size());
                                    if(bestServer != null){
                                        String t1 = null;
                                        for(ProxiedPlayer p : team1){
                                            if(t1 != null){
                                                t1 += "," + p.getUniqueId().toString();
                                            } else {
                                                t1 = p.getUniqueId().toString();
                                            }
                                        }

                                        String t2 = null;
                                        for(ProxiedPlayer p : team2){
                                            if(t2 != null){
                                                t2 += "," + p.getUniqueId().toString();
                                            } else {
                                                t2 = p.getUniqueId().toString();
                                            }
                                        }

                                        PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `soccer_upcomingGames` (`team1`,`team2`,`ranked`) VALUES(?,?,?);");
                                        ps.setString(1,t1);
                                        ps.setString(2,t2);
                                        ps.setBoolean(3,true); // TODO: Remove ranked boolean as it is unused
                                        ps.executeUpdate();
                                        ps.close();

                                        for(ProxiedPlayer p : team1){
                                            leaveQueue(p,false);
                                            p.connect(ProxyServer.getInstance().getServerInfo(bestServer.getName()));
                                        }

                                        for(ProxiedPlayer p : team2){
                                            leaveQueue(p,false);
                                            p.connect(ProxyServer.getInstance().getServerInfo(bestServer.getName()));
                                        }
                                    } else {
                                        System.err.println("QUEUE " + getName() + " WAS INTERRUPTED DUE TO NO GAME SERVER FOUND");

                                        for(ProxiedPlayer p : team1){
                                            BungeeUser u = BungeeUser.getUser(p);
                                            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Could not find an appropriate game server!")));
                                            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Please try again later.")));
                                        }

                                        for(ProxiedPlayer p : team2){
                                            BungeeUser u = BungeeUser.getUser(p);
                                            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Could not find an appropriate game server!")));
                                            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Please try again later.")));
                                        }

                                        break;
                                    }
                                } catch(Exception e){
                                    System.err.println("QUEUE " + getName() + " WAS INTERRUPTED DUE TO EXCEPTION");
                                    e.printStackTrace();
                                    break;
                                }
                            } else {
                                System.err.println("QUEUE " + getName() + " WAS INTERRUPTED DUE TO INVALID TEAM SIZE - (t1:" + team1.size() + " t2:" + team2.size() + ")");
                                break;
                            }
                        }
                    }
                }
            }

            if(delayNewTask){
                BungeeChest.getInstance().getProxy().getScheduler().schedule(BungeeChest.getInstance(), new Runnable(){
                    @Override
                    public void run(){
                        doTask();
                    }
                }, 10, TimeUnit.SECONDS);
            }
        });
    }

    public void leaveQueue(ProxiedPlayer p){
        leaveQueue(p,true);
    }

    public void leaveQueue(ProxiedPlayer p,boolean sendMessage){
        if(Party.getParty(p) == null){
            BungeeUser u = BungeeUser.getUser(p);

            if(u.getQueue() != null && u.getQueue().equals(this)){
                QueueBlock toRemove = null;
                for(QueueBlock b : getBlocks()) if(b.p.contains(p)) toRemove = b;

                if(toRemove != null){
                    getBlocks().remove(toRemove);
                    if(sendMessage) p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.YELLOW + u.getTranslatedMessage("You have left the queue.")));
                }
            }
        } else {
            if(Party.getParty(p).getOwner().equals(p)) leaveQueue(Party.getParty(p));
        }
    }

    public void leaveQueue(Party party){
        leaveQueue(party,true);
    }

    public void leaveQueue(Party party,boolean sendMessage){
        if(party.getQueue() != null && party.getQueue().equals(this)){
            QueueBlock toRemove = null;
            for(QueueBlock b : getBlocks()) if(b.party.equals(party)) toRemove = b;

            if(toRemove != null){
                getBlocks().remove(toRemove);

                if(sendMessage){
                    for(ProxiedPlayer p : party.getMembers()){
                        BungeeUser u = BungeeUser.getUser(p);
                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.YELLOW + u.getTranslatedMessage("You have left the queue.")));
                    }
                }
            }
        }
    }
}
