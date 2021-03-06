package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.explanation.util.Interactible;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class YearExplanation implements Explanation {
    public static final String NAME = "year";

    @Override
    public Interactible explanation() {
        return new ExplanationLineType(NAME,
                "If the year is not specified it defaults to the current year", OptionType.INTEGER
        );
    }
}
