package eu.thechest.bungeechest.user;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.thechest.bungeechest.BungeeChest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zeryt on 23.02.2017.
 */
public class UUIDFetcher {

    public static Map<String, String> uuidCache;

    static {
        uuidCache = new HashMap<String, String>();
    }

    public static String insertDashUUID(String uuid) {
        StringBuffer sb = new StringBuffer(uuid);
        sb.insert(8, "-");

        sb = new StringBuffer(sb.toString());
        sb.insert(13, "-");

        sb = new StringBuffer(sb.toString());
        sb.insert(18, "-");

        sb = new StringBuffer(sb.toString());
        sb.insert(23, "-");

        return sb.toString();
    }

    @Deprecated
    public static String getUUID(String username) {
        if (BungeeChest.getInstance().getProxy().getPlayer(username) != null) {
            String uuid = BungeeChest.getInstance().getProxy().getPlayer(username).getUniqueId().toString();

            if (!uuidCache.containsKey(username)) uuidCache.put(username, uuid);

            return uuid;
        } else {
            if (uuidCache.containsKey(username)) return uuidCache.get(username);
            try {
                URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
                InputStream stream = url.openStream();
                InputStreamReader inr = new InputStreamReader(stream);
                BufferedReader reader = new BufferedReader(inr);
                String s = null;
                StringBuilder sb = new StringBuilder();
                while ((s = reader.readLine()) != null) {
                    sb.append(s);
                }
                String result = sb.toString();

                JsonElement element = new JsonParser().parse(result);
                JsonObject obj = element.getAsJsonObject();

                String uuid = obj.get("id").toString();

                uuid = uuid.substring(1);
                uuid = uuid.substring(0, uuid.length() - 1);
                //if(!uuid.contains("-")) uuid = String.format("%1$-%2$-%3$-%4$", uuid.substring(0,7), uuid.substring(7,11), uuid.substring(11,15), uuid.substring(15,20));
                if(!uuid.contains("-")) uuid = insertDashUUID(uuid);

                uuidCache.put(username, uuid);

                return uuid;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
