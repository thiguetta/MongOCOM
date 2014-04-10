/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import com.mongocom.annotations.MongoCollection;
import com.mongocom.annotations.ObjectId;
import com.mongocom.exceptions.NoSuchMongoCollectionException;
import com.mongodb.WriteResult;

/**
 *
 * @author thiago
 */
public class CollectionManagerTest {

    //Provides a wrapper around an insert() and update() to insert new documents.
    private DB db;

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
            Annotation annotation = result.getClass().getAnnotation(MongoCollection.class);
            String collName = (String) annotation.annotationType().getMethod("name").invoke(annotation);
            if (collName.equals("")) {
                collName = collectionClass.getSimpleName();
            }
            ret = db.getCollection(collName).count(query.getQuery());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            Logger.getLogger(CollectionManagerTest.class.getName()).log(Level.SEVERE, null, ex);
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
            Annotation annotation = obj.getClass().getAnnotation(MongoCollection.class);
            String collName = (String) annotation.annotationType().getMethod("name").invoke(annotation);
            if (collName.equals("")) {
                collName = collectionClass.getSimpleName();
            }
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
            Logger.getLogger(CollectionManagerTest.class.getName()).log(Level.SEVERE, null, ex);
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
            Annotation annotation = result.getClass().getAnnotation(MongoCollection.class);
            String collName = (String) annotation.annotationType().getMethod("name").invoke(annotation);
            if (collName.equals("")) {
                collName = collectionClass.getSimpleName();
            }
            DBObject obj = db.getCollection(collName).findOne();
            if (obj == null) {
                return null;
            }
            fillFields(result, obj);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException ex) {
            Logger.getLogger(CollectionManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public void save(Object document) throws NoSuchMongoCollectionException {
        if (!document.getClass().isAnnotationPresent(MongoCollection.class)) {
            throw new NoSuchMongoCollectionException(document.getClass() + " is not a valid MongoCollection.");
        }
        try {
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
                    Logger.getLogger(CollectionManagerTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            Annotation annotation = document.getClass().getAnnotation(MongoCollection.class);
            String coll = (String) annotation.annotationType().getMethod("name").invoke(annotation);
            if (coll.equals("")) {
                coll = document.getClass().getSimpleName();
            }
            /*WriteResult writeresult = */
            db.getCollection(coll).save(obj);
            //TODO set the objectId of the saved object above when it is an insertion.
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            Logger.getLogger(CollectionManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Logger.getLogger(CollectionManagerTest.class.getName()).log(Level.INFO, "Objeto {0} salvo com sucesso.",result);
    }

    public void close() {
        db.getMongo().close();
    }
}
