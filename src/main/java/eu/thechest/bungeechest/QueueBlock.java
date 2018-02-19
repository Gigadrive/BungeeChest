package eu.thechest.bungeechest;

import eu.thechest.bungeechest.party.Party;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;

public class QueueBlock {
    public ArrayList<ProxiedPlayer> p;
    public Party party;

    public QueueBlock(ProxiedPlayer p){
        this.p = new ArrayList<ProxiedPlayer>();
        this.p.add(p);
    }

    public QueueBlock(Party p){
        this.p = new ArrayList<ProxiedPlayer>();
        this.p.addAll(p.getMembers());
        this.party = p;
        if(!this.p.contains(p.getOwner())) this.p.add(p.getOwner());
    }
}
