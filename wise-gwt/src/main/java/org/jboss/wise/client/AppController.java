package org.jboss.wise.client;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import org.jboss.wise.client.event.BackEvent;
import org.jboss.wise.client.event.BackEventHandler;
import org.jboss.wise.client.event.CancelledEvent;
import org.jboss.wise.client.event.CancelledEventHandler;
import org.jboss.wise.client.event.EndpointConfigEvent;
import org.jboss.wise.client.event.SendWsdlEventHandler;
import org.jboss.wise.client.presenter.EndpointsPresenter;
import org.jboss.wise.client.presenter.Presenter;
import org.jboss.wise.client.presenter.WsdlPresenter;
import org.jboss.wise.client.view.EndpointConfigView;
import org.jboss.wise.client.view.EndpointsView;
import org.jboss.wise.shared.WsdlInfo;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.HasWidgets;
import org.jboss.wise.client.event.EndpointConfigEventHandler;
import org.jboss.wise.client.event.SendWsdlEvent;
import org.jboss.wise.client.presenter.EndpointConfigPresenter;
import org.jboss.wise.client.view.WsdlView;

public class AppController implements Presenter, ValueChangeHandler<String> {
   private final HandlerManager eventBus;
   private final MainServiceAsync rpcService;
   private HasWidgets container;

   public AppController(MainServiceAsync rpcService, HandlerManager eventBus) {

      this.eventBus = eventBus;
      this.rpcService = rpcService;
      bind();
   }

   private void bind() {

      History.addValueChangeHandler(this);

      //- page1 send event.
      eventBus.addHandler(SendWsdlEvent.TYPE,
         new SendWsdlEventHandler() {
            public void onSendWsdl(SendWsdlEvent event) {

               doSendWsdl(event.getWsdlInfo());
            }
         });

      eventBus.addHandler(CancelledEvent.TYPE,
         new CancelledEventHandler() {
            public void onCancelled(CancelledEvent event) {

               doCancelled();
            }
         });

      eventBus.addHandler(BackEvent.TYPE,
         new BackEventHandler() {
            public void onBack(BackEvent event) {

               doBack();
            }
         });

      eventBus.addHandler(EndpointConfigEvent.TYPE,
         new EndpointConfigEventHandler() {
            public void onEndpointConfig(EndpointConfigEvent event) {

               doEndpointConfig(event.getId());
            }
         });

   }

   private void doSendWsdl(WsdlInfo wsdlInfo) {

      History.newItem("endpoints", false);
      Presenter presenter = new EndpointsPresenter(rpcService, eventBus, new EndpointsView(), wsdlInfo);
      presenter.go(container);
   }

   private void doEndpointConfig(String id) {

      History.newItem("config", false);
      Presenter presenter = new EndpointConfigPresenter(rpcService, eventBus, new EndpointConfigView(), id);
      presenter.go(container);
   }

   private void doCancelled() {

      History.newItem("list");
      //History.back();   // must reset prev data.
   }

   private void doBack() {

      History.newItem("list");
   }

   public void go(final HasWidgets container) {

      this.container = container;

      if ("".equals(History.getToken())) {
         History.newItem("list");
      } else {
         History.fireCurrentHistoryState();
      }
   }

   public void onValueChange(ValueChangeEvent<String> event) {

      String token = event.getValue();

      if (token != null) {
         Presenter presenter = null;

         if (token.equals("list")) {
            presenter = new WsdlPresenter(rpcService, eventBus, new WsdlView());
         } else if (token.equals("endpoints")) {
            presenter = new EndpointsPresenter(rpcService, eventBus, new EndpointsView());
         } else if (token.equals("config")) {
            presenter = new EndpointConfigPresenter(rpcService, eventBus, new EndpointConfigView());
         }

         if (presenter != null) {
            presenter.go(container);
         }
      }
   }
}
