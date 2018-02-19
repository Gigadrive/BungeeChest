package eu.thechest.bungeechest.cmd;

import eu.thechest.bungeechest.BungeeChest;
import eu.thechest.bungeechest.Queue;
import eu.thechest.bungeechest.QueueBlock;
import eu.thechest.bungeechest.party.Party;
import eu.thechest.bungeechest.user.BungeeUser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;

public class QueueManager extends Command {
    private static ArrayList<Queue> STORAGE = new ArrayList<Queue>();

    public static ArrayList<Queue> getQueues(){
        return STORAGE;
    }

    public static void registerQueue(String name){
        if(getQueue(name) == null) STORAGE.add(new Queue(name));
    }

    public static void registerQueue(String name,int partyLimit){
        if(getQueue(name) == null) STORAGE.add(new Queue(name,partyLimit));
    }

    public static Queue getQueue(String name){
        for(Queue q : getQueues()) if(q.getName().equalsIgnoreCase(name)) return q;
        return null;
    }

    public QueueManager(){
        super("queuemanager");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof ProxiedPlayer)){
            if(args.length == 3){
                String action = args[0];
                String queueName = args[1];
                ProxiedPlayer p = BungeeChest.getInstance().getProxy().getPlayer(args[2]);

                if(p != null){
                    BungeeUser u = BungeeUser.getUser(p);

                    if(u != null){
                        if(action.equalsIgnoreCase("join")){
                            if(Party.getParty(p) == null || (Party.getParty(p).getOwner().equals(p))){
                                Queue q = getQueue(queueName);

                                if(q != null){
                                    if(u.getQueue() == null){
                                        if((Party.getParty(p) == null) || (Party.getParty(p) != null && Party.getParty(p).getMembers().size() <= q.getPartyLimit())){
                                            if(Party.getParty(p) == null || Party.getParty(p).getMembers().size() == 1){
                                                q.getBlocks().add(new QueueBlock(p));
                                                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + u.getTranslatedMessage("You have joined the queue.")));
                                            } else {
                                                q.getBlocks().add(new QueueBlock(Party.getParty(p)));

                                                for(ProxiedPlayer pp : Party.getParty(p).getMembers()){
                                                    BungeeUser up = BungeeUser.getUser(pp);
                                                    pp.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.GREEN + up.getTranslatedMessage("You have joined the queue.")));
                                                }
                                            }
                                        } else {
                                            p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Your party does not fit into this queue (max %d players).",q.getPartyLimit())));
                                        }
                                    } else {
                                        p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("You are already in a queue.")));
                                    }
                                }
                            } else {
                                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Only the party leader may leave the queue.")));
                            }
                        } else if(action.equalsIgnoreCase("leave")){
                            if(Party.getParty(p) == null || (Party.getParty(p).getOwner().equals(p))){
                                if(u.getQueue() != null){
                                    for(Queue q : getQueues()) if(q.equals(u.getQueue())) q.leaveQueue(p);
                                } else {
                                    p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("You are not in a queue.")));
                                }
                            } else {
                                p.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + u.getTranslatedMessage("Only the party leader may leave the queue.")));
                            }
                        }
                    }
                }
            }
        }
    }
}
