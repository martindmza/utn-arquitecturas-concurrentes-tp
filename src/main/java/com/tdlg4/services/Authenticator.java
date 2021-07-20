/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tdlg4.services;

import com.tdlg4.resources.HttpServer;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;

/**
 *
 * @author pgm_1
 */
public class Authenticator {
    
    private static Vertx vertx = Vertx.currentContext().owner();
    private static final Logger LOGGER = LoggerFactory.getLogger(Authenticator.class);
    
    JWTAuthOptions config = new JWTAuthOptions()
     .setKeyStore(new KeyStoreOptions()
        .setType("jceks")
        .setPath("keystore.jceks")
        .setPassword("secret"));

    JWTAuth provider = JWTAuth.create(vertx, config);

    public String generateToken (String username, String password)
    {
        try
        {
            LOGGER.info("user: "+username + " password:"+password);
            String token="";
            if ("usuario1".equals(username) && "tdlg4".equals(password) ||
                "usuario2".equals(username) && "tdlg4".equals(password) ||
                "usuario3".equals(username) && "tdlg4".equals(password) ||
                "usuario4".equals(username) && "tdlg4".equals(password)) {
                token = provider.generateToken(new JsonObject().put("user", username));
                
            }
            /*else if ("usuario3".equals(username) && "tdlg4".equals(password) ||
                "usuario4".equals(username) && "tdlg4".equals(password)) {
                token = provider.generateToken(new JsonObject().put("user", username));
            }*/
            return token;
        }
        catch(Exception ex)
        {
            return ""; //ex.getMessage();
        }
        
    }
    public void verifyToken(String token, Handler<AsyncResult<JsonObject>> resultHandler)
    {
        LOGGER.info("Verifying token "+token);
        provider.authenticate(
        new JsonObject()
          .put("token", token)
          .put("options", new JsonObject()
          .put("ignoreExpiration", true)))
        .onSuccess(user -> { 
            LOGGER.info("authorization: " + user.principal());
            resultHandler.handle(Future.succeededFuture(user.principal())); 
        })
        .onFailure(err -> {
            resultHandler.handle(Future.failedFuture("error"));
        });
        
    }
}
