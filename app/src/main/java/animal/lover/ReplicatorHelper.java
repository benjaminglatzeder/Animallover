package animal.lover;

import com.couchbase.lite.Database;
import com.couchbase.lite.ReplicatorConfiguration;
import com.couchbase.lite.URLEndpoint;

import java.net.URI;
import java.net.URISyntaxException;

public class ReplicatorHelper {

    public static ReplicatorConfiguration getReplicatorConfiguration(Database database) throws URISyntaxException {
        URI syncGatewayURI = new URI("ws://<IP-ADDRESS>/database");
        URLEndpoint endpoint = new URLEndpoint(syncGatewayURI);
        ReplicatorConfiguration configuration = new ReplicatorConfiguration(database, endpoint);
        configuration.setReplicatorType(ReplicatorConfiguration.ReplicatorType.PUSH_AND_PULL);
        configuration.setContinuous(true);
        return configuration;
    }
}