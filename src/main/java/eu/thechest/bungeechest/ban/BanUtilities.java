package eu.thechest.bungeechest.ban;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by zeryt on 24.02.2017.
 */
public class BanUtilities {
    public static HashMap<UUID, Ban> ACTIVE_BANS = new HashMap<UUID, Ban>();
    public static HashMap<UUID, Mute> ACTIVE_MUTES = new HashMap<UUID, Mute>();
    public static HashMap<String, IPBan> ACTIVE_IPBANS = new HashMap<String, IPBan>();

    private static void registerBan(UUID uuid){
        new Ban(uuid);
    }

    private static void registerMute(UUID uuid){
        new Mute(uuid);
    }

    private static void registerIPBan(String ip){
        new IPBan(ip);
    }

    public static Ban getBan(UUID uuid){
        if(ACTIVE_BANS.containsKey(uuid)){
            return ACTIVE_BANS.get(uuid);
        } else {
            registerBan(uuid);

            if(ACTIVE_BANS.containsKey(uuid)){
                return ACTIVE_BANS.get(uuid);
            } else {
                return null;
            }
        }
    }

    public static Mute getMute(UUID uuid){
        if(ACTIVE_MUTES.containsKey(uuid)){
            return ACTIVE_MUTES.get(uuid);
        } else {
            registerMute(uuid);

            if(ACTIVE_MUTES.containsKey(uuid)){
                return ACTIVE_MUTES.get(uuid);
            } else {
                return null;
            }
        }
    }

    public static IPBan getIPBan(String ip){
        if(ACTIVE_IPBANS.containsKey(ip)){
            return ACTIVE_IPBANS.get(ip);
        } else {
            registerIPBan(ip);

            if(ACTIVE_IPBANS.containsKey(ip)){
                return ACTIVE_IPBANS.get(ip);
            } else {
                return null;
            }
        }
    }

    public static boolean isBanned(UUID uuid){
        if(getBan(uuid) == null){
            return false;
        } else {
            boolean b = !getBan(uuid).isExpired();
            return b;
        }
    }

    public static boolean isMuted(UUID uuid){
        if(getMute(uuid) == null){
            return false;
        } else {
            return !getMute(uuid).isExpired();
        }
    }

    public static boolean isIPBanned(String ip){
        return getIPBan(ip) != null;
    }
}
