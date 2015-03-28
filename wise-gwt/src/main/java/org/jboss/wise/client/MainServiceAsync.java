package org.jboss.wise.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.ArrayList;
import java.util.List;
import org.jboss.wise.gui.Service;
import org.jboss.wise.gui.tree.element.MessageInvocationResult;
import org.jboss.wise.gui.tree.element.TreeElement;
import org.jboss.wise.shared.WsdlAddress;
import org.jboss.wise.shared.WsdlInfo;

/**
 * User: rsearls
 * Date: 3/8/15
 */
public interface MainServiceAsync {
   public void getAddressDetails(AsyncCallback<ArrayList<WsdlAddress>> callback);
   public void getAddress(String id, AsyncCallback<WsdlAddress> callback);
   public void getEndpoints(WsdlInfo wsdlInfo, AsyncCallback<List<Service>> callback);
   public void getEndpointReflection(String id, AsyncCallback<TreeElement> callback);
   public void getRequestPreview(TreeElement rootTreeElement, AsyncCallback<String> callback);
   public void getPerformInvocationOutputTree(TreeElement rootTreeElement, AsyncCallback<MessageInvocationResult> callback);
}
