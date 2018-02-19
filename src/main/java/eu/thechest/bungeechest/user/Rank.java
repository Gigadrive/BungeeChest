package eu.thechest.bungeechest.user;

import net.md_5.bungee.api.ChatColor;

/**
 * Created by zeryt on 18.02.2017.
 */
public enum Rank {
    USER(0,"User",null,ChatColor.GRAY),
    PRO(1,"Pro","Pro",ChatColor.GOLD),
    PRO_PLUS(2,"Pro+","Pro+",ChatColor.DARK_AQUA),
    TITAN(3,"Titan","Titan",ChatColor.AQUA),
    VIP(4,"VIP","VIP",ChatColor.DARK_PURPLE),
    STAFF(5,"Staff Member","Staff",ChatColor.YELLOW),
    BUILD_TEAM(6,"Build Team Member","Builder",ChatColor.BLUE),
    MOD(7,"Moderator","Mod",ChatColor.GREEN),
    SR_MOD(8,"Senior Moderator","SrMod",ChatColor.DARK_GREEN),
    CM(9,"Community Manager","CM",ChatColor.RED),
    ADMIN(10,"Admin","Admin",ChatColor.DARK_RED);

    private int id;
    private String name;
    private String prefix;
    private ChatColor color;

    Rank(int id,String name,String prefix,ChatColor color){
        this.id = id;
        this.name = name;
        this.prefix = prefix;
        this.color = color;
    }

    public int getID(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }

    public String getPrefix(){
        return this.prefix;
    }

    public ChatColor getColor(){
        return this.color;
    }

    public static Rank fromID(int id){
        for(Rank r : values()){
            if(r.getID() == id) return r;
        }

        return null;
    }
}
