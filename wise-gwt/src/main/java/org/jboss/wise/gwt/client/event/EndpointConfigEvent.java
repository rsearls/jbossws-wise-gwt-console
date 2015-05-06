package org.jboss.wise.gwt.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * User: rsearls
 * Date: 3/9/15
 */
public class EndpointConfigEvent extends GwtEvent<EndpointConfigEventHandler> {
   public static Type<EndpointConfigEventHandler> TYPE = new Type<EndpointConfigEventHandler>();
   private final String id;

   public EndpointConfigEvent(String id) {

      this.id = id;
   }

   public String getId() {

      return id;
   }

   @Override
   public Type<EndpointConfigEventHandler> getAssociatedType() {

      return TYPE;
   }

   @Override
   protected void dispatch(EndpointConfigEventHandler handler) {

      handler.onEndpointConfig(this);
   }
}

