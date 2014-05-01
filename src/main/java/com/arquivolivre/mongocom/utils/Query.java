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
package com.arquivolivre.mongocom.utils;

import com.arquivolivre.mongocom.exceptions.MalformedQueryExeption;
import com.arquivolivre.mongocom.management.CollectionManager;
import com.arquivolivre.mongocom.management.MongoQuery;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import java.util.List;

/**
 *
 * @author Thiago da Silva Gonzaga <thiagosg@sjrp.unesp.br>
 */
public class Query extends QueryBuilder implements QueryPrototype {

    private DBObject retrivedFields;
    private DBObject setFields;
    private boolean isUpdate;
    private String currentKey;
    private Class<?> collectionClass;
    private CollectionManager cm;

    private Query() {
        super();
    }

    public Query(CollectionManager cm) {
        super();
        this.cm = cm;
    }

    public static Query withCollectionManager(CollectionManager cm) {
        Query q = new Query();
        q.setCollectionManager(cm);
        return q;
    }

    @Override
    public <A> Object execute() throws MalformedQueryExeption {
        if (collectionClass == null) {
            throw new MalformedQueryExeption("No collection defined.");
        }
        return cm.findOne(collectionClass, new MongoQuery(get(), retrivedFields));
    }

    @Override
    public List<?> executeFind() throws MalformedQueryExeption {
        if (collectionClass == null) {
            throw new MalformedQueryExeption("No collection defined.");
        }
        return cm.find(collectionClass, new MongoQuery(get(), retrivedFields));
    }

    public void executeUpdate() {

    }

    public Query update() {
        isUpdate = true;
        return this;
    }

    public Query set(String field, Object newValue) {
        if (setFields == null) {
            setFields = new BasicDBObject();
            setFields.put("$set", new BasicDBObject(field, newValue));
        } else {
            DBObject o = (DBObject) setFields.get("$set");
            o.put(field, newValue);
        }
        return this;
    }

    @Override
    public Query select(String... fields) {
        retrivedFields = new BasicDBList();
        for (String field : fields) {
            retrivedFields.put(field, 1);
        }
        return this;
    }

    @Override
    public Query selectRemovingFiedls(String... fields) {
        retrivedFields = new BasicDBList();
        for (String field : fields) {
            retrivedFields.put(field, 0);
        }
        return this;
    }

    @Override
    public Query from(Class<?> collection) {
        this.collectionClass = collection;
        return this;
    }

    @Override
    public Query where(String field) {
        put(field);
        currentKey = field;
        return this;
    }

    @Override
    public Query equalsTo(Object value) {
        is(value);
        return this;
    }

    @Override
    public Query notEqualsTo(Object value) {
        notEquals(value);
        return this;
    }

    @Override
    public Query graterThan(Object value) {
        greaterThan(value);
        return this;
    }

    @Override
    public Query lessThan(Object value) {
        lessThan(value);
        return this;
    }

    @Override
    public Query greaterThanOrEqualTo(Object value) {
        greaterThanEquals(value);
        return this;
    }

    @Override
    public Query lessThanOrEqualTo(Object valu) {
        lessThanEquals(valu);
        return this;
    }

    @Override
    public Query and(String field) {
        and(field);
        currentKey = field;
        return this;
    }

    @Override
    public Query or(String field) {
        return this;
    }

    @Override
    public Query exists() {
        exists(currentKey);
        return this;
    }

    @Override
    public Query doesntExist() {
        exists(currentKey);
        return this;
    }

    @Override
    public Query in(Object... values) {
        in(values);
        return this;
    }

    @Override
    public Query notIn(Object... values) {
        notIn(values);
        return this;
    }

    private void setCollectionManager(CollectionManager cm) {
        this.cm = cm;
    }

}
