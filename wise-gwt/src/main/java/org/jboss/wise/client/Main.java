package org.jboss.wise.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * User: rsearls
 * Date: 3/8/15
 */
public class Main implements EntryPoint {

   public void onModuleLoad() {
      MainServiceAsync rpcService = GWT.create(MainService.class);
      HandlerManager eventBus = new HandlerManager(null);
      AppController appViewer = new AppController(rpcService, eventBus);
      appViewer.go(RootPanel.get());
   }
}
