/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mongo.methods;

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
import com.github.mongo.annotations.MongoCollection;
import com.github.mongo.annotations.ObjectId;

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

    public <A extends Object> List<A> find(Class<A> collectionClass) {
        return find(collectionClass, null);
    }

    public <A extends Object> List<A> find(Class<A> collectionClass, MongoQuery query) {
        List<A> resultSet = new ArrayList<>();
        DBCursor resultDB;
        try {
            A obj = collectionClass.newInstance();
            Annotation annotation = obj.getClass().getAnnotation(MongoCollection.class);
            String collName = (String) annotation.annotationType().getMethod("name").invoke(annotation);
            if (query != null) {
                resultDB = db.getCollection(collName).find(query.getQuery(), query.getConstraits());
            } else {
                resultDB = db.getCollection(collName).find();
            }
            while (resultDB.hasNext()) {
                DBObject objDB = resultDB.next();
                if (objDB == null) {
                    return null;
                }
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
                resultSet.add(obj);
                obj = collectionClass.newInstance();
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            Logger.getLogger(CollectionManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resultSet;
    }

    public <A extends Object> A findOne(Class<A> collectionClass) {
        A result = null;
        try {
            result = collectionClass.newInstance();
            Annotation annotation = result.getClass().getAnnotation(MongoCollection.class);
            String collName = (String) annotation.annotationType().getMethod("name").invoke(annotation);
            DBObject obj = db.getCollection(collName).findOne();
            if (obj == null) {
                return null;
            }
            Field[] fields = result.getClass().getDeclaredFields();
            for (Field f : fields) {
                f.setAccessible(true);
                if (f.isAnnotationPresent(ObjectId.class)) {
                    f.set(result, obj.get("_id").toString());
                } else {
                    f.set(result, obj.get(f.getName()));
                }
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException ex) {
            Logger.getLogger(CollectionManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public void save(Object document) {
        try {
            BasicDBObject obj = new BasicDBObject();
            for (Field f : document.getClass().getDeclaredFields()) {
                try {
                    f.setAccessible(true);
                    if (!f.isAnnotationPresent(ObjectId.class)) {
                        obj.append(f.getName(), f.get(document));
                    }
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(CollectionManagerTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            Annotation annotation = document.getClass().getAnnotation(MongoCollection.class);
            String coll = (String) annotation.annotationType().getMethod("name").invoke(annotation);
            db.getCollection(coll).save(obj);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            Logger.getLogger(CollectionManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Logger.getLogger(CollectionManagerTest.class.getName()).log(Level.INFO, "Objeto {0} salvo com sucesso.",result);
    }
}
