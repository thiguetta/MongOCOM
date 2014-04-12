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
package com.example.collections;

import com.mongocom.annotations.Internal;
import com.mongocom.annotations.Document;
import com.mongocom.annotations.ObjectId;
import com.mongocom.annotations.Reference;
import java.util.List;

/**
 *
 * @author Thiago da Silva Gonzaga <thiagosg@sjrp.unesp.br>.
 */
@Document //this represents the structure of a document of the collection contact
public class Contact {

    @ObjectId
    private String id; //not required unless you want to retrive this information from db
    private String name;
    @Internal //embedded document(s)
    private List<Phone> phones;
    private String email;
    @Reference //relationship with another document
    private Contact company;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Phone> getPhones() {
        return phones;
    }

    public void setPhones(List<Phone> phones) {
        this.phones = phones;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Contact getCompany() {
        return company;
    }

    public void setCompany(Contact company) {
        this.company = company;
    }

}
