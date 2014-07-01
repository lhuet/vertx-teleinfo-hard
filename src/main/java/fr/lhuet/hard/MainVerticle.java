package fr.lhuet.hard;

import org.vertx.java.busmods.BusModBase;

/**
 * Created by lhuet on 29/05/14.
 */
public class MainVerticle extends BusModBase {

    @Override
    public void start() {

        super.start();

        container.deployModule("io.vertx~mod-mongo-persistor~2.1.0", config.getObject("mongo-persistor"));
        container.deployVerticle("fr.lhuet.hard.teleinfo.MongoLoggerVerticle");
        container.deployWorkerVerticle("fr.lhuet.hard.teleinfo.TeleinfoHardwareWorkerVerticle");
        container.deployWorkerVerticle("fr.lhuet.hard.dhw.DHWVerticle", config.getObject("dhw-system"));

        logger.info("MainVerticle teleinfo launched");
    }
}
