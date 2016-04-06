import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.wrapper.spotify.Api;
import com.wrapper.spotify.exceptions.WebApiException;
import com.wrapper.spotify.methods.AddTrackToPlaylistRequest;
import com.wrapper.spotify.methods.PlaylistCreationRequest;
import com.wrapper.spotify.methods.PlaylistRequest;
import com.wrapper.spotify.methods.TrackSearchRequest;
import com.wrapper.spotify.methods.authentication.ClientCredentialsGrantRequest;
import com.wrapper.spotify.models.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Lukas on 05-Apr-16.
 *
 * Creates a playlist based on Last.FM data. Fills the playlist with all songs played in a given range.
 */
public class SpotifyPlaylistCreator {
    private final static String lastFMuser = "Lukkiebe";
    private final static String lastFMkey = "3e82d0b33fd6e180f3c285337f9bbb52";

    private final static String spotifyUserID = "lukkie";
    private final static String spotifyClientID = "67c29a81a06b422e837d601bdb4380a0";
    private final static String spotifySecret = "a5d1fbda0cd24419bf8724693b4139e4";
    private final static String spotifyRedirectURI = "http://lukkietestshizzle.com/callback/";

    private final static long from = 1429280607L; // Fri Apr 17 16:23:27 CEST 2015 (in seconden)
    private final static int duration = 180; // in minuten
    //private final static String playListTitle = "LastFM History";
    private final static String playlistID = "7bqO6rEr9fQk0HnYMGDndM";


    //TODO: Checken of song al in playlist staat
    //TODO: Album error fixen


    public static void main(String[] args) {
        final Api api = Api.builder()
                .clientId(spotifyClientID)
                .clientSecret(spotifySecret)
                .redirectURI(spotifyRedirectURI)
                .build();
        long to = from + duration*60;

        //Get all (top) tracks from last FM between range
        ArrayList<Song> songs = SongFinder.getSongsInRange(lastFMuser, from-180*60, to);


        //Spotify authorization
        /* Set the necessary scopes that the application will need from the user */
        final List<String> scopes = Arrays.asList("user-read-private", "user-read-email", "playlist-modify-public");

        /* Set a state. This is used to prevent cross site request forgeries. */
        final String state = "someExpectedStateString";

        String authorizeURL = api.createAuthorizeURL(scopes, state);

        System.out.println("URL: "+authorizeURL);
        System.out.println("Authorize at URL and return the code");

        Scanner sc = new Scanner(System.in);

        //Apply authorization
        /* Application details necessary to get an access token */
        final String code = sc.nextLine();


        /* Make a token request. Asynchronous requests are made with the .getAsync method and synchronous requests
         * are made with the .get method. This holds for all type of requests. */
        final AuthorizationCodeCredentials authorizationCodeCredentials;
        try {
            authorizationCodeCredentials = api.authorizationCodeGrant(code).build().get();
            System.out.println("Successfully retrieved an access token! " + authorizationCodeCredentials.getAccessToken());
            System.out.println("The access token expires in " + authorizationCodeCredentials.getExpiresIn() + " seconds");
            System.out.println("Luckily, I can refresh it using this refresh token! " +     authorizationCodeCredentials.getRefreshToken());

            /* Set the access token and refresh token so that they are used whenever needed */
            api.setAccessToken(authorizationCodeCredentials.getAccessToken());
            api.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WebApiException e) {
            e.printStackTrace();
        }


        //Create playlist
        /*PlaylistCreationRequest playListRequest = null;
        try {
            playListRequest = api.createPlaylist(spotifyUserID, playListTitle)
                    .publicAccess(true)
                    .build();
        } catch(Exception e) {
            System.out.println("Couldn't create playlist: " + e.getMessage());
        }

        Playlist playlist = null;
        try {
            playlist = playListRequest.get();

            System.out.println("Playlist created.");
            System.out.println("Its title is " + playlist.getName());
        } catch (Exception e) {
            System.out.println("Something went wrong! " + e.getMessage());
        }*/



        // Open playlist
        final PlaylistRequest request = api.getPlaylist(spotifyUserID, playlistID).build();

        try {
            final Playlist playlist = request.get();

            System.out.println("Retrieved playlist " + playlist.getName());
            System.out.println(playlist.getDescription());
            System.out.println("It contains " + playlist.getTracks().getTotal() + " tracks");

        } catch (Exception e) {
            System.out.println("Something went wrong!" + e.getMessage());
        }



        //TODO: Search each track on spotify and add most relevant to playlist;
        ArrayList<String> trackUris = new ArrayList<String>();
        for (Song s: songs) {
            System.out.println("\n=====");

            final TrackSearchRequest trackSearchRequest = api.searchTracks(s.getSearchTerm()).market("BE").build();

            try {
                System.out.println("Searching for \""+s.getSearchTerm()+"\"");
                final Page<Track> trackSearchResult = trackSearchRequest.get();
                System.out.println(trackSearchResult.getTotal() + " results");
                System.out.println("First result: " + trackSearchResult.getItems().get(0).getName());
                String uri = trackSearchResult.getItems().get(0).getUri();
                System.out.println("URI: "+uri);
                trackUris.add(uri);

            } catch (Exception e) {
                System.out.println("Search Error!" + e.getMessage());
            }
            System.out.println("=====\n");

        }

        final AddTrackToPlaylistRequest addTrackToPlaylistRequest = api.addTracksToPlaylist(spotifyUserID, playlistID, trackUris)
                .position(0)
                .build();

        try {
            addTrackToPlaylistRequest.get();
            System.out.println("\n\n-- Playlist updated --");
        } catch (Exception e) {
            System.out.println("Something went wrong!" + e.getMessage());
        }

    }

}
