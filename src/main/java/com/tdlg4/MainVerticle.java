package com.tdlg4;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import com.tdlg4.resources.HttpServer;
import com.tdlg4.resources.Hazelcast;

public class MainVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        super.start();
        //deployVerticleHazelcast();
        //deployVerticleServer();
        
        //Ejemplo para levantar 2 verticles. 
        //Espera a levantar el primero sin error y luego el segundo. 
        deployVerticleServer().compose(i -> deployVerticleHazelcast()).onComplete(ar -> {
            if (ar.succeeded()) {
                System.out.println("Both operations completed");
            }
            else {
                ar.cause()
                .printStackTrace();
            }
        });
    }
    private Future<Void> deployVerticleServer() {
        Promise<Void> startPromise = Promise.promise();
        DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setConfig(config());
        
        // deploymentOptions.setInstances(1);
        //deploymentOptions.setInstances(Runtime.getRuntime().availableProcessors()*2);
                              
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
    private Future<Void> deployVerticleHazelcast() {
        Promise<Void> startPromise = Promise.promise();
        DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setConfig(config());
                              
        vertx.deployVerticle(Hazelcast.class.getName(), deploymentOptions, ar -> {
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
