package org.jboss.wise.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * User: rsearls
 * Date: 3/5/15
 */
public interface SendWsdlEventHandler extends EventHandler {
    void onSendWsdl(SendWsdlEvent event);
}
