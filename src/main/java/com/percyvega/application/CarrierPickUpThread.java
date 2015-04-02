package com.percyvega.application;

import com.percyvega.jms.JMSSender;
import com.percyvega.model.Carrier;
import com.percyvega.model.IntergateTransaction;
import com.percyvega.model.Status;
import com.percyvega.util.JacksonUtil;
import com.percyvega.util.Sleeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.jms.JMSException;

/**
 * Created by pevega on 2/25/2015.
 */
public class CarrierPickUpThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(CarrierPickUpThread.class);

    private final Carrier CARRIER;
    private final Status OLD_STATUS = Status.NEW;
    private final Status NEW_STATUS = Status.PICKED_UP;
    public static final int RECORDS_TO_PICK_UP_COUNT = 5;
    public static final int SLEEP_AFTER_PROCESSING = 3000;
    public static final int SLEEP_WHEN_UNAVAILABLE_SOURCE = 10000;
    public static final int SLEEP_WHEN_UNAVAILABLE_DESTINATION = 10000;
    public static final int SLEEP_WHEN_NO_RECORDS_FOUND = 5000;

    private static RestTemplate restTemplate = new RestTemplate();

    private static JMSSender jmsSender;
    public static void setJmsSender(JMSSender jmsSender) {
        CarrierPickUpThread.jmsSender = jmsSender;
    }

    private static String sourceUrl;
    public static void setSourceUrl(String sourceUrl) {
        CarrierPickUpThread.sourceUrl = sourceUrl;
    }

    public CarrierPickUpThread(Carrier carrier) {
        super(carrier.getName());

        if(sourceUrl == null)
            throw new RuntimeException("sourceUrl cannot be null.");
        if(jmsSender == null)
            throw new RuntimeException("jmsSender cannot be null.");

        this.CARRIER = carrier;
    }

    @Override
    public void run() {
        logger.debug("Starting run()");

        IntergateTransaction[] txs = null;

        boolean isSourceUnavailable;
        int sourceUnavailableCount;
        boolean isDestinationUnavailable;
        int destinationUnavailableCount;

        try {
            while (true) {

                sourceUnavailableCount = 0;
                do {
                    try {
                        txs = restTemplate.getForObject(getUrl(), IntergateTransaction[].class);
                        isSourceUnavailable = false;
                    } catch (ResourceAccessException e) {
                        logger.debug("Source unavailable #" + ++sourceUnavailableCount + ". About to sleep(" + SLEEP_WHEN_UNAVAILABLE_SOURCE + ").");
                        Sleeper.sleep(SLEEP_WHEN_UNAVAILABLE_SOURCE);
                        isSourceUnavailable = true;
                    }
                } while (isSourceUnavailable);

                if (txs.length > 0) {
                    for (int i = 0; i < txs.length; i++) {
                        logger.debug("Processing array, record " + (i + 1) + " of " + txs.length + ": " + JacksonUtil.fromTransactionToJson(txs[i]));

                        destinationUnavailableCount = 0;
                        do {
                            try {
                                jmsSender.sendMessage(Long.toString(txs[i].getObjid()), JacksonUtil.fromTransactionToJson(txs[i]));
                                isDestinationUnavailable = false;
                            } catch (JMSException e) {
                                logger.debug("Destination unavailable #" + ++destinationUnavailableCount + ". About to sleep(" + SLEEP_WHEN_UNAVAILABLE_DESTINATION + ").");
                                Sleeper.sleep(SLEEP_WHEN_UNAVAILABLE_SOURCE);
                                isDestinationUnavailable = true;
                            }
                        } while (isDestinationUnavailable);
                    }
                    logger.debug("Finished processing. About to sleep(" + SLEEP_AFTER_PROCESSING + ").");
                    Sleeper.sleep(SLEEP_AFTER_PROCESSING);
                } else {
                    logger.debug("No records to process. About to sleep(" + SLEEP_WHEN_NO_RECORDS_FOUND + ").");
                    Sleeper.sleep(SLEEP_WHEN_NO_RECORDS_FOUND);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            logger.debug("Finishing run()");
        }

    }

    private String getUrl() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(sourceUrl);
        builder.queryParam("oldStatus", OLD_STATUS.getName());
        builder.queryParam("newStatus", NEW_STATUS.getName());
        builder.queryParam("carrier", CARRIER.getName());
        builder.queryParam("count", Integer.toString(RECORDS_TO_PICK_UP_COUNT));

        return builder.build().toUriString();
    }

}
