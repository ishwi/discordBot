package core.commands.charts;

import core.apis.last.entities.chartentities.ChartUtil;
import core.apis.last.entities.chartentities.TopEntity;
import core.apis.last.queues.GroupingQueue;
import core.apis.last.queues.TrackGroupArtistQueue;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.params.ChartGroupParameters;
import dao.ChuuService;
import dao.entities.CountWrapper;
import dao.entities.DiscordUserDisplay;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.knowm.xchart.PieChart;

import java.util.List;

public class WastedChartCommand extends GroupingChartCommand {


    public WastedChartCommand(ChuuService dao) {
        super(dao);
    }


    @Override
    public String getDescription() {
        return "Chart with time spent on artists";
    }

    @Override
    public List<String> getAliases() {
        return List.of("timeartist", "tart", "tar", "ta", "timeartists");
    }


    @Override
    public String getName() {
        return "Artists ordered by listening time";
    }


    @Override
    public CountWrapper<GroupingQueue> processGroupedQueue(ChartGroupParameters params) throws LastFmException {
        GroupingQueue queue;

        if (params.isList()) {
            queue = new TrackGroupArtistQueue(db, discogsApi, spotifyApi, 200);

            lastFM.getChart(params.getUser(), params.getTimeFrameEnum(), 1499, 1, TopEntity.TRACK,
                    ChartUtil.getParser(params.getTimeFrameEnum(), TopEntity.ARTIST, params, lastFM, params.getUser()), queue);
        } else {
            queue = new TrackGroupArtistQueue(db, discogsApi, spotifyApi, params.getX() * params.getY());
            lastFM.getChart(params.getUser(), params.getTimeFrameEnum(), 1499, 1, TopEntity.TRACK,
                    ChartUtil.getParser(params.getTimeFrameEnum(), TopEntity.ARTIST, params, lastFM, params.getUser()), queue);
        }
        return new CountWrapper<>(-1, queue);
    }

    @Override
    public EmbedBuilder configEmbed(EmbedBuilder embedBuilder, ChartGroupParameters params, int count) {
        return params.initEmbed("'s most listened artists", embedBuilder,
                String.format(" has listened to artists for %s", String.format("%d:%02d hours", count / 3600, count / 60 % 60)), params.getUser().getName());
    }

    @Override
    public String configPieChart(PieChart pieChart, ChartGroupParameters params, int count, String initTitle) {
        pieChart.setTitle(initTitle + "'s most listened artists" + params.getTimeFrameEnum().getDisplayString());
        return String.format("%s has listened to %d artist (showing top %d)", initTitle, count, params.getX() * params.getY());

    }

    @Override
    public void noElementsMessage(ChartGroupParameters parameters) {
        MessageReceivedEvent e = parameters.getE();
        DiscordUserDisplay ingo = CommandUtil.getUserInfoConsideringGuildOrNot(e, parameters.getDiscordId());
        sendMessageQueue(e, String.format("%s didn't listen to any artist%s!", ingo.getUsername(), parameters.getTimeFrameEnum().getDisplayString()));
    }
}
