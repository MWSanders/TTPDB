package edu.uccs.cs5920.broker;

import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import edu.uccs.cs5920.datastore.Document;

@Path("/user")
@Singleton
public class UserResource {

	private BrokerService broker;
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public UserResource() {
		try {
			this.broker = new BrokerService();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    @POST
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
	public String createUser(@PathParam("username") String username, @Context HttpHeaders headers, @Context UriInfo ui) {
    	MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        MultivaluedMap<String, String> pathParams = ui.getPathParameters();
        String password = headers.getHeaderString("password");
    	broker.createUser(username, password);
    	return "User: " + username + " created";		
	}
    
    @POST
    @Path("/{username}/token")
    @Produces(MediaType.APPLICATION_JSON)
	public String generateToken(@PathParam("username") String username, @Context HttpHeaders headers) {
        String password = headers.getHeaderString("password");
        String tokenValue = broker.issueAccessToken(username, password);
        System.out.println(tokenValue);
        String returnValue = "{ \"token\":\"" + tokenValue + "\"}";		
        return returnValue;
	}
    
    @DELETE
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
	public String reportDocument(@PathParam("username") String username, @Context HttpHeaders headers) {
        String password = headers.getHeaderString("password");
        String documentId = headers.getHeaderString("id");
        boolean revoked=false;
        String error=null;
        try {
        	revoked = broker.processBadDocument(username, password, documentId);
        } catch (Exception e) {
        	error = e.getMessage();
        }
        	String returnValue;
        if (revoked){
        	returnValue = "{ \"user\":\"" + username + "\", \"documentId\":\"" + documentId + "\", \"removed\":true}";
        } else {
        	returnValue = "{ \"user\":\"" + username + "\", \"documentId\":\"" + documentId + "\", \"removed\":false, \"error\":\""+error+"\"}";
        }		
        return returnValue;
	}
}
