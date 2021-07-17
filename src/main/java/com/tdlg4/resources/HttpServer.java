/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tdlg4.resources;

import com.tdlg4.services.SharedDataService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.EventBusOptions;
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
import com.tdlg4.services.Authenticator;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;

/**
 *
 * @author pgm_1
 */
public class HttpServer extends AbstractVerticle{
    String sHtml="";
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);
    private static final String API_PATH_MEM = "app/mem";
    private static final String API_PATH = "app";
    
    /** Declaración de las rutas paths */
    //private static final String API_POST = String.format("/%s", API_PATH);
    private static final String API_GET_MEM = String.format("/%s/:id", API_PATH_MEM);
    private static final String API_POST_MEM = String.format("/%s", API_PATH_MEM);
    private static final String API_PUT_MEM = String.format("/%s", API_PATH_MEM);
    private static final String API_POST_AUTH = String.format("/%s/login", API_PATH);
    private static final String API_POST_AUTH_VALIDATE = String.format("/%s/validate", API_PATH);
    
    public JsonObject configJson= new JsonObject();

    Authenticator auth= new Authenticator();
    /*cambiar luego por configuración */
    int port=7777;
    String host="0.0.0.0";
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
        //router.post(API_POST).handler(this::apiPost);
        router.get(API_GET_MEM).handler(this::apiGetMem);
        router.post(API_POST_MEM).handler(this::apiPostMem);
        router.post(API_POST_AUTH).handler(this::apiPostAuth);
        router.post(API_POST_AUTH_VALIDATE).handler(this::apiPostAuthValidate);
       //router.put(API_PUT_MEM).handler(this::apiPutMem);
        
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
        LOGGER.info("METODO GET");
        try
        {
            String id = context.request().getParam("id");
            LOGGER.info("id: {0}" +id);
            JsonObject json= new JsonObject();
            json.put("action", "get");
            json.put("id",id);
            vertx.eventBus().request(Hazelcast.SERVICE_ADDRESS, json, handler -> {
                if (handler.succeeded()) {
                        context.response().setStatusCode(200).putHeader("content-type", "application/json").end(Json.encode((JsonObject) handler.result().body()));
                } else {
                        context.response().setStatusCode(200).putHeader("content-type", "application/json").end(Json.encode("ERROR"));
                }
            });
        }
        catch(Exception ex)
        {
            context.response().setStatusCode(200).putHeader("content-type", "application/json").end(Json.encode(ex.getMessage()));
        }
        
    }
    
    private void apiPostAuth(RoutingContext context) {
        
        try {
            
            JsonObject request=new JsonObject (context.getBodyAsString());
            LOGGER.info("request recv login: "+ request.toString());   
            String token=auth.generateToken(request.getString("user"), request.getString("password"));
            JsonObject resp=new JsonObject();
            if(!token.isEmpty())
            {
                
                resp.put("result", true);
                resp.put("token", token);
                context.response().setStatusCode(200).putHeader("content-type", "application/json").end(Json.encode(resp));
            }
            else{
                resp.put("result", false);
                context.response().setStatusCode(200).putHeader("content-type", "application/json").end(Json.encode(resp));
        
            }
        }
            
        catch(Exception ex)
        {
            context.response().setStatusCode(200).putHeader("content-type", "application/json").end(Json.encode(ex.getMessage()));
        }
        
    }
    
    
    
    private void apiPostMem (RoutingContext context) {
        /* Test para guardar objeto en memoria local */
        try{
            JsonObject jList=new JsonObject();
            LOGGER.info("post request: {0}" + context.getBodyAsString());
            jList.put("action", "save");
            jList.put("data", new JsonObject (context.getBodyAsString()));
            vertx.eventBus().request(Hazelcast.SERVICE_ADDRESS, jList, handler -> {
                if (handler.succeeded()) {
                        context.response().setStatusCode(200).putHeader("content-type", "application/json").end(Json.encode("OK"));
                } else {
                        context.response().setStatusCode(200).putHeader("content-type", "application/json").end(Json.encode("ERROR"));
                }
            });
        }
        catch(Exception ex)
        {
            context.response().setStatusCode(200).putHeader("content-type", "application/json").end(Json.encode(ex.getMessage()));
        }
    }
    
    private void apiPostAuthValidate (RoutingContext context) {
        HttpServerRequest request = context.request();
        String authorization = request.headers().get(HttpHeaders.AUTHORIZATION);
        LOGGER.info("request header: "+ authorization);
        String token;
        String scheme;

        try {
                String[] parts = authorization.split(" ");
                scheme = parts[0];
                LOGGER.info("scheme: "+ scheme);
                token = parts[1];
                LOGGER.info("token: "+ token);
        } catch (ArrayIndexOutOfBoundsException e) {
                context.fail(401);
                return;
        } catch (IllegalArgumentException | NullPointerException e) {
                // IllegalArgumentException includes PatternSyntaxException
                context.fail(e);
                return;
        }

        if (scheme.equalsIgnoreCase("bearer")) {
            LOGGER.info("Entro en if scheme");
            auth.verifyToken(token, handler -> {
                if (handler.succeeded()) {
                    JsonObject respAuth=handler.result();
                    context.response().setStatusCode(200).putHeader("content-type", "application/json").end(Json.encode(respAuth));
                }
                else {
                    context.fail(401);
                }
            });

        } else {
            context.fail(401);
        }
        
        
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
