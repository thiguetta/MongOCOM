/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mongocom;

import com.mongodb.DB;
import com.mongodb.Mongo;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thiago
 */
public class ConnectDB {

    private Mongo client;

    private ConnectDB() {
        try {
            client = new Mongo();
        } catch (UnknownHostException ex) {
            Logger.getLogger(ConnectDB.class.getName()).log(Level.SEVERE, "Local server is not running.", ex);
        }
        Logger.getLogger(ConnectDB.class.getName()).log(Level.INFO, "Connected to {0}", client.getAddress());

    }
    
    private ConnectDB(String host, int port) {
        try {
            if (port == 0) {
                client = new Mongo(host);
            } else {
                client = new Mongo(host, port);
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(ConnectDB.class.getName()).log(Level.SEVERE, "Server not found at {0}:{1}.", new Object[]{host, port});
        }
        Logger.getLogger(ConnectDB.class.getName()).log(Level.INFO, "Connected to {0}", client.getAddress());
    }

    public static ConnectDB connectLocal() {
        return new ConnectDB();
    }

    public static ConnectDB connectToServer(String host, int port) {
        return new ConnectDB(host, port);
    }

    public DB getDB(String dbname) {
        return client.getDB(dbname);
    }

}
