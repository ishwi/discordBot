package core.apis.spotify;

import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.special.SearchResult;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.search.SearchItemRequest;
import core.Chuu;

import java.io.IOException;
import java.time.LocalDateTime;

public class Spotify {

	private final SpotifyApi spotifyApi;
	private final ClientCredentialsRequest clientCredentialsRequest;
	private LocalDateTime time;

	public Spotify(String clientSecret, String clientId) {
		SpotifyApi spotifyApi = new SpotifyApi.Builder()
				.setClientId(clientId).setClientSecret(clientSecret).build();
		this.clientCredentialsRequest = spotifyApi.clientCredentials().build();

		this.spotifyApi = spotifyApi;

		clientCredentials_Sync();


	}

	private void clientCredentials_Sync() {
		try {
			ClientCredentials clientCredentials = this.clientCredentialsRequest.execute();

			// Set access token for further "spotifyApi" object usage
			spotifyApi.setAccessToken(clientCredentials.getAccessToken());
			this.time = LocalDateTime.now().plusSeconds(clientCredentials.getExpiresIn() - 140);

			System.out.println("Expires in: " + clientCredentials.getExpiresIn());
		} catch (IOException | SpotifyWebApiException e) {
			System.out.println("Error: " + e.getMessage());
		}
	}



	private void initRequest() {
		if (!this.time.isAfter(LocalDateTime.now())) {
			clientCredentials_Sync();
		}
	}

	public String searchItems(String track, String artist, String album) {
		initRequest();
		artist = artist.contains(":") ? "\"" + artist + "\"" : artist;
		SearchItemRequest tracksRequest =
				spotifyApi.searchItem("album:" + album + " artist:" + artist + " track:" + track, "album,artist,track").
						market(CountryCode.NZ)
						.limit(1)
						.offset(0)
						.build();
		String returned = "";
		try {
			SearchResult searchResult = tracksRequest.execute();

			for (Track item : searchResult.getTracks().getItems()) {
				returned = "https://open.spotify.com/track/" + item.getUri().split("spotify:track:")[1];
			}
			return returned;
		} catch (IOException | SpotifyWebApiException e) {
			Chuu.getLogger().warn(e.getMessage(), e);
		}
		return returned;
	}

	public String getArtistUrlImage(String artist) {
		initRequest();
		artist = artist.contains(":") ? "\"" + artist + "\"" : artist;
		SearchItemRequest tracksRequest =
				spotifyApi.searchItem(" artist:" + artist, "artist").
						market(CountryCode.NZ)
						.limit(1)
						.offset(0)
						.build();
		String returned = "";
		try {
			SearchResult searchResult = tracksRequest.execute();

			for (Artist item : searchResult.getArtists().getItems()) {
				Image[] images = item.getImages();
				if (images.length != 0)
					returned = images[0].getUrl();
			}
		} catch (IOException | SpotifyWebApiException e) {
			Chuu.getLogger().warn(e.getMessage(), e);
		}
		return returned;
	}
}
