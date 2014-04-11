/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mongocom.miscellaneous;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thiago
 */
//TODO: redo, rethink
public class ConnectDB {

    private MongoClient client;

    private ConnectDB() {
        try {
            client = new MongoClient();
        } catch (UnknownHostException ex) {
            Logger.getLogger(ConnectDB.class.getName()).log(Level.SEVERE, "Local server is not running.", ex);
        }
        Logger.getLogger(ConnectDB.class.getName()).log(Level.INFO, "Connected to {0}", client.getAddress());

    }

    private ConnectDB(String host, int port) {
        try {
            if (port == 0) {
                client = new MongoClient(host);
            } else {
                client = new MongoClient(host, port);
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
