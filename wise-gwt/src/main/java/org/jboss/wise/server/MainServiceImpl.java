package org.jboss.wise.server;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.jboss.wise.client.MainService;
import org.jboss.wise.gui.ClientConversationBean;
import org.jboss.wise.gui.GWTClientConversationBean;
import org.jboss.wise.gui.Service;
import org.jboss.wise.gui.tree.element.GroupTreeElement;
import org.jboss.wise.gui.tree.element.RequestResponse;
import org.jboss.wise.gui.tree.element.SimpleTreeElement;
import org.jboss.wise.gui.tree.element.TreeElement;
import org.jboss.wise.shared.WsdlAddress;
import org.jboss.wise.shared.WsdlInfo;

/**
 * User: rsearls
 * Date: 3/8/15
 */
@SuppressWarnings("serial")
public class MainServiceImpl extends RemoteServiceServlet implements
   MainService {

   private final HashMap<String, WsdlAddress> address = new HashMap<String, WsdlAddress>();
   private ArrayList<WsdlAddress> wsdlAddress = new ArrayList<WsdlAddress>();
   private GWTClientConversationBean gwtClientConversationBean;

   public MainServiceImpl() {

      initAddress();
      gwtClientConversationBean = new GWTClientConversationBean();
   }

   private void initAddress() {
      // For debugging use only
      WsdlFinder wsdlFinder = new WsdlFinder();
      List<String> wsdlList = wsdlFinder.getWsdlList();
      for (int i = 0; i < wsdlList.size(); ++i) {
         WsdlAddress detail = new WsdlAddress(String.valueOf(i), wsdlList.get(i));
         wsdlAddress.add(detail);
         address.put(detail.getId(), detail);
      }
   }

   public ArrayList<WsdlAddress> getAddressDetails() {

      return wsdlAddress;
   }

   public WsdlAddress getAddress(String id) {

      return address.get(id);
   }

   public ArrayList<Service> getEndpoints(WsdlInfo wsdlInfo) {

      ArrayList<Service> endpointList = new ArrayList<Service>();
      if (wsdlInfo != null) {
         gwtClientConversationBean.setWsdlUser(wsdlInfo.getUser());
         gwtClientConversationBean.setWsdlPwd(wsdlInfo.getPassword());
         gwtClientConversationBean.setWsdlUrl(wsdlInfo.getWsdl());
         gwtClientConversationBean.readWsdl();
         List<Service> serviceList = gwtClientConversationBean.getServices();
         endpointList.addAll(serviceList);
      } else {
         Window.alert("URL information not specified");
      }
      return endpointList;
   }

   public RequestResponse getEndpointReflection(String id) {

      if (id != null) {
         gwtClientConversationBean.setCurrentOperation(id);
         return gwtClientConversationBean.parseOperationParameters(id);
      }
      return null;
   }

   public String getRequestPreview(TreeElement rootTreeElement) {

      return gwtClientConversationBean.generateRequestPreview(rootTreeElement);
   }

   public RequestResponse getPerformInvocationOutputTree(TreeElement rootTreeElement, WsdlInfo wsdlInfo) {

      gwtClientConversationBean.setInvocationUrl(wsdlInfo.getWsdl());
      gwtClientConversationBean.setInvocationUser(wsdlInfo.getUser());
      gwtClientConversationBean.setInvocationPwd(wsdlInfo.getPassword());

      return gwtClientConversationBean.performInvocation(rootTreeElement);
   }

   private String dumpTree(TreeElement root) {

      StringBuilder sb = new StringBuilder();

      if (root == null) {
         sb.append("root is NULL \n");
      } else {
         sb.append("--- \n");
         sb.append("root: arg name: " + root.getName()
            + "  classType: " + root.getClassType()
            + "  kind: " + root.getKind() + "\n");

         for (TreeElement te : root.getChildren()) {

            if (TreeElement.SIMPLE.equals(te.getKind())) {
               /**
                sb.append("child: arg name: " + te.getName()
                + "  classType: " + te.getClassType()
                + "  kind: " + te.getKind() + "\n");
                **/
               sb.append(te.getClassType() + " : " + te.getName() + " = "
                  + ((SimpleTreeElement)te).getValue() + "\n");

            } else if (TreeElement.GROUP.equals(te.getKind())) {
               sb.append("child: arg name: " + te.getName()
                  + "  classType: " + te.getClassType()
                  + "  rawType: " + ((GroupTreeElement) te).getProtoType().getClassType()
                  + "  kind: " + te.getKind() + "\n");

            }
            /***
             else if (TreeElement.ENUMERATION.equals(te.getKind())) {
             sb.append("Enum child: arg name: " + te.getName()
             + "  classType: " + te.getClassType()
             + "  kind: " + te.getKind() + "\n");

             for(String v : ((EnumerationTreeElement)te).getEnumValues()) {
             sb.append("Enum child: value" + v + "\n");
             }

             } else {
             sb.append("UNKNOW Kind: child: arg name: " + te.getName()
             + "  classTypeAsString: " + te.getClassType()
             + "  kind: " + te.getKind() + "\n");
             }
             ***/
         }
      }
      return sb.toString();
   }
}
