package org.jboss.wise.gwt.client.event;

import com.google.gwt.event.shared.GwtEvent;
import org.jboss.wise.gwt.shared.tree.element.TreeElement;
import org.jboss.wise.gwt.shared.WsdlInfo;

/**
 * User: rsearls
 * Date: 3/26/15
 */
public class InvocationEvent extends GwtEvent<InvocationEventHandler> {
   public static Type<InvocationEventHandler> TYPE = new Type<InvocationEventHandler>();
   private final TreeElement treeElement;
   private final WsdlInfo wsdlInfo;

   public InvocationEvent(TreeElement treeElement, WsdlInfo wsdlInfo) {

      this.treeElement = treeElement;
      this.wsdlInfo = wsdlInfo;
   }

   public TreeElement getId() {

      return treeElement;
   }

   public WsdlInfo getWsdlInfo() {
      return wsdlInfo;
   }

   @Override
   public Type<InvocationEventHandler> getAssociatedType() {

      return TYPE;
   }

   @Override
   protected void dispatch(InvocationEventHandler handler) {

      handler.onInvocation(this);
   }
}
