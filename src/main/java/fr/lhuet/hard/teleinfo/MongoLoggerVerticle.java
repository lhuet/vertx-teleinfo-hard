package fr.lhuet.hard.teleinfo;

import fr.lhuet.hard.teleinfo.pojo.TeleinfoData;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import java.util.Date;

/**
 * Created by lhuet on 29/05/14.
 */
public class MongoLoggerVerticle extends BusModBase {

    private TeleinfoData data;

    @Override
    public void start() {
        super.start();
        logger.info("MongoLoggerVerticle launched ...");

        // Init TeleinfoData
        data = new TeleinfoData();
        initData();

        // Set handler to flush data to mongo every minute
        vertx.setPeriodic(60000, event -> flushToMongo());

        eb.registerHandler("trame", (Message<JsonObject> event) -> dataUpdate(event.body()));

        // Set Handler to expose instant values
        eb.registerHandler("teleinfo-data", (Message<String> request) -> {
            if (request.body().equals("get")) {
                JsonObject res = new JsonObject();
                res.putNumber("IINST", data.getIinst());
                res.putNumber("PAPP", data.getPapp());
                res.putNumber("INDEX", data.getIndexcpt());
                request.reply(res);
            }
        });

    }

    private void dataUpdate(JsonObject teleinfo) {
        logger.debug("Update teleinfo data" + teleinfo);

        data.setIndexcpt(teleinfo.getInteger("BASE"));
        data.setNbdata(data.getNbdata() + 1);
        data.setSumiinst(data.getSumiinst() + teleinfo.getInteger("IINST"));
        data.setIinst(teleinfo.getInteger("IINST"));
        data.setSumpapp(data.getSumpapp() + teleinfo.getInteger("PAPP"));
        data.setPapp(teleinfo.getInteger("PAPP"));
        if (teleinfo.getInteger("PAPP") > data.getPmax()) {
            data.setPmax(teleinfo.getInteger("PAPP"));
        }
        if (teleinfo.getInteger("IINST") > data.getImax()) {
            data.setImax(teleinfo.getInteger("IINST"));
        }
    }

    private void flushToMongo() {

        JsonObject dataToSave = new JsonObject();
        JsonObject dateJson = new JsonObject();
        Date now = new Date();
        dateJson.putValue("$date", now.getTime());
        dataToSave.putObject("datetime", dateJson);
        dataToSave.putNumber("pmax", data.getPmax());
        dataToSave.putNumber("imax", data.getImax());
        dataToSave.putNumber("imoy", ((float) data.getSumiinst()) / data.getNbdata());
        dataToSave.putNumber("pmoy", ((float) data.getSumpapp()) /data.getNbdata());
        dataToSave.putNumber("indexcpt", data.getIndexcpt());

        JsonObject mongoRequest = new JsonObject();
        mongoRequest.putString("action", "save");
        mongoRequest.putString("collection", "teleinfo");
        mongoRequest.putObject("document", dataToSave);

        logger.debug("flushing values to mongo " + dataToSave);
        eb.send("mongo-persistor", mongoRequest);

        initData();
    }

    private void initData() {
        data.setImax(0);
        data.setPmax(0);
        data.setNbdata(0);
        data.setSumiinst(0);
        data.setSumpapp(0);
    }

}
