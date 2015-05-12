package edu.uccs.cs5920.datastore;

import javax.inject.Inject;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class Application extends ResourceConfig {
    private static Logger log = LoggerFactory.getLogger(Application.class);

    public Application() {
        packages("edu.uccs.cs5920.datastore");
        registerInstances(new Bindings());
        register(ApplicationListener.class);
    }

    public static class ApplicationListener implements ContainerLifecycleListener {

        @Inject
        public ApplicationListener(Config config) {
        }

		@Override
		public void onReload(Container arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onShutdown(Container arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStartup(Container arg0) {
			// TODO Auto-generated method stub
			
		}

    }

    public static class Bindings extends AbstractBinder {

        @Override
        protected void configure() {
            Config config = ConfigFactory.load();
            bind(config).to(Config.class);

//            // Since there might be more than one Collection in this DB...
//            try {
//                String mongoConfigURI = config.getString("mongo.backend.config");
//                MongoClient mongo = new MongoClient(new MongoClientURI(mongoConfigURI));
//                DB db = mongo.getDB("ttp_datastore");
//            } catch (Exception e) {
//                log.error("Unable to connect to MongoDB", e);
//            }
        }
    }
}
