package edu.uccs.cs5920.datastore;

import java.util.List;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


@Path("/document")
@Singleton
public class DocumentResource {
	TokenValidator validator = new TokenValidator();
	DocumentService docService = new DocumentService();
	ObjectMapper mapper = new ObjectMapper();

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getDoc(@PathParam("id") String id) {
    	//TODO return doc from datastore
    	return "{\"id\":\"" + id + "\"}";
    }
    
    @GET
    @Path("/ids")
    @Produces(MediaType.APPLICATION_JSON)
    public String listDocs() {
    	List<Document> docList = docService.getUuids();
    	return toJson(docList);
    }
    
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public String getDocs() {
    	Iterable<Document> docList = docService.getDocs();
    	return toJson(docList);
    }
    
    private String toJson(Object object) {
        String result = null;
        try {
            result = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return result;
    }

    
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public String createDocument(Document document, @Context HttpHeaders headers) {
    	String token = headers.getHeaderString("token");
    	String uuid = null;
    	try {
    		uuid = validator.validateToken(token);
    		if (uuid==null) {
    			throw new Exception("Token does not contain valid ID value: " + token);
    		}
    		if (docService.getDocById(uuid) != null){
    			throw new Exception("A document has already been created with this token.");
    		}
    		
    		document.setUuid(uuid);
    		docService.createDocument(document);
    	} catch (MalformedClaimException e){
    		e.printStackTrace();
    		String msg = toJson(e.getMessage());
    		return msg;
    	} catch (InvalidJwtException e) {
    		e.printStackTrace();
    		String msg = toJson(e.getMessage());
    		return msg;
		} catch (Exception e) {
    		e.printStackTrace();
    		String msg = toJson(e.getLocalizedMessage());
    		return msg;
		} 
    	return "{\"docId\":\""+ uuid + "\"}";
    }
    
}
