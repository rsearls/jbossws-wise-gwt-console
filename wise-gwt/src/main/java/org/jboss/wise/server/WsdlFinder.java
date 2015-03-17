package org.jboss.wise.server;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import javax.enterprise.context.ConversationScoped;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * User: rsearls
 * Date: 3/1/15
 */
//@ConversationScoped
public class WsdlFinder implements Serializable {

    private List<String> wsdlList = new ArrayList<String>();

    private final String[] testData = new String [] {
        "http://localhost:8080/jaxws-jbws1798/Service?wsdl",
        "http://localhost:8080/jaxws-jbws2259?wsdl",
        "http://localhost:8080/schemasInWeirdPlaceFromSrc/SayHiImpl?wsdl"
    };

    public WsdlFinder() {
       // For debugging use only
        wsdlList.clear();
        init();
        /**
        try {
            List<ModelNode> dataSources = getDeployedApps();
            for (ModelNode dataSource : dataSources) {
                List<ModelNode> endpointList = getEndpoints(dataSource.asString());
                for (ModelNode endPt : endpointList) {
                    String wsdlName = getWsdlUrl(endPt);
                    if (wsdlName != null) {
                        wsdlList.add(wsdlName);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        **/
        dumpList(wsdlList);
    }

    private void init () {
        wsdlList.clear();
        for (int i = 0; i < testData.length; i++) {
            wsdlList.add(testData[i]);
        }
    }

    private void dumpList(List<String> wList) {
        for (String s : wList) {
            System.out.println("##++## wsdl-url: " + s);
        }
    }

    public List<String> getWsdlList() {
        return wsdlList;
    }

    /**
     * @param mNode
     * @return
     */
    private String getWsdlUrl(ModelNode mNode) {

        for (Property p : mNode.asPropertyList()) {
            for (Property pp : p.getValue().asPropertyList()) {
                if (pp.getValue().has("wsdl-url")) {
                    return pp.getValue().get("wsdl-url").asString();
                }
            }
        }
        return null;
    }

    /**
     * @param appName
     * @return
     * @throws java.io.IOException
     */
    private List<ModelNode> getEndpoints(String appName) throws IOException {
        final ModelNode request = new ModelNode();
        request.get(ClientConstants.OP).set("read-resource");
        ModelNode address = request.get("address");
        address.add("deployment", appName);
        address.add("subsystem", "webservices");
        request.get("recursive").set(true);
        ModelControllerClient client = null;

        List<ModelNode> resultList = new ArrayList<ModelNode>();
        try {
            client = ModelControllerClient.Factory.create(InetAddress.getByName("127.0.0.1"), 9990);
            final ModelNode response = client.execute(new OperationBuilder(request).build());

            if ("success".equals(response.get(ClientConstants.OUTCOME).asString())) {
                resultList.addAll(response.get(ClientConstants.RESULT).asList());
            }
        } finally {
            safeClose(client);
        }

        return resultList;
    }

    /**
     * @return
     * @throws IOException
     */
    private List<ModelNode> getDeployedApps() throws IOException {
        final ModelNode request = new ModelNode();
        request.get(ClientConstants.OP).set("read-children-names");
        request.get("child-type").set("deployment");
        ModelControllerClient client = null;

        List<ModelNode> resultList = new ArrayList<ModelNode>();
        try {
            client = ModelControllerClient.Factory.create(InetAddress.getByName("127.0.0.1"), 9990);
            final ModelNode response = client.execute(new OperationBuilder(request).build());

            if ("success".equals(response.get(ClientConstants.OUTCOME).asString())) {
                resultList.addAll(response.get(ClientConstants.RESULT).asList());
            }
        } finally {
            safeClose(client);
        }
        return resultList;
    }

    private void safeClose(final Closeable closeable) {
        if (closeable != null)
            try {
                closeable.close();
            } catch (Exception e) {
                // no-op
            }
    }
}
