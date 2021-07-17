/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tdlg4.services;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.*;
import io.vertx.core.json.JsonObject;
import com.tdlg4.resources.Hazelcast;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;

/**
 *
 * @author pgm_1
 */
public class SharedDataService {
    
    //private static Vertx vertx = Vertx.vertx();
    private static Vertx vertx = Vertx.currentContext().owner();
    private static final Logger LOGGER = LoggerFactory.getLogger(Hazelcast.class);
    
    public void GetAsyncArrayContextJson(String newMap, JsonArray arr, Handler<AsyncResult<JsonArray>> resultHandler) {
        SharedData sharedData = vertx.sharedData();
        JsonArray jArr=new JsonArray();
        sharedData.<String, JsonObject>getClusterWideMap(newMap, res -> {
            if (res.succeeded()) {
                AsyncMap<String, JsonObject> map = res.result();
                for(int i=0; i<arr.size(); i++){
                    LOGGER.info("GetString: "+i+" "+arr.getString(i));
                    map.get(arr.getString(i), resGet -> {
                    if (resGet.succeeded()) {
                        // Successfully got the value
                        JsonObject val = resGet.result();
                        LOGGER.info("arrayTask: "+val);
                        jArr.add(val);
                        
                    } else {
                      // Something went wrong!
                      LOGGER.info("no existe "+newMap);
                      resultHandler.handle(Future.failedFuture(resGet.cause()));
                    }
                  });
                }
                LOGGER.info("jArr "+jArr);
                resultHandler.handle(Future.succeededFuture(jArr));

            } else {
              // Something went wrong!
              resultHandler.handle(Future.failedFuture(res.cause()));
            }
        });
    }
    
    
    /*Async*/
    public void GetAsyncContextJson(String newMap, String key, Handler<AsyncResult<JsonObject>> resultHandler) {
        SharedData sharedData = vertx.sharedData();

        sharedData.<String, JsonObject>getClusterWideMap(newMap, res -> {
            if (res.succeeded()) {
                AsyncMap<String, JsonObject> map = res.result();
                map.get(key, resGet -> {
                if (resGet.succeeded()) {
                   LOGGER.info("existe "+key);
                  // Successfully got the value
                  JsonObject val = resGet.result();
                  LOGGER.info("val resultado getasync "+resGet.result());
                  if(resGet.result()!=null)
                    resultHandler.handle(Future.succeededFuture(val));
                  else
                    resultHandler.handle(Future.failedFuture(resGet.cause()));
                } else {
                  // Something went wrong!
                  LOGGER.info("no existe "+newMap+" "+key);
                  resultHandler.handle(Future.failedFuture(resGet.cause()));
                }
              });

            } else {
              // Something went wrong!
              resultHandler.handle(Future.failedFuture(res.cause()));
            }
        });
    }

    public void SaveAsyncContextJson(String newMap, String key, JsonObject json, Handler<AsyncResult<JsonObject>> resultHandler) {
        SharedData sharedData = vertx.sharedData();
        sharedData.<String, JsonObject>getClusterWideMap(newMap, res -> {
            if (res.succeeded()) {
                AsyncMap<String, JsonObject> map = res.result();
                //map.putIfAbsent(key, json);
                map.put(key, json);
                resultHandler.handle(Future.succeededFuture());
            } else {
              resultHandler.handle(Future.failedFuture(res.cause()));
            }
        });
    }
    
    
    public void RemoveAsyncContextJson(String newMap, String key, JsonObject json, Handler<AsyncResult<JsonObject>> resultHandler) {
        SharedData sharedData = vertx.sharedData();
        sharedData.<String, JsonObject>getClusterWideMap(newMap, res -> {
            if (res.succeeded()) {
                AsyncMap<String, JsonObject> map = res.result();
                map.remove(key);
                resultHandler.handle(Future.succeededFuture());
            } else {
              resultHandler.handle(Future.failedFuture(res.cause()));
            }
        });
    }

    public void counter(Vertx vertx) {
        SharedData sharedData = vertx.sharedData();

        sharedData.getCounter("__vertx.haInfo", res -> {
          if (res.succeeded()) {
            Counter counter = res.result();
          } else {
            // Something went wrong!
          }
        });
    }
}

  

    
    
