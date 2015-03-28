package org.jboss.wise.client.event;

import com.google.gwt.event.shared.GwtEvent;
import org.jboss.wise.gui.tree.element.TreeElement;

/**
 * User: rsearls
 * Date: 3/26/15
 */
public class InvocationEvent extends GwtEvent<InvocationEventHandler> {
   public static Type<InvocationEventHandler> TYPE = new Type<InvocationEventHandler>();
   private final TreeElement treeElement;

   public InvocationEvent(TreeElement treeElement) {

      this.treeElement = treeElement;
   }

   public TreeElement getId() {

      return treeElement;
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
