package eu.thechest.bungeechest.crews;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * Created by zeryt on 11.03.2017.
 */
public class CrewMember {
    public UUID uuid;
    public int crewID;
    public Timestamp timeJoined;
    public CrewRank rank;

    public CrewMember(UUID uuid, int crewID, Timestamp timeJoined, CrewRank rank){
        this.uuid = uuid;
        this.crewID = crewID;
        this.timeJoined = timeJoined;
        this.rank = rank;
    }
}
