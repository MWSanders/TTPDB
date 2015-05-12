package edu.uccs.cs5920;
import static com.codahale.metrics.MetricRegistry.name;

import java.util.UUID;

import edu.uccs.cs5920.broker.TokenProvider;
import edu.uccs.cs5920.datastore.TokenValidator;


import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

public class TokenGeneratorTest {
	TokenProvider provider;
	TokenValidator validator;
    MetricRegistry metricRegistry;
    Timer tokenGenTimer;
    Timer validationTimer;
	
    public static void main(String[] args) throws Exception {
    	TokenGeneratorTest tester = new TokenGeneratorTest();
    	tester.benchmarkTokens();
    }

	public void before() {
		try {
			this.provider = new TokenProvider();
			this.validator = new TokenValidator();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		metricRegistry = new MetricRegistry();
		tokenGenTimer = metricRegistry.timer(name(getClass(), "tokenGeneration"));
		validationTimer = metricRegistry.timer(name(getClass(), "tokenValidation"));
	}
	
	public void benchmarkTokens() throws Exception {
		before();
		int testRuns =1000;
		String uuids[] = new String[testRuns];
		String tokens[] = new String[testRuns];
		for (int i=0; i<testRuns; i++) {
			uuids[i] = UUID.randomUUID().toString();
		}
		String warmingToken = provider.generateToken("WARMING AND INIT");
		for (int i=0; i<testRuns; i++) {
			final Timer.Context context = tokenGenTimer.time();
			try {
				tokens[i] = provider.generateToken(uuids[i]);
			} finally {
				context.stop();
			}
		}
		
		validator.validateToken(warmingToken);
		for (int i=0; i<testRuns; i++) {
			final Timer.Context context = validationTimer.time();
			try {
				validator.validateToken(tokens[i]);
			} finally {
				context.stop();
			}
		}
		
		ConsoleReporter reporter = ConsoleReporter.forRegistry(metricRegistry).build();
		reporter.report();
	}
}
