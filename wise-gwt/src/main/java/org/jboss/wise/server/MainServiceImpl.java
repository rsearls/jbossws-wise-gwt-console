package org.jboss.wise.server;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.jboss.wise.client.MainService;
import org.jboss.wise.gui.ClientConversationBean;
import org.jboss.wise.gui.ParamNode;
import org.jboss.wise.gui.Service;
import org.jboss.wise.shared.WsdlAddress;
import org.jboss.wise.shared.WsdlInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * User: rsearls
 * Date: 3/8/15
 */
@SuppressWarnings("serial")
public class MainServiceImpl extends RemoteServiceServlet implements
   MainService {

   private final HashMap<String, WsdlAddress> address = new HashMap<String, WsdlAddress>();
   private ArrayList<WsdlAddress> wsdlAddress = new ArrayList<WsdlAddress>();
   private ClientConversationBean clientConversationBean;

   public MainServiceImpl() {

      //initAddress();
      clientConversationBean = new ClientConversationBean();
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
         clientConversationBean.setWsdlUser(wsdlInfo.getUser());
         clientConversationBean.setWsdlPwd(wsdlInfo.getPassword());
         clientConversationBean.setWsdlUrl(wsdlInfo.getWsdl());
         clientConversationBean.readWsdl();
         List<Service> serviceList = clientConversationBean.getServices();
         clientConversationBean.debugOprParams();  // debug only
         endpointList.addAll(serviceList);
      } else {
         //endpointList.add("getEndpoints: idWSDL was null");
         Window.alert("URL information not specified");
      }
      return endpointList;
   }

   public ParamNode getEndpointReflection(String id) {

      if (id != null) {
         return clientConversationBean.getParamNode(id);
      }
      return null;
   }

}
