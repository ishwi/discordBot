package core.commands.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.zaxxer.hikari.HikariDataSource;
import core.Chuu;
import core.apis.last.ConcurrentLastFM;
import core.commands.Context;
import dao.ChuuService;
import dao.ServiceView;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.LoggerFactory;

public record EvalContext(JDA jda, Context e,
                          User owner, Guild guild,
                          String[] params, ChuuService db, ConcurrentLastFM lastFM) {

    public void sendMessage(Object message) {
        e.sendMessage(message.toString()).queue();
    }


    public void checkDB() throws Exception {
        var db = (ServiceView) FieldUtils.readStaticField(core.Chuu.class, "db", true);
        var b = FieldUtils.readField(db.longService(), "dataSource", true);
        var longPool = ((HikariDataSource) (FieldUtils.readField(b, "ds", true))).getHikariPoolMXBean();


        var db2 = (ServiceView) FieldUtils.readStaticField(core.Chuu.class, "db", true);
        var b2 = FieldUtils.readField(db2.normalService(), "dataSource", true);
        var shortPool = ((HikariDataSource) (FieldUtils.readField(b2, "ds", true))).getHikariPoolMXBean();

        String a = "Short pool => %d total | %d active | %d idle | %d waiting".formatted(shortPool.getTotalConnections(), shortPool.getActiveConnections(), shortPool.getIdleConnections(), shortPool.getThreadsAwaitingConnection());
        String b3 = "Long pool => %d total | %d active | %d idle | %d waiting".formatted(longPool.getTotalConnections(), longPool.getActiveConnections(), longPool.getIdleConnections(), longPool.getThreadsAwaitingConnection());
        sendMessage(a + "\n" + b3);
    }

    public void type(boolean type) {
        Chuu.doTyping = type;
        sendMessage("Typing status set to " + type);
    }

    public void setLog(String level) {
        var rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        var newLevel = Level.toLevel(level);
        rootLogger.setLevel(newLevel);
        sendMessage("Typing status set to " + newLevel);
    }
}
