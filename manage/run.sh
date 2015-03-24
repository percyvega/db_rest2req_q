#!/bin/bash

cd ..

nohup /opt/bea/bea1033/jdk1.6.0_20/bin/java \
-Ddb_rest2req_q \
-jar target/db_rest2req_q-1.0-SNAPSHOT.jar &
