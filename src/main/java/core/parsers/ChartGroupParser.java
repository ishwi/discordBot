package core.parsers;

import core.exceptions.ChuuServiceException;
import core.exceptions.InstanceNotFoundException;
import core.exceptions.LastFmException;
import core.parsers.params.ChartGroupParameters;
import core.parsers.params.ChartParameters;
import dao.ChuuService;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ChartGroupParser extends ChartableParser<ChartGroupParameters> {

    private final ChartParser inner;

    public ChartGroupParser(ChuuService dao, TimeFrameEnum defaultTFE) {
        super(dao, defaultTFE);
        this.inner = new ChartParser(dao, defaultTFE);
        this.inner.addOptional(new OptionalEntity("--notime", "dont display time spent"));
        this.opts.addAll(this.inner.opts);
    }

    @Override
    public ChartGroupParameters parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {
        ChartParameters chartParameters;
        try {
            chartParameters = inner.parse(e);
            return new ChartGroupParameters(e, chartParameters.getLastfmID(), chartParameters.getDiscordId(), chartParameters.getTimeFrameEnum(), chartParameters.getX(), chartParameters.getY(), !chartParameters.hasOptional("--notime"), chartParameters.chartMode());

        } catch (LastFmException lastFmException) {
            throw new ChuuServiceException("Improvable Exception");
        }
    }
}