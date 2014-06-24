package fr.lhuet.teleinfo;

import org.vertx.java.busmods.BusModBase;

/**
 * Created by lhuet on 29/05/14.
 */
public class MainVerticle extends BusModBase {

    @Override
    public void start() {

        super.start();

        container.deployWorkerVerticle("fr.lhuet.teleinfo.MongoPersistor", config.getObject("mongo-persistor"));
        container.deployVerticle("fr.lhuet.teleinfo.MongoLoggerVerticle");
        container.deployWorkerVerticle("fr.lhuet.teleinfo.TeleinfoHardwareWorkerVerticle");
        container.deployWorkerVerticle("fr.lhuet.dhw.DHWVerticle", config.getObject("dhw-system"));

        logger.info("MainVerticle teleinfo launched");
    }
}
