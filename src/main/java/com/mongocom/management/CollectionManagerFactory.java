/*
 * Copyright 2014 Thiago da Silva Gonzaga <thiagosg@sjrp.unesp.br>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mongocom.management;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Thiago da Silva Gonzaga <thiagosg@sjrp.unesp.br>
 */
public final class CollectionManagerFactory {

    private static MongoClient client;

    private static final Logger logger = Logger.getLogger("CollectionManagerFactory");

    public static CollectionManager createCollectionManager() {
        return createBaseCollectionManager("", 0, "", "", "");
    }

    public static CollectionManager createCollectionManager(String host) {
        return createBaseCollectionManager(host, 0, "", "", "");
    }

    public static CollectionManager createCollectionManager(String host, int port) {
        return createBaseCollectionManager(host, port, "", "", "");
    }

    public static CollectionManager createCollectionManager(String dbName, String user, String password) {
        return createBaseCollectionManager("", 0, dbName, user, password);
    }

    public static CollectionManager createCollectionManager(String host, int port, String dbName, String user, String password) {
        return createBaseCollectionManager(host, port, dbName, user, password);
    }

    private static CollectionManager createBaseCollectionManager(String host, int port, String dbName, String user, String password) {
        try {
            if ("".equals(host)) {
                client = new MongoClient();
            } else {
                if (port == 0) {
                    client = new MongoClient(host);
                } else {
                    client = new MongoClient(host, port);
                }
            }
            logger.log(Level.INFO, "Connected to {0}", client.getAddress());
            if ("".equals(user)) {
                return new CollectionManager(client, dbName);
            }
            return new CollectionManager(client, dbName, user, password);
        } catch (MongoException | UnknownHostException ex) {
            logger.log(Level.SEVERE, "Unable to connect to a mongoDB instance, maybe it is not running or you do not have the right permission: ", ex);
        }
        return null;
    }
}
