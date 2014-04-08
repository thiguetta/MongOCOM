/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mongocom;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import com.github.mongo.entities.NovaColecao;
import com.github.mongo.methods.CollectionManagerTest;

/**
 *
 * @author thiago
 */
public class MongoTest {

    /**
     * @param args the command line arguments
     * @throws java.net.UnknownHostException
     */
    public static void main(String[] args) throws UnknownHostException {

        ConnectDB db = ConnectDB.connectLocal();
        //NewClass c = new NewClass();
        //c.setNome("teste");
        //c.setTelefone("mais um");
        //MeuObjeto m = new MeuObjeto();
        //m.setCnpj("22345323525");
        //m.setEmail("mail@mail.com");
        //m.setEmpresa("minha emprsa");
        CollectionManagerTest conn = new CollectionManagerTest();
        conn.setDB(db.getDB("mydb"));
        for (int i = 0; i < 10; i++) {
            NovaColecao c = new NovaColecao();
            c.setNome("gelio" + i);
            c.setNumero(1000);
            c.setTelefone("98115-5151");
            conn.save(c);
        }
        List resultSet = conn.find(NovaColecao.class);
        Iterator i = resultSet.iterator();
        while (i.hasNext()) {
            NovaColecao n = (NovaColecao) i.next();
            System.out.println(n.getId());
            System.out.println(n.getNome());
            System.out.println(n.getNumero());
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
            System.out.println(formatter.format(n.getTimestamp()));
        }
    }

}
