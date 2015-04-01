package com.percyvega.application;

import com.percyvega.jms.JMSSender;
import com.percyvega.model.Carrier;
import com.percyvega.util.Sleeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Created by pevega on 4/1/2015.
 */
@Component
public class MyCLR implements CommandLineRunner {

//    private static final Logger logger = LoggerFactory.getLogger(MyCLR.class);

    @Autowired
    private JMSSender jmsSender;

    @Value("${sourceUrl}")
    private String sourceUrl;

    @Override
    public void run(String... args) throws Exception {
        jmsSender.init();

        CarrierPickUpThread.setJmsSender(jmsSender);
        CarrierPickUpThread.setSourceUrl(sourceUrl);

        for (Carrier carrier : Carrier.values()) {
            new CarrierPickUpThread(carrier).start();
            Sleeper.sleep(CarrierPickUpThread.SLEEP_AFTER_PROCESSING / Carrier.values().length);
        }
    }
}
