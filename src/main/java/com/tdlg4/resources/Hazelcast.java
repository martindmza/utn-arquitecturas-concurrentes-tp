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
import com.tdlg4.services.Authenticator;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.SharedData;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import static java.lang.Integer.parseInt;

/**
 *
 * @author pgm_1
 */
public class Hazelcast extends AbstractVerticle implements Handler<Message<JsonObject>>{
    private static final Logger LOGGER = LoggerFactory.getLogger(Hazelcast.class);
    //public static Vertx vertx;
    public static final String SERVICE_ADDRESS = "hazelcast-address";
    public JsonArray tasks=new JsonArray();
    Config hazelcastConfig = ConfigUtil.loadConfig();

    ClusterManager mgr = new HazelcastClusterManager(hazelcastConfig);

    VertxOptions options = new VertxOptions().setClusterManager(mgr);
    SharedDataService sds;
    Authenticator auth;
   
    
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
            auth= new Authenticator();
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
        String action=request.getString("action");
        switch (action) {
        case "saveList": 
            //"__vertx.haInfo"
            
            sds.SaveAsyncContextJson("vertx.list", data.getInteger("id"), data, resSave -> {
                if (resSave.succeeded()) {
                    LOGGER.info(String.format("Save context OK"));
                    event.reply(Json.encode("OK"));
                }else {
                    LOGGER.info(String.format("Save context Error"));
                    event.reply(Json.encode("error"));
                }
            });
        break;
        case "get":
            try{
                LOGGER.info("key id"+request.getInteger("id"));
            sds.GetAsyncContextJson("vertx.list", request.getInteger("id"), resGet -> {
                if (resGet.succeeded()) {
                    LOGGER.info(String.format("Get context OK"));
                    LOGGER.info(resGet.result());
                    event.reply(resGet.result());
                }else {
                    LOGGER.info(String.format("GET context list Error"));
                    event.fail(0, "error");
                }
            });
            }
            catch(Exception ex)
            {
                LOGGER.info(String.format("Get context Error"));
                event.fail(0, "error");
            }
        break;
        case "getAllLists":
            try{
            sds.GetAsyncAllListsContextJson("vertx.list", resGet -> {
                if (resGet.succeeded()) {
                    LOGGER.info(String.format("Get all list context OK"));
                    LOGGER.info(resGet.result());
                    event.reply(resGet.result());
                }else {
                    LOGGER.info(String.format("GET context all list Error"));
                    event.fail(0, "error");
                }
            });
            }
            catch(Exception ex)
            {
                LOGGER.info(String.format("Get context all list Error"));
                event.fail(0, "error");
            }
        break;
        case "getTask":
            try{
                
            sds.GetAsyncContextJson("vertx.task", request.getInteger("id"), resGet -> {
                if (resGet.succeeded()) {
                    LOGGER.info(String.format("Get task context OK"));
                    LOGGER.info(resGet.result());
                    event.reply(resGet.result());
                                    
                }else {
                    LOGGER.info(String.format("Get context Error"));
                    event.fail(0, "error");
                }
            });
            }
            catch(Exception ex)
            {
                LOGGER.info(String.format("Get context Error"));
                event.fail(0, "error");
            }
        break;
        case "saveTask":
            sds.SaveAsyncContextJson("vertx.task", data.getInteger("id"), data, resSaveTask -> {
                if (resSaveTask.succeeded()) {
                    LOGGER.info(String.format("Save context task OK"));
                    sds.GetAsyncContextJson("vertx.list", data.getInteger("list"), resGet -> {
                        if (resGet.succeeded()) {
                            LOGGER.info(String.format("Get context list OK"));
                            LOGGER.info(resGet.result());
                            JsonObject list=resGet.result();
                            var arr=list.getJsonArray("task_ids");
                            LOGGER.info("Id task: "+ data.getInteger("id"));
                            boolean existId = arr.stream().anyMatch(i -> i.equals(data.getInteger("id")));
                            LOGGER.info("Id task exist: "+ existId);
                            if(!existId) {
                            	arr.add(data.getInteger("id"));
                            }
                            LOGGER.info(arr);
                            list.remove("task_ids");
                            list.put("task_ids", arr);
                            sds.SaveAsyncContextJson("vertx.list", data.getInteger("list"), list, resSaveList -> {
                                if (resSaveList.succeeded()) {
                                    LOGGER.info(String.format("Save task context OK"));
                                    event.reply(Json.encode("OK"));
                                }else {
                                    LOGGER.info(String.format("Save task context Error"));
                                    event.fail(0, "error");
                                }
                            });
                        }else {
                            LOGGER.info(String.format("save task Error"));
                            event.fail(0, "error");
                        }
                    });
                }else {
                    LOGGER.info(String.format("Save task Error"));
                    event.fail(0, "error");
                }
            });
            
        break;
        case "delList":
            LOGGER.info("case delList");
            sds.GetAsyncContextJson("vertx.list", request.getInteger("id"), resGet -> {
                if (resGet.succeeded()) {
                    JsonObject list=resGet.result();
                    var arr=list.getJsonArray("task_ids");
                    arr.forEach(m -> {
                        sds.RemoveAsyncContextJson("vertx.task", parseInt(m.toString()), resultHandler ->{ 
                            if (resultHandler.succeeded()) {
                                LOGGER.info("Del task id:"+m.toString());
                            }
                        });
                    });
                }
            });
            sds.RemoveAsyncContextJson("vertx.list", request.getInteger("id"), resDelList -> {
                JsonObject resp=new JsonObject();
                if (resDelList.succeeded()) {
                    resp.put("result", true);   
                }
                else
                {
                    resp.put("result", false);
                }
                event.reply(resp);
            });
            
            break;
        case "delTask":
            JsonObject resp=new JsonObject();
            sds.GetAsyncContextJson("vertx.task", request.getInteger("id"), resGet -> {
                if (resGet.succeeded()) {
                    LOGGER.info(String.format("Get task context OK"));
                    LOGGER.info(resGet.result());
                    JsonObject jTask = resGet.result();
                    var idList=jTask.getInteger("list");
                    sds.GetAsyncContextJson("vertx.list", idList, resGetL -> {
                        if (resGetL.succeeded()) {
                            LOGGER.info(String.format("Get context list OK"));
                            LOGGER.info(resGetL.result());
                            JsonObject list=resGetL.result();
                            list.getJsonArray("task_ids").remove(request.getString("id"));
                            sds.SaveAsyncContextJson("vertx.list", idList, list, resSaveList -> {
                                if (resSaveList.succeeded()) {
                                    LOGGER.info(String.format("Save task context OK"));
                                    sds.RemoveAsyncContextJson("vertx.task", request.getInteger("id"), resDelTask -> {
                                        if (resDelTask.succeeded()) {
                                            resp.put("result", true); 
                                        }
                                        else
                                        {
                                            resp.put("result", false);
                                        }
                                        event.reply(resp);
                                    });
                                }else {
                                    LOGGER.info(String.format("Del task context Error"));
                                    event.fail(0, "error");
                                }
                            });
                        }else {
                            LOGGER.info(String.format("Del task Error"));
                            event.fail(0, "error");
                        }
                    });
                }
                else
                {
                    LOGGER.info(String.format("del task Error"));
                            event.fail(0, "error");
                }
            });       
            break;
        case "validate":
            auth.verifyToken(request.getString("token"), handler -> {
                if (handler.succeeded()) {
                    JsonObject respAuth=handler.result();
                    event.reply(respAuth);
                }
                else {
                    event.fail(0, "error");
                }
            });
            break;
        case "login":
            String token=auth.generateToken(request.getString("user"), request.getString("password"));
            if (token.isEmpty())
                event.fail(0, "error");
            else
            {
                JsonObject respToken=new JsonObject();
                respToken.put("result", true);
                respToken.put("token", token);
                event.reply(respToken);
            }
            break;
        }
    }
    

}
