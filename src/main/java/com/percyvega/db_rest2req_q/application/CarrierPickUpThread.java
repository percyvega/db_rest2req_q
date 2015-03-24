package com.percyvega.db_rest2req_q.application;

import com.percyvega.db_rest2req_q.jms.JMSSender;
import com.percyvega.db_rest2req_q.model.Status;
import com.percyvega.db_rest2req_q.util.Sleeper;
import com.percyvega.db_rest2req_q.model.Carrier;
import com.percyvega.db_rest2req_q.model.IntergateTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.jms.JMSException;

/**
 * Created by pevega on 2/25/2015.
 */
@Component
public class CarrierPickUpThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(CarrierPickUpThread.class);

    private final Carrier CARRIER;
    private final Status OLD_STATUS = Status.QUEUED;
    private final Status NEW_STATUS = Status.PICKED_UP;
    public static final int RECORDS_TO_PICK_UP_COUNT = 7;
    public static final int SLEEP_AFTER_PROCESSING = 10000;
    public static final int SLEEP_WHEN_UNAVAILABLE_SOURCE = 15000;
    public static final int SLEEP_WHEN_UNAVAILABLE_DESTINATION = 15000;
    public static final int SLEEP_WHEN_NO_RECORDS_FOUND = 20000;

    private static String restUrl;
    @Value("${restUrl}")
    public void setRestUrl(String restUrl) {
        this.restUrl = restUrl;
    }

    public CarrierPickUpThread() {
        super("do-not-use");
        this.CARRIER = null;
    }

    public CarrierPickUpThread(Carrier carrier) {
        super(carrier.getName());
        this.CARRIER = carrier;
    }

    @Override
    public void run() {
        logger.debug("Starting run()");

        JMSSender jmsSender = new JMSSender();
        RestTemplate restTemplate = new RestTemplate();
        IntergateTransaction[] txs = null;

        boolean isSourceUnavailable;
        int sourceUnavailableCount;
        boolean isDestinationUnavailable;
        int destinationUnavailableCount;

        String url = getUrl(OLD_STATUS, NEW_STATUS, CARRIER, RECORDS_TO_PICK_UP_COUNT);

        try {
            while (true) {

                sourceUnavailableCount = 0;
                do {
                    try {
                        txs = restTemplate.getForObject(url, IntergateTransaction[].class);
                        isSourceUnavailable = false;
                    } catch (ResourceAccessException e) {
                        logger.debug("Source unavailable #" + ++sourceUnavailableCount + ". About to sleep(" + SLEEP_WHEN_UNAVAILABLE_SOURCE + ").");
                        Sleeper.sleep(SLEEP_WHEN_UNAVAILABLE_SOURCE);
                        isSourceUnavailable = true;
                    }
                } while (isSourceUnavailable);

                if (txs.length > 0) {
                    for (int i = 0; i < txs.length; i++) {
                        logger.debug("Processing array, record " + (i + 1) + " of " + txs.length + ".");
                        logger.debug(txs[i].toString());

                        destinationUnavailableCount = 0;
                        do {
                            try {
                                jmsSender.sendMessage(txs[i].toString());
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

    private String getUrl(Status oldStatus, Status newStatus, Carrier carrier, int count) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(restUrl);
        builder.queryParam("oldStatus", oldStatus.getName());
        builder.queryParam("newStatus", newStatus.getName());
        builder.queryParam("carrier", carrier.getName());
        builder.queryParam("count", count);

        return builder.build().toUriString();
    }

}
