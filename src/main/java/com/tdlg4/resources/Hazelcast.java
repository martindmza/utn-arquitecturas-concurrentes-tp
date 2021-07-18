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
import io.vertx.core.json.JsonArray;

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

    void getTaskInOrder(Message<JsonObject> event,JsonObject result,JsonArray listResult,JsonArray ids,int ind,int size){
        String taskId=Integer.toString(ids.getInteger(ind));
        sds.GetAsyncContextJson("vertx.task", taskId, resGet -> {
            if (resGet.succeeded()) {
                LOGGER.info(String.format("Get task "+taskId+" OK"));
                LOGGER.info(resGet.result());
                
                listResult.add(resGet.result());
                //ind++;
                if(ind+1==size){
                    result.put("tasks",listResult);
                    event.reply(result);
                }else{
                    getTaskInOrder(event,result,listResult,ids,ind+1,size);
                }                    
            }else {
                LOGGER.info(String.format("Save context Error"));
                event.fail(0, "error");
            }
        });
    }    

    @Override
    public void handle(Message<JsonObject> event) {
        JsonObject request = event.body();
        JsonObject data=request.getJsonObject("data");
        if ((request.getString("action")).equals("saveList"))
        {            //"__vertx.haInfo"
            sds.SaveAsyncContextJson("vertx.list", data.getString("id"), data, resSave -> {
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
            JsonObject resp=new JsonObject();
            try{
                
            sds.GetAsyncContextJson("vertx.list", request.getString("id"), resGet -> {
                if (resGet.succeeded()) {
                    LOGGER.info(String.format("Get context OK"));
                    LOGGER.info(resGet.result());
                    //JsonObject list=resGet.result();
                    //var arr=list.getJsonArray("task_ids");
                    /*
                    sds.GetAsyncArrayContextJson("vertx.task", arr, resGetTask -> {
                        if (resGet.succeeded()) {
                            LOGGER.info("resGetTask: "+resGetTask.result());
                            list.put("tasks", resGetTask.result());
                            LOGGER.info("lista final "+ list);
                            event.reply(list);
                        }
                        else
                        {
                            LOGGER.info(String.format("Get context list Error 1"));
                            event.fail(0, "error");
                        }
                    });
                    */
                    //int size=arr.size();
                    //getTaskInOrder(event,list,new JsonArray(),arr,0,size);
                    event.reply(resGet.result());
                }else {
                    LOGGER.info(String.format("GET context list Error 2"));
                    event.fail(0, "error");
                }
            });
            }
            catch(Exception ex)
            {
                LOGGER.info(String.format("Save context Error"));
                event.fail(0, "error");
            }
        }
        else if ((request.getString("action")).equals("getTask"))
        {
            JsonObject resp=new JsonObject();
            try{
                
            sds.GetAsyncContextJson("vertx.task", request.getString("id"), resGet -> {
                if (resGet.succeeded()) {
                    LOGGER.info(String.format("Get task context OK"));
                    LOGGER.info(resGet.result());
                    event.reply(resGet.result());
                                    
                }else {
                    LOGGER.info(String.format("Save context Error"));
                    event.fail(0, "error");
                }
            });
            }
            catch(Exception ex)
            {
                LOGGER.info(String.format("Save context Error"));
                event.fail(0, "error");
            }
        }
        else if((request.getString("action")).equals("saveTask"))
        {
            sds.SaveAsyncContextJson("vertx.task", data.getString("id"), data, resSaveTask -> {
                if (resSaveTask.succeeded()) {
                    LOGGER.info(String.format("Save context task OK"));
                    sds.GetAsyncContextJson("vertx.list", data.getString("list"), resGet -> {
                        if (resGet.succeeded()) {
                            LOGGER.info(String.format("Get context OK"));
                            LOGGER.info(resGet.result());
                            JsonObject list=resGet.result();
                            var arr=list.getJsonArray("task_ids");
                            LOGGER.info("Id task: "+ data.getString("id"));
                            boolean existId = arr.stream().anyMatch(i -> i.equals(data.getString("id")));
                            LOGGER.info("Id task exist: "+ existId);
                            if(!existId) {
                            	
                            	arr.add(data.getString("id"));
                            }
                            LOGGER.info(arr);
                            list.remove("task_ids");
                            list.put("task_ids", arr);
                            sds.SaveAsyncContextJson("vertx.list", data.getString("list"), list, resSaveList -> {
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
            
        }
    }

}
