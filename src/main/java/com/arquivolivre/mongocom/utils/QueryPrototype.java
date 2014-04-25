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
import java.util.List;

/**
 *
 * @author Thiago da Silva Gonzaga <thiagosg@sjrp.unesp.br>
 */
public interface QueryPrototype {

    <A extends Object> Object execute() throws MalformedQueryExeption;

    List<?> executeFind() throws MalformedQueryExeption;

    QueryPrototype select(String... fields);
    
    QueryPrototype selectRemovingFiedls(String... fields);

    QueryPrototype from(Class<?> collection);

    QueryPrototype where(String field);

    QueryPrototype equalsTo(Object value);

    QueryPrototype notEqualsTo(Object value);

    QueryPrototype graterThan(Object value);

    QueryPrototype lessThan(Object value);

    QueryPrototype greaterThanOrEqualTo(Object value);

    QueryPrototype lessThanOrEqualTo(Object valu);

    QueryPrototype and(String field);

    QueryPrototype or(String field);

    QueryPrototype exists();

    QueryPrototype doesntExist();

    QueryPrototype in(Object... values);

    QueryPrototype notIn(Object... values);

}
