package com.percyvega.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.percyvega.db_rest2req_q.model.IntergateTransaction;

/**
 * Created by pevega on 3/25/2015.
 */
public abstract class JacksonUtil {

    public static String toJson(Object o) {
        String jsonString = null;

        try {
            jsonString = new ObjectMapper().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return jsonString;
    }

    public static void main(String[] args) throws JsonProcessingException {
        IntergateTransaction intergateTransaction = new IntergateTransaction("9547325664");
        System.out.println(toJson(intergateTransaction));
    }

}
