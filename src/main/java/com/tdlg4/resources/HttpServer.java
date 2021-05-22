/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tdlg4.resources;

import com.tdlg4.services.SharedDataService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import java.io.File;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import java.util.HashSet;
import java.util.Set;
import io.vertx.core.json.Json;

/**
 *
 * @author pgm_1
 */
public class HttpServer extends AbstractVerticle{
    String sHtml="";
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);
    private static final String API_PATH = "app";
    private static final String API_PATH_MEM = "mem";
    
    /** Declaración de las rutas paths */
    private static final String API_POST = String.format("/%s", API_PATH);
    private static final String API_GET_MEM = String.format("/%s/:id", API_PATH_MEM);
    private static final String API_POST_MEM = String.format("/%s", API_PATH_MEM);
    
    public JsonObject configJson= new JsonObject();
    private final SharedDataService sds= new SharedDataService();
    
    /*cambiar luego por configuración */
    int port=7777;
    String host="127.0.0.1";
    /**************************/
    
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        
        try{
            /*agregar lectura configuracion*/
        }
        catch(Exception e)
        {
            LOGGER.info("fallo lectura config");
        }
        
        Router router = Router.router(vertx);
        
        /* CORS */
        enableCorsSupport(router);

        /* Permite el body handler para obtener el payload */
        router.route().handler(BodyHandler.create());
        
        /* Rutas */
        router.post(API_POST).handler(this::apiPost);
        router.get(API_GET_MEM).handler(this::apiGetMem);
        router.post(API_POST_MEM).handler(this::apiPostMem);
        
        /* INFO del Servicio */
        sHtml="<h1>Bienvenidos al servicio TO DO LIST <h1>"+"<br>";
        sHtml+="<h3>Para mas informaci&oacute;n vaya a /info";
        router.get("/")
            .handler(routingContext -> routingContext.response()
                .putHeader("content-type", "text/html")
                .end(sHtml));
        
        router.get("/info").handler(routingContext -> {
            String conf = "";
            String input = "";
            String output ="";
            String path = "";
            File fullPath=new File(input);
                routingContext.response().setStatusCode(200)
                    .putHeader("content-type", "text/html")
                    .end("pathConfig:"+path+"<br>Conf:"+conf+"<br>"+"input:" +input+"<br>output:"+output+"<br> Errores: ");//+service.findDataByConfigName("mensaje")+service.findDataByConfigName("mensaje2"));
            });
        
        /* Crea servidor http */
        vertx.createHttpServer().requestHandler(router).listen(port, host, ar -> {
            if (ar.succeeded()) {
                    startPromise.complete();
                    LOGGER.info(String.format("Our http server is running at %d", port));
            } else {
                    startPromise.fail(ar.cause());
                    LOGGER.info(String.format("Our http server is not running !!!!!!!! at %d", port));

            }
        });
    }   
    
    private void apiGetMem(RoutingContext context) {
        /* Test para Consulta objeto guardado en memoria local recibiendo key por parametro */
        String id = context.request().getParam("id");
        JsonObject json= new JsonObject();
        //var a=sds.localMapGetContextJsonRequest(vertx, id); consulta sincrónica a mem local
        sds.GetAsyncContextJson("map",id, resGet ->{
            if (resGet.succeeded()) {
                JsonObject a=resGet.result();
                context.response().setStatusCode(200).putHeader("content-type", "application/json").end(Json.encode(a));
            }
            else
            {
                context.response().setStatusCode(200).putHeader("content-type", "application/json").end(Json.encode(resGet.cause()));
            }
        });
        
    }
    
    private void apiPostMem (RoutingContext context) {
        /* Test para guardar objeto en memoria local */
        JsonObject request=new JsonObject (context.getBodyAsString());
        LOGGER.info("request: {0}" + request.toString());
        sds.SaveAsyncContextJson("map",request.getString("id"), request, resSave -> {
            if (resSave.succeeded()) {
                LOGGER.info(String.format("Save context OK"));
                context.response().setStatusCode(200).putHeader("content-type", "application/json").end(Json.encode("OK"));
            }else {
                LOGGER.info(String.format("Save context Error"));
                context.response().setStatusCode(200).putHeader("content-type", "application/json").end(Json.encode("error"));
            }
        });
    }
    
    private void apiPost (RoutingContext context) {
        /* Parseamos a nuesto objeto de entrada */
        JsonObject request=new JsonObject (context.getBodyAsString());
        LOGGER.info("request recv: "+ request.toString());     
        context.response().setStatusCode(200).putHeader("content-type", "application/json").end("OK");
    }
    
    
    protected void enableCorsSupport(Router router) {
        Set<String> allowHeaders = new HashSet<>();
        allowHeaders.add("x-requested-with");
        allowHeaders.add("Access-Control-Allow-Origin");
        allowHeaders.add("origin");
        allowHeaders.add("Content-Type");
        allowHeaders.add("accept");
        Set<HttpMethod> allowMethods = new HashSet<>();
        allowMethods.add(HttpMethod.GET);
        allowMethods.add(HttpMethod.POST);
        allowMethods.add(HttpMethod.DELETE);
        allowMethods.add(HttpMethod.PATCH);

        router.route().handler(CorsHandler.create("*").allowedHeaders(allowHeaders).allowedMethods(allowMethods));
    }

    /**
     * Enable simple heartbeat check mechanism via HTTP.
     *
     * @param router
     *            router instance
     * @param config
     *            configuration object
     */
    protected void enableHeartbeatCheck(Router router, JsonObject config) {
        router.get(config.getString("heartbeat.path", "/ping")).handler(context -> {
                JsonObject checkResult = new JsonObject().put("status", "UP");
                context.response().end(checkResult.encode());
        });
    }   
    
}
