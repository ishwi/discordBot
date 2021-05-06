package core.commands.stats;

import core.commands.Context;
import core.commands.abstracts.ConcurrentCommand;
import core.commands.utils.CommandCategory;
import core.commands.utils.CommandUtil;
import core.parsers.NumberParser;
import core.parsers.OnlyUsernameParser;
import core.parsers.Parser;
import core.parsers.params.ChuuDataParams;
import core.parsers.params.NumberParameters;
import dao.ChuuService;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static core.parsers.ExtraParser.LIMIT_ERROR;

public class TotalArtistNumberCommand extends ConcurrentCommand<NumberParameters<ChuuDataParams>> {
    public TotalArtistNumberCommand(ChuuService dao) {
        super(dao);
    }

    @Override
    protected CommandCategory initCategory() {
        return CommandCategory.USER_STATS;
    }

    @Override
    public Parser<NumberParameters<ChuuDataParams>> initParser() {


        Map<Integer, String> map = new HashMap<>(2);
        map.put(LIMIT_ERROR, "The number introduced must be positive and not very big");
        String s = "You can also introduce the playcount to only show artists above that number of plays";
        return new NumberParser<>(new OnlyUsernameParser(db),
                -0L,
                Integer.MAX_VALUE,
                map, s, false, true, true, "filter");

    }

    @Override
    public String getDescription() {
        return ("Number of artists listened by an user");
    }

    @Override
    public List<String> getAliases() {
        return List.of("artists", "art");
    }

    @Override
    protected void onCommand(Context e, @NotNull NumberParameters<ChuuDataParams> params) {

        ChuuDataParams innerParams = params.getInnerParams();
        String lastFmName = innerParams.getLastFMData().getName();
        long discordID = innerParams.getLastFMData().getDiscordId();
        String username = getUserString(e, discordID, lastFmName);
        int threshold = params.getExtraParam().intValue();

        int plays = db.getUserArtistCount(lastFmName, threshold == 0 ? -1 : threshold);
        String filler = "";
        if (threshold != 0) {
            filler += " with more than " + threshold + " plays";
        }
        sendMessageQueue(e, String.format("**%s** has scrobbled **%d** different %s%s", username, plays, CommandUtil.singlePlural(plays, "artist", "artists"), filler));

    }

    @Override
    public String getName() {
        return "Artist count ";
    }
}
