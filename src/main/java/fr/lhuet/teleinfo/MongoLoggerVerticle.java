package fr.lhuet.teleinfo;

import fr.lhuet.teleinfo.pojo.TeleinfoData;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

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
        dataToSave.putNumber("PMAX", data.getPmax());
        dataToSave.putNumber("IMAX", data.getImax());
        dataToSave.putNumber("IMOY", ((float) data.getSumiinst()) / data.getNbdata());
        dataToSave.putNumber("PMOY", ((float) data.getSumpapp()) /data.getNbdata());
        dataToSave.putNumber("INDEX", data.getIndexcpt());

        logger.debug("flushing values to mongo " + dataToSave);
        eb.send("teleinfo-mongopersistor", dataToSave);

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
