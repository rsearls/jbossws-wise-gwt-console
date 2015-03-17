package org.jboss.wise.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface CancelledEventHandler extends EventHandler {
  void onCancelled(CancelledEvent event);
}
