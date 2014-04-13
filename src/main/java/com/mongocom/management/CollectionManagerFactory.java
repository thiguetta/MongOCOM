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
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Thiago da Silva Gonzaga <thiagosg@sjrp.unesp.br>
 */
public final class CollectionManagerFactory {

    private static MongoClient client;

    private static final Logger logger = Logger.getLogger("com.mongocom.management.CollectionManagerFactory");

    public static CollectionManager createCollectionManager() {
        try {
            client = new MongoClient();
        } catch (UnknownHostException ex) {
            logger.log(Level.SEVERE, "Local server is not running.", ex);
        }
        logger.log(Level.INFO, "Connected to {0}", client.getAddress());
        return new CollectionManager(client);
    }

    public static CollectionManager createCollectionManager(String host, int port) {
        try {
            if (port == 0) {
                client = new MongoClient(host);
            } else {
                client = new MongoClient(host, port);
            }
        } catch (UnknownHostException ex) {
            logger.log(Level.SEVERE, "Server not found at {0}:{1}.", new Object[]{host, port});
        }
        logger.log(Level.INFO, "Connected to {0}", client.getAddress());
        return new CollectionManager(client);
    }

    public static CollectionManager createCollectionManager(String host) {
        return createCollectionManager(host, 0);
    }
}
