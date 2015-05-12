package edu.uccs.cs5920.broker;

import java.net.UnknownHostException;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Date;
import java.util.UUID;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.jongo.Jongo;
import org.jongo.MongoCollection;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.tensorwrench.shiro.realm.MongoUserPasswordRealm;

import edu.uccs.cs5920.datastore.Document;
import edu.uccs.cs5920.datastore.Token;

public class BrokerService {
	MongoClient mongoClient;
	DB db;
	Encoder encoder = Base64.getEncoder();
	DBCollection userCollection;
	MongoCollection tokenCollection;
	Jongo jongo;
	MongoUserPasswordRealm mongoRealm;
	TokenProvider tokenProvider;

	
	public BrokerService() throws Exception {
		this.mongoClient = new MongoClient();
		this.db = mongoClient.getDB( "ttp_broker" );
		Jongo jongo = new Jongo(this.db);
		userCollection = db.getCollection("users");
		tokenCollection = jongo.getCollection("tokens");
		mongoRealm = new MongoUserPasswordRealm(userCollection);
		tokenProvider = new TokenProvider();
	}
	
	public void createUser(String username, String password){
		DBObject newUser = mongoRealm.createUserCredentials(username, password);
		String[] permissions = {"create", "revoke"};
		newUser.put("permissions", permissions);
		userCollection.insert(newUser);
	} 
	
	public String issueAccessToken(String username, String password) {
		AuthenticationInfo ai = authenticateUser(username, password);
		if (ai!= null) {
			PrincipalCollection pi = ai.getPrincipals();
			if (mongoRealm.isPermitted(pi, "create")) {
				Date date = new Date();
				UUID uuid = UUID.randomUUID();
				
				String token = null;
				try {
					token = tokenProvider.generateToken(uuid.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
				Token record = new Token();
				record.setId(uuid.toString());
				record.setDate(date);
				record.setUserName(username);
				tokenCollection.insert(record);
				return token;
			}
		}
		return null;
	}
	
	public AuthenticationInfo authenticateUser(String username, String password) {
		AuthenticationInfo ai;
		try { 
			UsernamePasswordToken creds = new UsernamePasswordToken(username, password);
			ai = mongoRealm.getAuthenticationInfo(creds);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return ai;
	}
	public boolean processBadDocument(String username, String password, String recordId) throws Exception {
		AuthenticationInfo ai = authenticateUser(username, password);
		if (ai==null) 
			throw new Exception("Could not authenticate user: " + username);
		PrincipalCollection pi = ai.getPrincipals();
		if (!mongoRealm.isPermitted(pi, "revoke"))
			throw new Exception("User: " + username + " is not permitted to revoke documents");
		String docAuthor = findAuthor(recordId);
		System.out.println("User is: " + docAuthor);
		if (!revokeUser(docAuthor))
			throw new Exception("User: " + username + " could not be found or has no permissions");
		return true;
	}
	
	private String findAuthor(String recordId) {
		Token tokenDoc = tokenCollection.findOne("{id:#}",recordId).as(Token.class);
		return tokenDoc.getUserName();
	};
	
	private boolean revokeUser(String username) {
		BasicDBObject query = new BasicDBObject("name", username);
		DBObject result = userCollection.findOne(query);
		if (result!=null) {
			if (!result.containsField("permissions"))
				return false;
			result.removeField("permissions");
			userCollection.update(query,result);
			return true;
		}
		return false;
	}
	
	//public void deleteRecord(String username, String password, String userId) {};
	
	
}
