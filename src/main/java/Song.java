/**
 * Created by Lukas on 05-Apr-16.
 */
public class Song {

    // Gebruiken om data van SongFinder naar SpotifyPlaylistCreator te sturen.
    String title;
    String artist;

    public Song(String title, String artist) {
        this.title = title;
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }



    public String getSearchTerm() {
        return "title:"+title+" artist:"+artist;
    }
}
