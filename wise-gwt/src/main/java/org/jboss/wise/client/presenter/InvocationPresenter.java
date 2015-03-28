package org.jboss.wise.client.presenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.wise.client.MainServiceAsync;
import org.jboss.wise.client.event.BackEvent;
import org.jboss.wise.gui.tree.element.MessageInvocationResult;
import org.jboss.wise.gui.tree.element.TreeElement;

/**
 * User: rsearls
 * Date: 3/26/15
 */
public class InvocationPresenter implements Presenter {
   public interface Display {
      HasClickHandlers getBackButton();

      HasClickHandlers getViewMessageButton();

      Tree getData();

      Widget asWidget();

      String getResponseMessage();

      void setData(MessageInvocationResult result);
   }

   private final MainServiceAsync rpcService;
   private final HandlerManager eventBus;
   private final Display display;

   public InvocationPresenter(MainServiceAsync rpcService, HandlerManager eventBus, Display display) {

      this.rpcService = rpcService;
      this.eventBus = eventBus;
      this.display = display;
      bind();
   }

   public InvocationPresenter(MainServiceAsync rpcService, HandlerManager eventBus, Display display, TreeElement treeElement) {

      this.rpcService = rpcService;
      this.eventBus = eventBus;
      this.display = display;
      bind();

      rpcService.getPerformInvocationOutputTree(treeElement, new AsyncCallback<MessageInvocationResult>() {
         public void onSuccess(MessageInvocationResult result) {

            InvocationPresenter.this.display.setData(result);
         }

         public void onFailure(Throwable caught) {

            Window.alert("Error PerformInvocationOutputTree");
         }
      });
   }

   public void bind() {

      this.display.getBackButton().addClickHandler(new ClickHandler() {
         public void onClick(ClickEvent event) {

            eventBus.fireEvent(new BackEvent());
         }
      });

      this.display.getViewMessageButton().addClickHandler(new ClickHandler() {
         public void onClick(ClickEvent event) {
            Window.alert(display.getResponseMessage());
         }
      });

   }

   public void go(final HasWidgets container) {

      container.clear();
      container.add(display.asWidget());
   }

}
