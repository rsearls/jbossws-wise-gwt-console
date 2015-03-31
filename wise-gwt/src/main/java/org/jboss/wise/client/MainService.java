package org.jboss.wise.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.ArrayList;
import java.util.List;
import org.jboss.wise.gui.Service;
import org.jboss.wise.gui.tree.element.RequestResponse;
import org.jboss.wise.gui.tree.element.RequestResponse;
import org.jboss.wise.gui.tree.element.TreeElement;
import org.jboss.wise.shared.WsdlAddress;
import org.jboss.wise.shared.WsdlInfo;

/**
 * User: rsearls
 * Date: 3/8/15
 */
@RemoteServiceRelativePath("mainService")
public interface MainService extends RemoteService {
   ArrayList<WsdlAddress> getAddressDetails();
   WsdlAddress getAddress(String id);
   List<Service> getEndpoints(WsdlInfo wsdlInfo);
   RequestResponse getEndpointReflection(String id);
   String getRequestPreview(TreeElement rootTreeElement);
   RequestResponse getPerformInvocationOutputTree(TreeElement rootTreeElement, WsdlInfo wsdlInfo);
}
