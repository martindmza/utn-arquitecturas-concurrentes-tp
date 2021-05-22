/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tdlg4;

import io.vertx.core.Vertx;

/**
 *
 * @author pgm_1
 */
public class VertxApplication {
    
    private VertxApplication() {
	}

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
                /* Http server verticle */
		String verticleName = MainVerticle.class.getName();
                vertx.deployVerticle(verticleName);
	}  
}

