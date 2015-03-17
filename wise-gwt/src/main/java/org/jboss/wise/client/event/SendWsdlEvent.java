package org.jboss.wise.client.event;

import com.google.gwt.event.shared.GwtEvent;
import org.jboss.wise.shared.WsdlInfo;

/**
 * User: rsearls
 * Date: 3/5/15
 */
public class SendWsdlEvent extends GwtEvent<SendWsdlEventHandler> {
   public static Type<SendWsdlEventHandler> TYPE = new Type<SendWsdlEventHandler>();
   private final WsdlInfo wsdlInfo;

   public SendWsdlEvent(WsdlInfo wsdlInfo) {

      this.wsdlInfo = wsdlInfo;
   }

   public WsdlInfo getWsdlInfo() {

      return wsdlInfo;
   }

   @Override
   public Type<SendWsdlEventHandler> getAssociatedType() {

      return TYPE;
   }

   @Override
   protected void dispatch(SendWsdlEventHandler handler) {

      handler.onSendWsdl(this);
   }
}
