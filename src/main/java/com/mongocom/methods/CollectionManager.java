/*
 * Copyright 2014 Thiago da Silva Gonzaga <thiagosg@sjrp.unesp.br>..
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
package com.mongocom.methods;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.mongocom.annotations.Document;
import com.mongocom.annotations.ObjectId;
import com.mongodb.WriteConcern;

/**
 *
 * @author Thiago da Silva Gonzaga <thiagosg@sjrp.unesp.br>.
 */
public class CollectionManager {

    private DB db;

    //TODO: a better way to manage db connection
    public void setDB(DB db) {
        this.db = db;
    }

    public <A extends Object> long count(Class<A> collectionClass) {
        return count(collectionClass, new MongoQuery());
    }

    public <A extends Object> long count(Class<A> collectionClass, MongoQuery query) {
        long ret = 0l;
        try {
            A result = collectionClass.newInstance();
            String collName = reflectAnnotation(result);
            ret = db.getCollection(collName).count(query.getQuery());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            Logger.getLogger(CollectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public <A extends Object> List<A> find(Class<A> collectionClass) {
        return find(collectionClass, new MongoQuery());
    }

    public <A extends Object> List<A> find(Class<A> collectionClass, MongoQuery query) {
        List<A> resultSet = new ArrayList<>();
        DBCursor cursor = null;
        try {
            A obj = collectionClass.newInstance();
            String collName = reflectAnnotation(obj);
            cursor = db.getCollection(collName).find(query.getQuery(), query.getConstraits());
            if (query.getSkip() > 0) {
                cursor = cursor.skip(query.getSkip());
            }
            if (query.getLimit() > 0) {
                cursor = cursor.limit(query.getLimit());
            }
            while (cursor.hasNext()) {
                DBObject objDB = cursor.next();
                fillFields(obj, objDB);
                resultSet.add(obj);
                obj = collectionClass.newInstance();
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            Logger.getLogger(CollectionManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            cursor.close();
        }
        return resultSet;
    }

    private <A extends Object> void fillFields(A obj, DBObject objDB) throws IllegalAccessException, IllegalArgumentException, SecurityException {
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            if (objDB.get("_id") != null && f.isAnnotationPresent(ObjectId.class)) {
                f.set(obj, objDB.get("_id").toString());
            } else {
                if (objDB.get(f.getName()) == null && f.getType().isPrimitive()) {

                } else {
                    f.set(obj, objDB.get(f.getName()));
                }
            }
        }
    }

    public <A extends Object> A findOne(Class<A> collectionClass) {
        A result = null;
        try {
            result = collectionClass.newInstance();
            String collName = reflectAnnotation(result);
            DBObject obj = db.getCollection(collName).findOne();
            if (obj == null) {
                return null;
            }
            fillFields(result, obj);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException ex) {
            Logger.getLogger(CollectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public void insert(Object document) {
        try {
            BasicDBObject obj = fillDBObject(document);
            String collName = reflectAnnotation(document);
            db.getCollection(collName).insert(obj);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | SecurityException | IllegalArgumentException ex) {
            Logger.getLogger(CollectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void update(MongoQuery query, Object document) {
        update(query, document, false, false);
    }

    public void update(MongoQuery query, Object document, boolean upsert, boolean multi) {
        update(query, document, upsert, multi, WriteConcern.ACKNOWLEDGED);
    }

    public void update(MongoQuery query, Object document, boolean upsert, boolean multi, WriteConcern concern) {
        try {
            BasicDBObject obj = fillDBObject(document);
            String collName = reflectAnnotation(document);
            db.getCollection(collName).update(query.getQuery(), obj, upsert, multi, concern);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | SecurityException | IllegalArgumentException ex) {
            Logger.getLogger(CollectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateMulti(MongoQuery query, Object document) {
        update(query, document, false, true);
    }

    public void save(Object document) {
        //TODO: a better way to throw/treat exceptions
        /*if (!document.getClass().isAnnotationPresent(Document.class)) {
         throw new NoSuchMongoCollectionException(document.getClass() + " is not a valid Document.");
         }*/
        try {
            BasicDBObject obj = fillDBObject(document);
            String collName = reflectAnnotation(document);
            /*WriteResult writeresult = */
            db.getCollection(collName).save(obj);
            //TODO: set the objectId of the saved object above when it is an insertion.
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            Logger.getLogger(CollectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Logger.getLogger(CollectionManager.class.getName()).log(Level.INFO, "Objeto {0} salvo com sucesso.",result);
    }

    private BasicDBObject fillDBObject(Object document) throws SecurityException {
        BasicDBObject obj = new BasicDBObject();
        Field objectIdField = null;
        for (Field f : document.getClass().getDeclaredFields()) {
            try {
                f.setAccessible(true);
                if (!f.isAnnotationPresent(ObjectId.class)) {
                    obj.append(f.getName(), f.get(document));
                } else if (f.get(document) != null && !f.get(document).equals("")) {
                    objectIdField = f;
                    obj.append("_id", new org.bson.types.ObjectId((String) f.get(document)));
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(CollectionManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return obj;
    }

    private String reflectAnnotation(Object document) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, SecurityException, IllegalArgumentException {
        Annotation annotation = document.getClass().getAnnotation(Document.class);
        String coll = (String) annotation.annotationType().getMethod("collection").invoke(annotation);
        if (coll.equals("")) {
            coll = document.getClass().getSimpleName();
        }
        return coll;
    }

    public void close() {
        db.getMongo().close();
    }
}
