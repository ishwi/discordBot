package core.parsers.params;

import core.commands.Context;
import core.parsers.utils.CustomTimeFrame;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;

public class ChartSizeParameters extends ChartParameters {

    public ChartSizeParameters(Context e, int x, int y, LastFMData lastFMData) {
        super(e, lastFMData, CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL), x, y);
    }
}
