package eu.thechest.bungeechest.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Created by zeryt on 29.03.2017.
 */
public class StoredChatMessage {
    public String message;
    public String time;
    public String server;

    public StoredChatMessage(String message, Timestamp time, String server){
        this.message = message;
        this.time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time);
        this.server = server;
    }
}
