package com.percyvega.revenueassurance.rest2jms.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Hashtable;

/**
 * Created by pevega on 1/21/2015.
 */
@Component
public class JMSSender {

    private static final Logger logger = LoggerFactory.getLogger(JMSSender.class);

    private InitialContext initialContext;
    private QueueConnectionFactory queueConnectionFactory;
    private QueueConnection queueConnection;
    private QueueSession queueSession;
    private Queue queue;
    private QueueSender qsndr;
    private TextMessage textMessage;

    private static String qcfName;
    @Value("${jms.qcfName}")
    public void setQcfName(String qcfName) {
        this.qcfName = qcfName;
    }

    private static String queueName;
    @Value("${jms.queueName}")
    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    private static String providerUrl;
    @Value("${jms.providerUrl}")
    public void setProviderUrl(String providerUrl) {
        this.providerUrl = providerUrl;
    }

    private static String icfName;
    @Value("${jms.icfName}")
    public void setIcfName(String icfName) {
        this.icfName = icfName;
    }

    @Override
    public String toString() {
        return "JMSSender [icfName=" + icfName + ", providerUrl=" + providerUrl + ", qcfName=" + qcfName + ", queueName=" + queueName + "]";
    }

    private boolean initialized = false;

    public void init() {
        initialized = true;
        try {
            Hashtable properties = new Hashtable();
            properties.put(Context.INITIAL_CONTEXT_FACTORY, icfName);
            properties.put(Context.PROVIDER_URL, providerUrl);
//            properties.put(Context.SECURITY_PRINCIPAL, "username");                   // username
//            properties.put(Context.SECURITY_CREDENTIALS, "password");                 // password

            initialContext = new InitialContext(properties);
//            logger.debug("Got InitialContext " + initialContext.toString());

            queueConnectionFactory = (QueueConnectionFactory) initialContext.lookup(qcfName);
//            logger.debug("Got QueueConnectionFactory " + queueConnectionFactory.toString());

            queueConnection = queueConnectionFactory.createQueueConnection();
//            logger.debug("Got QueueConnection " + queueConnection.toString());

            queueSession = queueConnection.createQueueSession(false, 0);
//            logger.debug("Got QueueSession " + queueSession.toString());

            queue = (Queue) initialContext.lookup(queueName);
//            logger.debug("Got Queue " + queue.toString());

            qsndr = queueSession.createSender(queue);
//            logger.debug("Got QueueSender " + qsndr.toString());

            textMessage = queueSession.createTextMessage();
//            logger.debug("Got TextMessage " + textMessage.toString());

        } catch (Exception e) {
            e.printStackTrace(System.err);
            logger.warn(e.toString());
        }
    }

    public void sendMessage(String messageText) throws JMSException {
        if(!initialized)
            init();

        textMessage.setText(messageText);

        qsndr.send(textMessage);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        qsndr.close();
        queueSession.close();
        queueConnection.close();
    }

    public static void main(String args[]) throws JMSException {
        JMSSender jmsSender = new JMSSender();
        for (int i = 1; i <= 10; i++)
            jmsSender.sendMessage("This is my JMS message #" + i + "!");
    }
}