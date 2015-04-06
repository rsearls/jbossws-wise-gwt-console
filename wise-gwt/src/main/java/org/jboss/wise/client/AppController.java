package org.jboss.wise.client;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import java.util.HashMap;
import org.jboss.wise.client.event.BackEvent;
import org.jboss.wise.client.event.BackEventHandler;
import org.jboss.wise.client.event.CancelledEvent;
import org.jboss.wise.client.event.CancelledEventHandler;
import org.jboss.wise.client.event.EndpointConfigEvent;
import org.jboss.wise.client.event.InvocationEvent;
import org.jboss.wise.client.event.InvocationEventHandler;
import org.jboss.wise.client.event.SendWsdlEventHandler;
import org.jboss.wise.client.presenter.EndpointsPresenter;
import org.jboss.wise.client.presenter.InvocationPresenter;
import org.jboss.wise.client.presenter.Presenter;
import org.jboss.wise.client.presenter.WsdlPresenter;
import org.jboss.wise.client.view.EndpointConfigView;
import org.jboss.wise.client.view.EndpointsView;
import org.jboss.wise.client.view.InvocationView;
import org.jboss.wise.gui.tree.element.TreeElement;
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
   private HashMap<String, Presenter> presenterMap = new HashMap<String, Presenter>();

   public AppController(MainServiceAsync rpcService, HandlerManager eventBus) {

      this.eventBus = eventBus;
      this.rpcService = rpcService;
      bind();
      initPresenters();
   }

   private void bind() {

      History.addValueChangeHandler(this);

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

      eventBus.addHandler(InvocationEvent.TYPE,
         new InvocationEventHandler() {
            public void onInvocation(InvocationEvent event) {

               doInvocation(event.getId(), event.getWsdlInfo());
            }
         });
   }

   private void initPresenters(){
      presenterMap.clear();
      presenterMap.put("list", new WsdlPresenter(rpcService, eventBus, new WsdlView()));
   }

   private void doSendWsdl(WsdlInfo wsdlInfo) {

      History.newItem("endpoints", false);
      Presenter presenter = new EndpointsPresenter(rpcService, eventBus, new EndpointsView(), wsdlInfo);
      presenterMap.put("endpoints", presenter);
      presenter.go(container);
   }

   private void doEndpointConfig(String id) {

      History.newItem("config", false);
      Presenter presenter = new EndpointConfigPresenter(rpcService, eventBus, new EndpointConfigView(), id);
      presenterMap.put("config", presenter);
      presenter.go(container);
   }

   private void doInvocation (TreeElement treeElement, WsdlInfo wsdlInfo) {
      History.newItem("invoke", false);
      Presenter presenter = new InvocationPresenter(rpcService, eventBus, new InvocationView(),
         treeElement, wsdlInfo);
      presenterMap.put("invoke", presenter);
      presenter.go(container);
   }

   private void doCancelled() {

      initPresenters();
      History.newItem("list", false);
      Presenter presenter = presenterMap.get("list");
      presenter.go(container);
   }

   private void doBack() {

      History.back();
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
            initPresenters();
         }
         presenter = presenterMap.get(token);

         if (presenter != null) {
            presenter.go(container);
         }
      }
   }
}
