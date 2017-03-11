import de.umass.lastfm.*;

import java.text.DateFormat;
import java.util.*;

/**
 * Created by Lukas on 05-Apr-16.
 */
public class SongFinder {

    private static String key = "enter_key_here";
    private static String user = "Lukkiebe";

    private static String songByArtist = "Adana Twins";


    public static void main(String[] args) {
        Caller.getInstance().setUserAgent("tst");
        //Caller.getInstance().setDebugMode(true);


        PaginatedResult<Track> trackChart = User.getArtistTracks(user, songByArtist, key);
        Collection<Track> allTracks = trackChart.getPageResults();
        for (Track t : allTracks) {
            System.out.println("Title: "+t.getName()+"\t\tPlayed on: "+t.getPlayedWhen().toString() + "("+t.getPlayedWhen().getTime()+")");
        }
    }

    public static ArrayList<Song> getSongsInRange(String user, long from, long to) {
        System.out.println("Getting songs for user "+user+" in range from "+from+" to "+to);
        Caller.getInstance().setUserAgent("tst");

        PaginatedResult<Track> tracks = getTracksInRange(user, from, to, key);
        Collection<Track> allTracks = tracks.getPageResults();

        //Chart<Track> tracks = User.getWeeklyTrackChart(user, ""+from, ""+to, 200, key);
        //Collection<Track> allTracks = tracks.getEntries();
        ArrayList<Song> songs = new ArrayList<Song>();
        HashSet<String> collectedSongs = new HashSet<String>();
        System.out.println(allTracks.size() +" tracks found.");
        for (Track t : allTracks) {
            if (!collectedSongs.contains(t.getName())) {
                System.out.printf("\"%s\" by %s\n", t.getName(), t.getArtist());
                songs.add(new Song(t.getName(), t.getArtist()));
                collectedSongs.add(t.getName());
            }
        }

        return songs;
    }

    public static PaginatedResult<Track> getTracksInRange(String user, long from, long to, String apiKey) {
        HashMap params = new HashMap();
        params.put("user", user);
        params.put("limit", ""+200);
        params.put("from", ""+from);
        params.put("to", ""+to);

        //params.put("page", 1);
        Result result = Caller.getInstance().call("user.getRecentTracks", apiKey, params);
        return ResponseBuilder.buildPaginatedResult(result, Track.class);
    }
}
