package edu.uccs.cs5920.datastore;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.jongo.Jongo;
import org.jongo.MongoCollection;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;


public class DocumentService {

	Mongo mongoClient;
	DB db;
	Jongo jongo;
	MongoCollection docCollection;
	
	public DocumentService() {
		try {
			this.mongoClient = new MongoClient();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.db = mongoClient.getDB( "ttp_datastore" );
		Jongo jongo = new Jongo(this.db);
		docCollection = jongo.getCollection("documents");
	}
	
	public String createDocument(Document doc) {		
		docCollection.save(doc);
		return doc.getUuid();
	}
	
	public List<Document> getUuids() {
		List<Document> docs;
		docs = docCollection.distinct("uuid").as(Document.class);
		return docs;
	}

	public Iterable<Document> getDocs() {
		Iterable<Document> docs;
		docs = docCollection.find().as(Document.class);
		return docs;
	}
	
	public Document getDocById(String uuid) {
		Document doc = docCollection.findOne("{uuid: #}", uuid).as(Document.class);
		return doc;
	}
}
