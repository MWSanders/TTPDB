import static com.codahale.metrics.MetricRegistry.name;

import java.io.IOException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uccs.cs5920.datastore.Token;


public class BulkTestClient {
	static ObjectMapper mapper = new ObjectMapper();
    static MetricRegistry metricRegistry = new MetricRegistry();
    static Timer tokenTimer = metricRegistry.timer(name(BulkTestClient.class, "FetchToken"));
    static Timer docTimer = metricRegistry.timer(name(BulkTestClient.class, "InsertDocument"));
	
	public static void main(String[] args) throws JsonProcessingException, IOException {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target("http://localhost:8001/Broker/v1/user/user1/token");
		
		int testRuns=1000;
		String tokens[] = new String[testRuns];
		for (int i=0; i<testRuns; i++){
			final Timer.Context context = tokenTimer.time();
			Response response = target.request().header("Content-Type", "application/json").header("password", "password1").post(null);
			tokens[i] = mapper.readTree(response.readEntity(String.class)).path("token").textValue();
			context.stop();
		}
		
		String testDoc = "{\"incidentType\":\"Port Scan\",\"description\":\"Our public website was portscanned from 1.1.1.1\",\"incidentDate\":1427633254834,\"reportDate\":1427633254834}";
		Entity<String> docEntity = Entity.entity(testDoc, "application/json"); 
		WebTarget datastoreTarget = client.target("http://localhost:8002/Datastore/v1/document/");
		for (int i=0; i<testRuns; i++){
			final Timer.Context context = docTimer.time();
			Response response = datastoreTarget.request().header("Content-Type", "application/json").header("token", tokens[i]).post(docEntity);
			String strResponse = mapper.readTree(response.readEntity(String.class)).path("docId").textValue();
			context.stop();
		}
		
		ConsoleReporter reporter = ConsoleReporter.forRegistry(metricRegistry).build();
		reporter.report();
	}

}
