package core.scheduledtasks;

import core.Chuu;
import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.exceptions.DiscogsServiceException;
import dao.DaoImplementation;
import dao.entities.ArtistInfo;

import java.util.Set;

public class ImageUpdaterThread implements Runnable {
	private final DaoImplementation dao;
	private final DiscogsApi discogsApi;

	public ImageUpdaterThread(DaoImplementation dao) {
		this.dao = dao;
		this.discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();

	}

	@Override
	public void run() {
		Set<String> artistData = dao.getNullUrls();
		System.out.println("Found at lest " + artistData.size() + "null artist ");
		for (String artistDatum : artistData) {
			String url;
			System.out.println("Working with artist " + artistDatum);
			try {
				url = discogsApi.findArtistImage(artistDatum);
				if (url != null) {

					System.out.println("Upserting buddy");
					if (!url.isEmpty())
						System.out.println(artistDatum);
					dao.upsertUrl(new ArtistInfo(url, artistDatum));
				}
			} catch (DiscogsServiceException e) {
				Chuu.getLogger().warn(e.getMessage(), e);


			}
		}
	}


}