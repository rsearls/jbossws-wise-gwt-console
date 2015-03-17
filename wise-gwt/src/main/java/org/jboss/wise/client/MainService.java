package org.jboss.wise.client;

import org.jboss.wise.gui.ParamNode;
import org.jboss.wise.shared.WsdlAddress;
import org.jboss.wise.shared.WsdlInfo;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.jboss.wise.gui.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * User: rsearls
 * Date: 3/8/15
 */
@RemoteServiceRelativePath("mainService")
public interface MainService extends RemoteService {
   ArrayList<WsdlAddress> getAddressDetails();
   WsdlAddress getAddress(String id);
   List<Service> getEndpoints(WsdlInfo wsdlInfo);
   ParamNode getEndpointReflection(String id);
}
