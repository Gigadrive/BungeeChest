package eu.thechest.bungeechest.cmd;

import eu.thechest.bungeechest.BungeeChest;
import eu.thechest.bungeechest.party.Party;
import eu.thechest.bungeechest.user.BungeeUser;
import eu.thechest.bungeechest.user.PlayerUtilities;
import eu.thechest.bungeechest.user.Rank;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * Created by zeryt on 19.03.2017.
 */
public class PartyCMD extends Command {
    public PartyCMD(){
        super("party");
    }

    private void sendUsage(ProxiedPlayer p){
        BungeeUser u = BungeeUser.getUser(p);
        p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.YELLOW + "/party create"));
        p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.YELLOW + "/party invite <" + u.getTranslatedMessage("Player") + ">"));
        p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.YELLOW + "/party accept <" + u.getTranslatedMessage("Player") + ">"));
        p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.YELLOW + "/party deny <" + u.getTranslatedMessage("Player") + ">"));
        p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.YELLOW + "/party kick <" + u.getTranslatedMessage("Player") + ">"));
        p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.YELLOW + "/party leave"));
        p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.YELLOW + "/party disband"));
        p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.YELLOW + "/party info"));
        p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.YELLOW + "/party warp"));
        p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.YELLOW + "/p <" + u.getTranslatedMessage("Message") + ">"));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer p = (ProxiedPlayer) sender;
            BungeeUser u = BungeeUser.getUser(p);

            if(args.length == 0){
                sendUsage(p);
            } else if(args.length == 1) {
                if(args[0].equalsIgnoreCase("create")){
                    if(!Party.hasParty(p)){
                        Party party = new Party(p);

                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.DARK_GREEN + u.getTranslatedMessage("The party has been created.")));
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.RED + u.getTranslatedMessage("You are already in a party.")));
                    }
                } else if(args[0].equalsIgnoreCase("leave")){
                    if(Party.hasParty(p)){
                        Party party = Party.getParty(p);
                        party.leaveParty(p);
                        party.saveData();
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.RED + u.getTranslatedMessage("You are not in a party.")));
                    }
                } else if(args[0].equalsIgnoreCase("disband")){
                    if(Party.hasParty(p)){
                        if(Party.getParty(p).getOwner() == p){
                            Party.getParty(p).disband();
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.RED + u.getTranslatedMessage("Only the party leader can do that.")));
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.RED + u.getTranslatedMessage("You are not in a party.")));
                    }
                } else if(args[0].equalsIgnoreCase("info")){
                    if(Party.hasParty(p)){
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + BungeeChest.LINE_SEPERATOR));
                        u.sendCenteredMessage(ChatColor.YELLOW + "Party");
                        p.sendMessage(TextComponent.fromLegacyText("   " + ChatColor.YELLOW + u.getTranslatedMessage("Leader") + ": " + BungeeUser.getUser(Party.getParty(p).getOwner()).getRank().getColor() + BungeeUser.getUser(Party.getParty(p).getOwner()).getProxiedPlayer().getName()));
                        if(Party.getParty(p).getPartyLimit() == Integer.MAX_VALUE){
                            p.sendMessage(TextComponent.fromLegacyText("   " + ChatColor.YELLOW + u.getTranslatedMessage("Members") + ": " + ChatColor.GOLD + "(" + Party.getParty(p).getMembers().size() + ")"));
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText("   " + ChatColor.YELLOW + u.getTranslatedMessage("Members") + ": " + ChatColor.GOLD + "(" + Party.getParty(p).getMembers().size() + "/" + Party.getParty(p).getPartyLimit() + ")"));
                        }
                        for(ProxiedPlayer member : Party.getParty(p).getMembers()){
                            BungeeUser memberU = BungeeUser.getUser(member);

                            if(Party.getParty(p).getOwner() == member){
                                p.sendMessage(TextComponent.fromLegacyText("      " + memberU.getRank().getColor() + member.getName() + "  " + ChatColor.RED + "[LEADER]"));
                            } else {
                                p.sendMessage(TextComponent.fromLegacyText("      " + memberU.getRank().getColor() + member.getName()));
                            }
                        }
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + BungeeChest.LINE_SEPERATOR));
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.RED + u.getTranslatedMessage("You are not in a party.")));
                    }
                } else if(args[0].equalsIgnoreCase("warp")){
                    if(Party.hasParty(p)){
                        if(Party.getParty(p).getOwner() == p){
                            for(ProxiedPlayer pp : Party.getParty(p).getMembers()){
                                if(!pp.getServer().getInfo().getName().equals(p.getServer().getInfo().getName())) pp.connect(p.getServer().getInfo());

                                pp.sendMessage(ChatColor.BLUE + "[PARTY] " + ChatColor.GREEN + BungeeUser.getUser(pp).getTranslatedMessage("The party leader has moved everyone to %s.").replace("%s",p.getServer().getInfo().getName().toUpperCase()));
                            }
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.RED + u.getTranslatedMessage("Only the party leader can do that.")));
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.RED + u.getTranslatedMessage("You are not in a party.")));
                    }
                } else {
                    sendUsage(p);
                }
            } else if(args.length == 2) {
                if(args[0].equalsIgnoreCase("invite")){
                    if(!Party.hasParty(p)){
                        BungeeChest.getInstance().getProxy().getPluginManager().dispatchCommand(p, "party create");
                    } else {
                        if(Party.getParty(p).getOwner() != p){
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.RED + u.getTranslatedMessage("Only the party leader can do that.")));
                            return;
                        }
                    }

                    if(BungeeChest.getInstance().getProxy().getPlayer(args[1]) != null){
                        ProxiedPlayer p2 = BungeeChest.getInstance().getProxy().getPlayer(args[1]);
                        if(PlayerUtilities.allowsPartyRequests(p2.getUniqueId()) || u.hasPermission(Rank.MOD) || u.getFriends().contains(p2.getUniqueId().toString())){
                            BungeeUser u2 = BungeeUser.getUser(p2);

                            if(!Party.hasParty(p2)){
                                Party.getParty(p).getRequestedPlayers().add(p2);

                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.GREEN + u.getTranslatedMessage("The party request has been sent.")));

                                p2.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.YELLOW + u2.getTranslatedMessage("You have been invited to %p's party.").replace("%p", u.getRank().getColor() + p.getName() + ChatColor.YELLOW)));
                                p2.sendMessage(new ComponentBuilder("[PARTY] ").color(ChatColor.BLUE).append("[" + u2.getTranslatedMessage("ACCEPT") + "]  ").color(ChatColor.DARK_GREEN).bold(true).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party accept " + p.getName())).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GREEN + u2.getTranslatedMessage("ACCEPT") + ""))).append("[" + u2.getTranslatedMessage("DENY") + "]").color(ChatColor.DARK_RED).bold(true).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party deny " + p.getName())).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.RED + u2.getTranslatedMessage("DENY") + ""))).create());
                            } else {
                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.RED + u.getTranslatedMessage("That player is already in a party.")));
                            }
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.RED + u.getTranslatedMessage("That player does not allow party requests.")));
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.RED + u.getTranslatedMessage("That player is not online.")));
                    }
                } else if(args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("join")){
                    if(!Party.hasParty(p)){
                        if(BungeeChest.getInstance().getProxy().getPlayer(args[1]) != null){
                            ProxiedPlayer p2 = BungeeChest.getInstance().getProxy().getPlayer(args[1]);

                            if(Party.hasParty(p2)){
                                if(Party.getParty(p2).getRequestedPlayers().contains(p)){
                                    Party.getParty(p2).getRequestedPlayers().remove(p);

                                    Party.getParty(p2).joinParty(p);
                                    Party.getParty(p2).saveData();
                                } else {
                                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.RED + u.getTranslatedMessage("You haven't been invited to that party.")));
                                }
                            } else {
                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.RED + u.getTranslatedMessage("That party could not be found.")));
                            }
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.RED + u.getTranslatedMessage("That player is not online.")));
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.RED + u.getTranslatedMessage("You are already in a party.")));
                    }
                } else if(args[0].equalsIgnoreCase("deny") ||args[0].equalsIgnoreCase("decline")){
                    if(!Party.hasParty(p)){
                        if(BungeeChest.getInstance().getProxy().getPlayer(args[1]) != null){
                            ProxiedPlayer p2 = BungeeChest.getInstance().getProxy().getPlayer(args[1]);

                            if(Party.hasParty(p2)){
                                if(Party.getParty(p2).getRequestedPlayers().contains(p)){
                                    Party.getParty(p2).getRequestedPlayers().remove(p);

                                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.RED + u.getTranslatedMessage("The request has been denied.")));
                                    Party.getParty(p2).getOwner().sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.RED + u.getTranslatedMessage("%p denied the party request.").replace("%p", u.getRank().getColor() + p.getName() + ChatColor.RED)));

                                    u.achieve(34);
                                } else {
                                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.RED + u.getTranslatedMessage("You haven't been invited to that party.")));
                                }
                            } else {
                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.RED + u.getTranslatedMessage("That party could not be found.")));
                            }
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.RED + u.getTranslatedMessage("That player is not online.")));
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.RED + u.getTranslatedMessage("You are already in a party.")));
                    }
                } else if(args[0].equalsIgnoreCase("kick")){
                    if(Party.hasParty(p)){
                        if(Party.getParty(p).getOwner() == p){
                            if(BungeeChest.getInstance().getProxy().getPlayer(args[1]) != null) {
                                ProxiedPlayer p2 = BungeeChest.getInstance().getProxy().getPlayer(args[1]);

                                if(Party.hasParty(p2) && Party.getParty(p2) == Party.getParty(p)) {
                                    for(ProxiedPlayer all : Party.getParty(p).getMembers()){
                                        all.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.RED + u.getTranslatedMessage("%p was kicked from the party.").replace("%p", BungeeUser.getUser(p2).getRank().getColor() + p2.getName() + ChatColor.RED)));
                                    }

                                    Party.getParty(p2).leaveParty(p2);
                                    Party.getParty(p).saveData();

                                    u.achieve(35);
                                } else {
                                    p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.RED + u.getTranslatedMessage("That player is not in your party.")));
                                }
                            } else {
                                p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.RED + u.getTranslatedMessage("That player is not online.")));
                            }
                        } else {
                            p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.RED + u.getTranslatedMessage("Only the party leader can do that.")));
                        }
                    } else {
                        p.sendMessage(TextComponent.fromLegacyText(ChatColor.BLUE + "[PARTY] " + ChatColor.RED + u.getTranslatedMessage("You are not in a party.")));
                    }
                } else {
                    sendUsage(p);
                }
            } else {
                sendUsage(p);
            }
        } else {
            sender.sendMessage(TextComponent.fromLegacyText(BungeeChest.PREFIX + ChatColor.RED + "You have to be a player in order to do this."));
        }
    }
}