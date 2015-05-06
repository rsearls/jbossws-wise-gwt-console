package org.jboss.wise.gwt.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * User: rsearls
 * Date: 3/26/15
 */
public interface InvocationEventHandler extends EventHandler {
   void onInvocation(InvocationEvent event);
}