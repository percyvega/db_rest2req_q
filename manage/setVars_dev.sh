#!/bin/bash

APP_NAME=db_rest2req_q
export APP_NAME

sourceUrl=http://localhost:8181/actions/findAndUpdate
export sourceUrl
jms_qcfName=jms/myConnectionFactory
export jms_qcfName
jms_providerUrl=t3://localhost:8001
export jms_providerUrl
jms_icfName=weblogic.jndi.WLInitialContextFactory
export jms_icfName
jms_destinationQueueName=jms/percyvegaQueue
export jms_destinationQueueName
