package org.jboss.wise.gwt.client.event;

import com.google.gwt.event.shared.EventHandler;
/**
 * User: rsearls
 * Date: 3/11/15
 */
public interface BackEventHandler extends EventHandler {
   void onBack(BackEvent event);
}