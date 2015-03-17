package org.jboss.wise.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * User: rsearls
 * Date: 3/11/15
 */
public class BackEvent extends GwtEvent<BackEventHandler> {
   public static Type<BackEventHandler> TYPE = new Type<BackEventHandler>();

   @Override
   public Type<BackEventHandler> getAssociatedType() {

      return TYPE;
   }

   @Override
   protected void dispatch(BackEventHandler handler) {

      handler.onBack(this);
   }
}
