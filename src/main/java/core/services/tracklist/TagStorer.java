package core.services.tracklist;

import core.apis.last.ConcurrentLastFM;
import core.apis.last.entities.chartentities.TopEntity;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.services.TagAlbumService;
import core.services.TagArtistService;
import core.services.TrackTagService;
import dao.ChuuService;
import dao.entities.AlbumInfo;
import dao.entities.ArtistInfo;
import dao.entities.NowPlayingArtist;
import dao.entities.TrackInfo;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ExecutorService;

public record TagStorer(ChuuService db, ConcurrentLastFM lastFM,
                        ExecutorService executor, NowPlayingArtist nowPlayingInfo) {

    public List<String> findTags() throws LastFmException {
        return findTags(5);
    }

    public List<String> findTags(int limit) throws LastFmException {
        Set<String> tags;
        if ((CommandUtil.rand.nextFloat() < 0.8)) {
            tags = getStrings(limit, getArtistTags(limit), getTrackTags(limit));
        } else {
            tags = getStrings(limit, getTrackTags(limit), getArtistTags(limit));
        }
        return new ArrayList<>(tags);
    }

    @NotNull
    private Set<String> getStrings(int limit, List<String> first, List<String> fallback) throws LastFmException {
        Set<String> tags;
        tags = new HashSet<>(first);
        if (tags.isEmpty() || tags.size() < limit) {
            tags.addAll(fallback);
            if (tags.isEmpty() || tags.size() < limit) {
                tags.addAll(getAlbumTags(limit));
            }
        }
        return tags;
    }

    private List<String> getArtistTags(int limit) throws LastFmException {
        var tags = lastFM.getTrackTags(limit, TopEntity.ARTIST, nowPlayingInfo.artistName(), null);
        if (!tags.isEmpty()) {
            executor.submit(new TagArtistService(db, lastFM, tags, new ArtistInfo(nowPlayingInfo.url(), nowPlayingInfo.artistName(), nowPlayingInfo.artistMbid())));
        }
        return tags;
    }

    private List<String> getAlbumTags(int limit) throws LastFmException {
        if (nowPlayingInfo.albumName() != null && !nowPlayingInfo.albumName().isBlank()) {
            var tags = lastFM.getTrackTags(limit, TopEntity.ALBUM, nowPlayingInfo.artistName(), nowPlayingInfo.albumName());
            if (!tags.isEmpty()) {
                executor.submit(new TagAlbumService(db, lastFM, tags, new AlbumInfo(null, nowPlayingInfo.albumName(), nowPlayingInfo.artistName())));
            }
            return tags;
        } else {
            return Collections.emptyList();
        }
    }

    private List<String> getTrackTags(int limit) throws LastFmException {
        var tags = lastFM.getTrackTags(limit, TopEntity.TRACK, nowPlayingInfo.artistName(), nowPlayingInfo.songName());
        if (!tags.isEmpty()) {
            executor.submit(new TrackTagService(db, lastFM, tags, new TrackInfo(nowPlayingInfo.artistName(), null, nowPlayingInfo.songName(), null)));
        }
        return tags;
    }


}