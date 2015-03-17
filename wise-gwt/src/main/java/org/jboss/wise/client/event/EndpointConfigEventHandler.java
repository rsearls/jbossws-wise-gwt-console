package org.jboss.wise.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * User: rsearls
 * Date: 3/9/15
 */
public interface EndpointConfigEventHandler extends EventHandler {
   void onEndpointConfig(EndpointConfigEvent event);
}