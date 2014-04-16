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
package com.mongocom.management;

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
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import java.io.Closeable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.bson.types.BasicBSONList;

/**
 *
 * @author Thiago da Silva Gonzaga <thiagosg@sjrp.unesp.br>.
 */
public final class CollectionManager implements Closeable {

    private final MongoClient client;
    private DB db;
    private static final Logger LOG = Logger.getLogger(CollectionManager.class.getName());

    //TODO: a better way to manage db connection
    protected CollectionManager(MongoClient client, String dataBase) {
        this.client = client;
        if (dataBase != null && !dataBase.equals("")) {
            this.db = client.getDB(dataBase);
        } else {
            this.db = client.getDB(client.getDatabaseNames().get(0));
        }
    }

    protected CollectionManager(MongoClient client, String dbName, String user, String password) {
        this(client, dbName);
        db.authenticate(user, password.toCharArray());
    }

    public void useDB(String dbName) {
        db = client.getDB(dbName);
    }

    /**
     * The number of documents in the specified collection.
     *
     * @param <A> generic type of the collection.
     * @param collectionClass
     * @return the total of documents.
     */
    public <A extends Object> long count(Class<A> collectionClass) {
        return count(collectionClass, new MongoQuery());
    }

    /**
     * The number of documents that match the specified query.
     *
     * @param <A> generic type of the collection.
     * @param collectionClass
     * @param query
     * @return the total of documents.
     */
    public <A extends Object> long count(Class<A> collectionClass, MongoQuery query) {
        long ret = 0l;
        try {
            A result = collectionClass.newInstance();
            String collectionName = reflectCollectionName(result);
            ret = db.getCollection(collectionName).count(query.getQuery());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    /**
     * Find all documents in the specified collection.
     *
     * @param <A> generic type of the collection.
     * @param collectionClass
     * @return a list of documents.
     */
    public <A extends Object> List<A> find(Class<A> collectionClass) {
        return find(collectionClass, new MongoQuery());
    }

    /**
     * Find all documents that match the specified query.
     *
     * @param <A> generic type of the collection.
     * @param collectionClass
     * @param query
     * @return a list of documents.
     */
    public <A extends Object> List<A> find(Class<A> collectionClass, MongoQuery query) {
        List<A> resultSet = new ArrayList<>();
        DBCursor cursor = null;
        try {
            A obj = collectionClass.newInstance();
            String collectionName = reflectCollectionName(obj);
            cursor = db.getCollection(collectionName).find(query.getQuery(), query.getConstraits());
            if (query.getSkip() > 0) {
                cursor = cursor.skip(query.getSkip());
            }
            if (query.getLimit() > 0) {
                cursor = cursor.limit(query.getLimit());
            }
            while (cursor.hasNext()) {
                DBObject objDB = cursor.next();
                loadObject(obj, objDB);
                resultSet.add(obj);
                obj = collectionClass.newInstance();
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return resultSet;
    }

    /**
     * Find a single document of the the specified collection.
     *
     * @param <A> generic type of the collection.
     * @param collectionClass
     * @return a document.
     */
    public <A extends Object> A findOne(Class<A> collectionClass) {
        A result = null;
        try {
            result = collectionClass.newInstance();
            String collectionName = reflectCollectionName(result);
            DBObject obj = db.getCollection(collectionName).findOne();
            if (obj == null) {
                return null;
            }
            loadObject(result, obj);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * Find a single document that matches the specified query.
     *
     * @param <A> generic type of the collection.
     * @param collectionClass
     * @param query
     * @return a document.
     */
    public <A extends Object> A findOne(Class<A> collectionClass, MongoQuery query) {
        A result = null;
        try {
            result = collectionClass.newInstance();
            String collectionName = reflectCollectionName(result);
            DBObject obj = db.getCollection(collectionName).findOne(query.getQuery(), query.getConstraits());
            if (obj == null) {
                return null;
            }
            loadObject(result, obj);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * Find a single document that matches the specified id.
     *
     * @param <A> generic type of the collection.
     * @param collectionClass
     * @param id
     * @return a document.
     */
    public <A extends Object> A findById(Class<A> collectionClass, String id) {
        return findOne(collectionClass, new MongoQuery("_id", id));
    }

    /**
     * Remove the specified document from the collection.
     *
     * @param document to be removed.
     */
    public void remove(Object document) {
        try {
            BasicDBObject obj = loadDocument(document);
            String collectionName = reflectCollectionName(document);
            db.getCollection(collectionName).remove(obj);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | SecurityException | IllegalArgumentException ex) {
            LOG.log(Level.SEVERE, "An error occured while removing this document: {0}", ex.getMessage());
        }

    }

    /**
     * Insert the document in a collection
     *
     * @param document
     * @return the <code>_id</code> of the inserted document, <code>null</code>
     * if fails.
     */
    public String insert(Object document) {
        String _id = null;
        if (document == null) {
            return _id;
        }
        try {
            BasicDBObject obj = loadDocument(document);
            String collectionName = reflectCollectionName(document);
            db.getCollection(collectionName).insert(obj);
            _id = obj.getString("_id");
            Field field = getFieldByAnnotation(document, ObjectId.class, false);
            if (field != null) {
                field.setAccessible(true);
                field.set(document, _id);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | SecurityException | IllegalArgumentException | NoSuchFieldException ex) {
            LOG.log(Level.SEVERE, "An error occured while inserting this document: {0}", ex.getMessage());
        }
        if (_id != null) {
            LOG.log(Level.INFO, "Object \"{0}\" inserted successfully.", _id);
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
            BasicDBObject obj = loadDocument(document);
            String collectionName = reflectCollectionName(document);
            db.getCollection(collectionName).update(query.getQuery(), obj, upsert, multi, concern);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | SecurityException | IllegalArgumentException ex) {
            LOG.log(Level.SEVERE, null, ex);
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
        if (document == null) {
            return _id;
        }
        try {
            BasicDBObject obj = loadDocument(document);
            String collectionName = reflectCollectionName(document);
            db.getCollection(collectionName).save(obj);
            _id = obj.getString("_id");
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            LOG.log(Level.SEVERE, "An error occured while saving this document: {0}", ex.getMessage());
        }
        if (_id != null) {
            LOG.log(Level.INFO, "Object \"{0}\" saved successfully.", _id);
        }
        return _id;
    }

    private BasicDBObject loadDocument(Object document) throws SecurityException {
        Field[] fields = document.getClass().getDeclaredFields();
        BasicDBObject obj = new BasicDBObject();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                String fieldName = field.getName();
                Object fieldContent = field.get(document);
                if (fieldContent == null) {
                    continue;
                }
                if (fieldContent instanceof List) {
                    BasicDBList list = new BasicDBList();
                    boolean isInternal = field.isAnnotationPresent(Internal.class);
                    for (Object item : (List) fieldContent) {
                        if (isInternal) {
                            list.add(loadDocument(item));
                        } else {
                            list.add(item);
                        }
                    }
                    obj.append(fieldName, list);
                } else if (field.getType().isEnum()) {
                    obj.append(fieldName, fieldContent.toString());
                } else if (field.isAnnotationPresent(Reference.class)) {
                    obj.append(fieldName, new org.bson.types.ObjectId(save(fieldContent)));
                } else if (field.isAnnotationPresent(Internal.class)) {
                    obj.append(fieldName, loadDocument(fieldContent));
                } else if (!field.isAnnotationPresent(ObjectId.class)) {
                    obj.append(fieldName, fieldContent);
                } else if (!fieldContent.equals("")) {
                    obj.append("_id", new org.bson.types.ObjectId((String) fieldContent));
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
        return obj;
    }

    private <A extends Object> void loadObject(A object, DBObject document) throws IllegalAccessException, IllegalArgumentException, SecurityException, InstantiationException {
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            Object fieldContent = document.get(fieldName);
            if (fieldContent == null) {
                continue;
            }
            if (fieldContent instanceof BasicBSONList) {
                Class<?> fieldArgClass = null;
                ParameterizedType genericFieldType = (ParameterizedType) field.getGenericType();
                Type[] fieldArgTypes = genericFieldType.getActualTypeArguments();
                for (Type fieldArgType : fieldArgTypes) {
                    fieldArgClass = (Class<?>) fieldArgType;
                }
                List<Object> list = new ArrayList<>();
                boolean isInternal = field.isAnnotationPresent(Internal.class);
                for (Object item : (BasicBSONList) fieldContent) {
                    if (isInternal) {
                        Object o = fieldArgClass.newInstance();
                        loadObject(o, (DBObject) item);
                        list.add(o);
                    } else {
                        list.add(item);
                    }
                }
                field.set(object, list);
            } else if (field.getType().isEnum()) {
                field.set(object, Enum.valueOf((Class) field.getType(), (String) fieldContent));
            } else if (field.isAnnotationPresent(ObjectId.class)) {
                field.set(object, ((BasicDBObject) fieldContent).getString("_id"));
            } else if (field.isAnnotationPresent(Reference.class)) {
                field.set(object, findById(field.getType(), ((org.bson.types.ObjectId) fieldContent).toString()));
            } else if (field.getType().isPrimitive()) {

            } else {
                field.set(object, fieldContent);

            }
        }
    }

    private <A> Field getFieldByAnnotation(Object obj, Class<? extends Annotation> annotationClass, boolean annotationRequired) throws NoSuchFieldException {
        Field[] fields = getFieldsByAnnotation(obj, annotationClass);
        if ((fields.length == 0) && annotationRequired) {
            throw new NoSuchFieldException("@" + annotationClass.getSimpleName() + " field not found.");
        } else if (fields.length > 0) {
            if (fields.length > 1) {
                LOG.log(Level.WARNING, "There are more than one @{0} field. Assuming the first one.", annotationClass.getSimpleName());
            }
            return fields[0];
        }
        return null;
    }

    private <A> Field[] getFieldsByAnnotation(Object obj, Class<? extends Annotation> annotationClass) {
        Field[] fields = obj.getClass().getDeclaredFields();
        List<Field> fieldsAnnotated = new ArrayList<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(annotationClass)) {
                fieldsAnnotated.add(field);
            }
        }
        return (Field[]) fieldsAnnotated.toArray();
    }

    private String reflectCollectionName(Object document) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, SecurityException, IllegalArgumentException {
        Annotation annotation = document.getClass().getAnnotation(Document.class);
        String coll = (String) annotation.annotationType().getMethod("collection").invoke(annotation);
        if (coll.equals("")) {
            coll = document.getClass().getSimpleName();
        }
        return coll;
    }

    public String getStatus() {
        return client.getAddress() + " " + client.getMongoOptions();
    }

    @Override
    public void close() {
        client.close();
    }
}
