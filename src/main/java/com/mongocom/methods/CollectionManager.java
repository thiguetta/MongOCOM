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
import com.mongocom.annotations.Internal;
import com.mongocom.annotations.ObjectId;
import com.mongocom.annotations.Reference;
import com.mongodb.BasicDBList;
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
            String fieldName = f.getName();
            Object fieldContent = objDB.get(fieldName);
            if (fieldContent != null && f.isAnnotationPresent(ObjectId.class)) {
                f.set(obj, ((BasicDBObject) fieldContent).getString("_id"));
            } else if (fieldContent == null && f.getType().isPrimitive()) {
                
            } else {
                f.set(obj, fieldContent);
                
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
    
    public String insert(Object document) {
        String _id = null;
        try {
            BasicDBObject obj = fillDBObject(document);
            String collName = reflectAnnotation(document);
            db.getCollection(collName).insert(obj);
            _id = obj.getString("_id");
            Field fObjectId = getFieldByAnnotation(document, ObjectId.class, false);
            if (fObjectId != null) {
                fObjectId.setAccessible(true);
                fObjectId.set(document, _id);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | SecurityException | IllegalArgumentException | NoSuchFieldException ex) {
            Logger.getLogger(CollectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return _id;
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
    
    public String save(Object document) {
        //TODO: a better way to throw/treat exceptions
        /*if (!document.getClass().isAnnotationPresent(Document.class)) {
         throw new NoSuchMongoCollectionException(document.getClass() + " is not a valid Document.");
         }*/
        String _id = null;
        try {
            BasicDBObject obj = fillDBObject(document);
            String collName = reflectAnnotation(document);
            /*WriteResult writeresult = */
            db.getCollection(collName).save(obj);
            _id = obj.getString("_id");
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            Logger.getLogger(CollectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return _id;
        //Logger.getLogger(CollectionManager.class.getName()).log(Level.INFO, "Objeto {0} salvo com sucesso.",result);
    }
    
    private BasicDBObject fillDBObject(Object document) throws SecurityException {
        BasicDBObject obj = new BasicDBObject();
        for (Field f : document.getClass().getDeclaredFields()) {
            try {
                f.setAccessible(true);
                String fieldName = f.getName();
                Object fieldContent = f.get(document);
                if (fieldContent instanceof List) {
                    BasicDBList list = new BasicDBList();
                    boolean isInternal = fieldContent.getClass().isAnnotationPresent(Internal.class);
                    for (Object item : (List) fieldContent) {
                        if (isInternal) {
                            list.add(fillDBObject(item));
                        } else {
                            list.add(item);
                        }
                    }
                    obj.append(fieldName, list);
                } else if (f.isAnnotationPresent(Reference.class)) {
                    obj.append(fieldName, save(fieldContent));
                } else if (f.isAnnotationPresent(Internal.class)) {
                    obj.append(fieldName, fillDBObject(fieldContent));
                } else if (!f.isAnnotationPresent(ObjectId.class)) {
                    obj.append(fieldName, fieldContent);
                } else if (fieldContent != null && !fieldContent.equals("")) {
                    obj.append("_id", new org.bson.types.ObjectId((String) fieldContent));
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(CollectionManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return obj;
    }
    
    private <A> Field getFieldByAnnotation(Object obj, Class<? extends Annotation> annotationClass, boolean annotationRequired) throws NoSuchFieldException {
        Field[] fields = obj.getClass().getFields();
        for (Field f : fields) {
            if (f.isAnnotationPresent(annotationClass)) {
                return f;
            }
        }
        if (annotationRequired) {
            throw new NoSuchFieldException("@" + annotationClass.getSimpleName() + " field not found.");
        }
        return null;
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
