/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.mongo.entities;

import java.util.Date;
import com.github.mongo.annotations.MongoCollection;
import com.github.mongo.annotations.ObjectId;

/**
 *
 * @author thiago
 */
@MongoCollection(name="colecao")
public class NovaColecao {
    @ObjectId
    private String id;
    private String nome;
    private String telefone;
    private int numero;
    private Date timestamp = new Date();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
}
