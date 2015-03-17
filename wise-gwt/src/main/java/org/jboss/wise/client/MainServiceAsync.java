package org.jboss.wise.client;

import org.jboss.wise.gui.ParamNode;
import org.jboss.wise.shared.WsdlAddress;
import org.jboss.wise.shared.WsdlInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.wise.gui.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * User: rsearls
 * Date: 3/8/15
 */
public interface MainServiceAsync {
   public void getAddressDetails(AsyncCallback<ArrayList<WsdlAddress>> callback);
   public void getAddress(String id, AsyncCallback<WsdlAddress> callback);
   public void getEndpoints(WsdlInfo wsdlInfo, AsyncCallback<List<Service>> callback);
   public void getEndpointReflection(String id, AsyncCallback<ParamNode> callback);
}
