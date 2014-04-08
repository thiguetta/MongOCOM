/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.mongo.entities;

import com.github.mongo.annotations.MongoCollection;
import com.github.mongo.annotations.ObjectId;

/**
 *
 * @author thiago
 */
@MongoCollection(name="lista")
public class NewClass {
    
    @ObjectId
    private String id;
    private String nome;
    private String telefone;

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
    
    
    
}
