package core.services.tags;

import core.apis.last.ConcurrentLastFM;
import core.exceptions.LastFmException;
import core.services.validators.ArtistValidator;
import dao.ChuuService;
import dao.entities.ArtistInfo;
import dao.entities.Genre;
import dao.entities.ScrobbledArtist;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class TagArtistService extends TagService<ArtistInfo, ScrobbledArtist> {
    public TagArtistService(ChuuService dao, ConcurrentLastFM lastFM, Map<Genre, List<ArtistInfo>> genres) {
        super(dao, lastFM, genres);
    }

    public TagArtistService(ChuuService dao, ConcurrentLastFM lastFM, List<String> tags, ArtistInfo artistInfo) {
        super(dao, lastFM, tags.stream().map(x -> new Genre(x)).distinct()
                .collect(Collectors.toMap((t) -> t, t -> Collections.singletonList(artistInfo))));
    }

    public TagArtistService(ChuuService dao, ConcurrentLastFM lastFM, List<ArtistInfo> artists, String genre) {
        super(dao, lastFM, artists, genre);
    }

    @Override
    protected void insertGenres(Map<Genre, List<ScrobbledArtist>> genres) {
        dao.insertArtistTags(genres);

    }

    @Override
    protected Map<ArtistInfo, ScrobbledArtist> validate(List<ArtistInfo> toValidate) {
        Map<ArtistInfo, ScrobbledArtist> scrobbledArtistMap = new HashMap<>();
//        Map<String, AlbumInfo> mbidIndex = toValidate.stream().hasMbidToArtists(Collectors.toMap(EntityInfo::getMbid, x -> x));
        List<ScrobbledArtist> scrobbledArtists = toValidate.stream().map(x -> new ScrobbledArtist(x.getArtist(), 0, null)).toList();
        dao.filldArtistIds(scrobbledArtists);

        Map<String, ScrobbledArtist> foudnAlbumIndexMap = scrobbledArtists.stream().collect(Collectors.toMap(ScrobbledArtist::getArtist, x -> x, (x, y) -> x));
        Set<String> foundMbids = foudnAlbumIndexMap.keySet();
        Map<Boolean, List<ArtistInfo>> hasMbidToArtists = toValidate.stream().collect(Collectors.partitioningBy(x -> foundMbids.contains(x.getArtist())));
        List<ArtistInfo> foundAlbums = hasMbidToArtists.get(true);
        foundAlbums.forEach(x -> scrobbledArtistMap.put(x, foudnAlbumIndexMap.get(x.getArtist())));
        List<ArtistInfo> notFoundAlbums = hasMbidToArtists.get(false);
        notFoundAlbums.stream().map(x -> {
            try {
                return Pair.of(x, new ArtistValidator(dao, lastFM, null).validate(x.getArtist(), false, true));
            } catch (LastFmException exception) {
                return null;
            }
        }).filter(Objects::nonNull).forEach(x -> scrobbledArtistMap.put(x.getKey(), x.getValue()));
        return scrobbledArtistMap;
    }
}
