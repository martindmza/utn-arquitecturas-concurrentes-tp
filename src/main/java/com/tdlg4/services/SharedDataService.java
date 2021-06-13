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

/**
 *
 * @author pgm_1
 */
public class SharedDataService {
    
    //private static Vertx vertx = Vertx.vertx();
    private static Vertx vertx = Vertx.currentContext().owner();
    //private static Vertx vertx=Hazelcast.vertx;
    
    /*Async*/
    public void GetAsyncContextJson(String newMap, String key, Handler<AsyncResult<JsonObject>> resultHandler) {
        SharedData sharedData = vertx.sharedData();

        sharedData.<String, JsonObject>getClusterWideMap(newMap, res -> {
            if (res.succeeded()) {
                AsyncMap<String, JsonObject> map = res.result();
                map.get(key, resGet -> {
                if (resGet.succeeded()) {
                  // Successfully got the value
                  JsonObject val = resGet.result();
                  resultHandler.handle(Future.succeededFuture(val)); 
                } else {
                  // Something went wrong!
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
                var r=map.put(key, json);
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

  

    
    
