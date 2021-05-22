package com.tdlg4;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import com.tdlg4.resources.HttpServer;

public class MainVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        //Simple example
        /*vertx.createHttpServer().requestHandler(req -> {
        req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello from Vert.x!");
        }).listen(8888, http -> {
            if (http.succeeded()) {
                startPromise.complete();
                System.out.println("HTTP server started on port 8888");
            } else {
                startPromise.fail(http.cause());
            }
        });*/
        super.start();
        deployVerticleServer();
        
        //Ejemplo para levantar 2 verticles. 
        //Espera a levantar el primero sin error y luego el segundo. 
        /*deployVerticleServer().compose(i -> deployVerticle2()).onComplete(ar -> {
            if (ar.succeeded()) {
                System.out.println("Both operations completed");
            }
            else {
                ar.cause()
                .printStackTrace();
            }
        });*/
    }
    private Future<Void> deployVerticleServer() {
        Promise<Void> startPromise = Promise.promise();
        DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setConfig(config());
        
        // deploymentOptions.setInstances(1);
        //deploymentOptions.setInstances(Runtime.getRuntime().availableProcessors());
                              
        vertx.deployVerticle(HttpServer.class.getName(), deploymentOptions, ar -> {
                if (ar.succeeded()) {
                        LOGGER.info(String.format("Deployment HttpServer verticle ok"));
                        startPromise.complete();
                } else {
                        LOGGER.info(String.format("Deployment verticle ko HttpServer " + ar.cause()));
                        startPromise.fail(ar.cause());
                }
        });
        return startPromise.future();

    }
}
