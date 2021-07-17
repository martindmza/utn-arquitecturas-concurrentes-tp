/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tdlg4.services;

import com.tdlg4.resources.HttpServer;
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
        try{
            LOGGER.info("user: "+username + " password:"+password);
            String token="";
            if ("usuario".equals(username) && "tdlg4".equals(password)) {
                LOGGER.info("entro if");
                token = provider.generateToken(new JsonObject().put("sub", "paulo"));
                LOGGER.info("token "+token);
                
            }
            return token;
        }
        catch(Exception ex)
        {
            return ex.getMessage();
        }
        
    }
    public boolean verifyToken(String token)
    {
        provider.authenticate(
        new JsonObject()
          .put("token", token)
          .put("options", new JsonObject()
            .put("ignoreExpiration", true)))
        .onSuccess(user -> System.out.println("User: " + user.principal())
        )
        .onFailure(err -> {
          //
        });
        return true;
    }
}
