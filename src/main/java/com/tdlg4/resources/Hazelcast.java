/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tdlg4.resources;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import io.vertx.spi.cluster.hazelcast.ClusterHealthCheck;
import io.vertx.spi.cluster.hazelcast.ConfigUtil;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.tdlg4.services.SharedDataService;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.SharedData;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;

/**
 *
 * @author pgm_1
 */
public class Hazelcast extends AbstractVerticle implements Handler<Message<JsonObject>>{
    private static final Logger LOGGER = LoggerFactory.getLogger(Hazelcast.class);
    //public static Vertx vertx;
    public static final String SERVICE_ADDRESS = "hazelcast-address";
    Config hazelcastConfig = ConfigUtil.loadConfig();

    ClusterManager mgr = new HazelcastClusterManager(hazelcastConfig);

    VertxOptions options = new VertxOptions().setClusterManager(mgr);
    SharedDataService sds;
    
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        VertxOptions options = new VertxOptions().setClusterManager(mgr);
        options.setEventBusOptions(new EventBusOptions()
                .setClusterPublicHost("192.168.56.1") );
        
        Vertx.clusteredVertx(options, res -> {
        vertx.eventBus().consumer(SERVICE_ADDRESS, this);	
          if (res.succeeded()) {
            Vertx vertx = res.result();
            
            LOGGER.info(String.format("Service  %s started.", SERVICE_ADDRESS));
            sds = new SharedDataService();
            if (vertx.isClustered()) {
                LOGGER.info(String.format("Es un cluster"));
            }
            else
            {
                LOGGER.info(String.format("NO es un cluster"));
            }
          } else {
            // failed!
          }
        });
    }
    
    @Override
    public void handle(Message<JsonObject> event) {
        JsonObject request = event.body();
        JsonObject data=request.getJsonObject("data");
        if ((request.getString("action")).equals("save"))
        {
            
            sds.SaveAsyncContextJson("__vertx.haInfo", data.getString("listName"), data, resSave -> {
                if (resSave.succeeded()) {
                    LOGGER.info(String.format("Save context OK"));
                    event.reply(Json.encode("OK"));
                }else {
                    LOGGER.info(String.format("Save context Error"));
                    event.reply(Json.encode("error"));
                }
            });
        }
        else if ((request.getString("action")).equals("get"))
        {
            sds.GetAsyncContextJson("__vertx.haInfo", request.getString("id"), resGet -> {
                if (resGet.succeeded()) {
                    LOGGER.info(String.format("Get context OK"));
                    LOGGER.info(resGet.result());
                    JsonObject a=resGet.result();
                    event.reply(a);
                }else {
                    LOGGER.info(String.format("Save context Error"));
                    event.reply(Json.encode("error"));
                }
            });
        }
    }

}
