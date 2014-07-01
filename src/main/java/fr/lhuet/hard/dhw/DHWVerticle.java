package fr.lhuet.hard.dhw;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;

import java.io.IOException;

/**
 * Created by lhuet on 30/05/14.
 */
public class DHWVerticle extends BusModBase{

    private float dhwTemp;
    private float bufferTemp;

    @Override
    public void start() {
        super.start();

        String w1_dhw_file = getMandatoryStringConfig("dhw");
        String w1_buffer_file = getMandatoryStringConfig("buffer-storage");

        vertx.setPeriodic(60000, event -> {
            try {
                readW1temp(w1_dhw_file, "dhw");
                readW1temp(w1_buffer_file, "buffer");
            } catch (IOException e) {
                logger.error("Exception while reading 1-Wire virtual file", e);
            }
        });

        eb.registerHandler("dhw-temp", (Message<String> req) -> {
            switch (req.body()) {
                case "dhw":
                    req.reply(this.dhwTemp);
                    break;
                case "buffer":
                    req.reply(this.bufferTemp);
                    break;
                default:
                    req.reply(Double.valueOf(null));
            }
        });
    }

    private void readW1temp(String file, String sensor) throws IOException {
        // w1 file content like :
        //    ce 02 4b 46 7f ff 02 10 0c : crc=0c YES
        //    ce 02 4b 46 7f ff 02 10 0c t=44875
        vertx.fileSystem().readFile(file, (AsyncResult<Buffer> res) -> {
            String content = res.result().toString();
            if (content.contains("YES")) {
                // CRC ok -> extract Temp in deg C
                String[] temp = content.split("t=");
                switch (sensor) {
                    case "dhw":
                        this.dhwTemp = Float.valueOf(temp[1]).floatValue()/1000;
                        break;
                    case "buffer":
                        this.bufferTemp = Float.valueOf(temp[1]).floatValue()/1000;
                        break;
                    default:
                        logger.error("readW1temp -> Bad sensor switch");
                }
            } else {
                throw new RuntimeException("(One Wire bus error) Bad CRC on file " + file);
            }
        });
    }

}
