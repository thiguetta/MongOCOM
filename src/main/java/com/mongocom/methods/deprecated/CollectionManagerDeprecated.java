/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mongocom.methods.deprecated;

import com.mongocom.methods.MongoQuery;
import java.util.List;

/**
 *
 * @author thiago
 */
@Deprecated
public interface CollectionManagerDeprecated {

    /**
     *
     * Provides access to the aggregation pipeline.
     */
    void aggregate();

    /**
     *
     * Wraps count to return a count of the number of documents in a collection
     * or matching a query.
     *
     * @param query
     * @return a count of the number of documents
     */
    int count(MongoQuery query);

    /**
     *
     * Wraps eval to copy data between collections in a single MongoDB instance.
     */
    void copyTo();

    /**
     *
     * Builds an index on a collection. Use db.collection.ensureIndex().
     */
    void createIndex();

    /**
     *
     * Renders a human-readable view of the data collected by indexStats which
     * reflects B-tree utilization.
     *
     * @return
     */
    String getIndexStats();

    /**
     *
     * Renders a human-readable view of the data collected by indexStats which
     * reflects B-tree utilization.
     *
     * @return
     */
    String indexStats();

    /**
     *
     * Wraps the size field in the output of the collStats
     *
     * @return the size of the collection
     */
    int dataSize();

    /**
     *
     * Returns an array of documents that have distinct values for the specified
     * field.
     *
     * @return
     */
    List<String> distinct();

    /**
     *
     * Removes the specified collection from the database.
     */
    void drop();

    void dropIndex();	//Removes a specified index on a collection.

    void dropIndexes();	//Removes all indexes on a collection.

    void ensureIndex();	//Creates an index if it does not currently exist. If the index exists ensureIndex() does nothing.

    void find();//	Performs a query on a collection and returns a cursor object.

    void findAndModify();	//Atomically modifies and returns a single document.

    void findOne();	//Performs a query and returns a single document.

    void getIndexes();	//Returns an array of documents that describe the existing indexes on a collection.

    void getShardDistribution();	//For collections in sharded clusters, db.collection.getShardDistribution() reports data of chunk distribution.

    void getShardVersion();	 //Internal diagnostic method for shard cluster.

    void group();	//Provides simple data aggregation function. Groups documents in a collection by a key, and processes the results. Use aggregate() for more complex data aggregation.

    void insert();	//Creates a new document in a collection.

    void isCapped(); //	Reports if a collection is a capped collection.

    void mapReduce();//	Performs map-reduce style data aggregation.

    void reIndex();//	Rebuilds all existing indexes on a collection.

    void remove();//	Deletes documents from a collection.

    void renameCollection();//	Changes the name of a collection.

    void save();//	Provides a wrapper around an insert() and update() to insert new documents.

    void stats();//	Reports on the state of a collection. Provides a wrapper around the collStats.

    void storageSize();//	Reports the total size used by the collection in bytes. Provides a wrapper around the storageSize field of the collStats output.

    void totalSize();//	Reports the total size of a collection, including the size of all documents and all indexes on a collection.

    void totalIndexSize();//	Reports the total size used by the indexes on a collection. Provides a wrapper around the totalIndexSize field of the collStats output.

    void update();//	Modifies a document in a collection.

    void validate();//	Performs diagnostic operations on a collection.

}
