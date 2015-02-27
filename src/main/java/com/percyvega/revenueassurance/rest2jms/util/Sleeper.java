package com.percyvega.revenueassurance.rest2jms.util;

/**
 * Created by pevega on 2/25/2015.
 */
public class Sleeper {

//    private static final Logger logger = LoggerFactory.getLogger(Sleeper.class);

    public static void sleep(int mils) {
//        logger.debug("About to sleep(" + mils + ")");
        try {
            Thread.sleep(mils);
//            logger.debug("Waking up...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}