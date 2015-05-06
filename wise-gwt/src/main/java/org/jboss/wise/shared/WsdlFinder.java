package org.jboss.wise.shared;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Retrieve all wsdl-urls deployed to the current server.
 * User: rsearls
 * Date: 2/18/15
 */
public class WsdlFinder implements Serializable {

   private List<String> wsdlList;

   public WsdlFinder() {
      wsdlList = new ArrayList<String>();
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

      dumpList(wsdlList);
   }

   private void dumpList(List<String> wList) {
      for (String s : wList) {
         System.out.println("##--##--## wsdl-url: " + s);
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
