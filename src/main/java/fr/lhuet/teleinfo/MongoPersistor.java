package fr.lhuet.teleinfo;

import com.mongodb.*;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import javax.net.ssl.SSLSocketFactory;
import java.net.UnknownHostException;
import java.util.Date;


/**
 * Created by lhuet on 30/05/14.
 */
public class MongoPersistor extends BusModBase implements Handler<Message<JsonObject>> {

    protected String address;
    protected String host;
    protected int port;
    protected String dbName;
    protected String username;
    protected String password;
    protected ReadPreference readPreference;
    protected boolean autoConnectRetry;
    protected int socketTimeout;
    protected boolean useSSL;

    protected Mongo mongo;
    protected DB db;


    @Override
    public void start() {
        super.start();

        address = getOptionalStringConfig("address", "teleinfo-mongopersistor");

        host = getOptionalStringConfig("host", "localhost");
        port = getOptionalIntConfig("port", 27017);
        dbName = getOptionalStringConfig("db_name", "teleinfo");
        username = getOptionalStringConfig("username", null);
        password = getOptionalStringConfig("password", null);
        readPreference = ReadPreference.valueOf(getOptionalStringConfig("read_preference", "primary"));
        int poolSize = getOptionalIntConfig("pool_size", 10);
        autoConnectRetry = getOptionalBooleanConfig("auto_connect_retry", true);
        socketTimeout = getOptionalIntConfig("socket_timeout", 60000);
        useSSL = getOptionalBooleanConfig("use_ssl", false);

        try {
            MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
            builder.connectionsPerHost(poolSize);
            builder.autoConnectRetry(autoConnectRetry);
            builder.socketTimeout(socketTimeout);
            builder.readPreference(readPreference);

            if (useSSL) {
                builder.socketFactory(SSLSocketFactory.getDefault());
            }

            ServerAddress address = new ServerAddress(host, port);
            mongo = new MongoClient(address, builder.build());
            db = mongo.getDB(dbName);
            if (username != null && password != null) {
                db.authenticate(username, password.toCharArray());
            }
        } catch (UnknownHostException e) {
            logger.error("Failed to connect to mongo server", e);
        }
        eb.registerHandler(address, this);
    }

    @Override
    public void stop() {
        if (mongo != null) {
            mongo.close();
        }
    }

    @Override
    public void handle(Message<JsonObject> event) {

        JsonObject dataToInsert = event.body();

        BasicDBObject doc = new BasicDBObject("datetime", new Date())
                .append("indexcpt", dataToInsert.getInteger("INDEX"))
                .append("imoy", dataToInsert.getNumber("IMOY"))
                .append("imax", dataToInsert.getInteger("IMAX"))
                .append("pmoy", dataToInsert.getNumber("PMOY"))
                .append("pmax", dataToInsert.getInteger("PMAX"));

        DBCollection coll = db.getCollection(getOptionalStringConfig("collection", "teleinfo"));

        coll.insert(doc);

    }
}
