package org.jboss.wise.client.presenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerManager;
import org.jboss.wise.client.MainServiceAsync;
import org.jboss.wise.client.event.CancelledEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.wise.gui.ParamNode;

/**
 * User: rsearls
 * Date: 3/9/15
 */
public class EndpointConfigPresenter implements Presenter {

   public interface Display {
      HasClickHandlers getInvokeButton();

      HasClickHandlers getPreviewButton();

      HasClickHandlers getCancelButton();

      Widget asWidget();

      void setData(ParamNode data);
   }


   private final MainServiceAsync rpcService;
   private final HandlerManager eventBus;
   private final Display display;

   public EndpointConfigPresenter(MainServiceAsync rpcService, HandlerManager eventBus, Display display) {

      this.rpcService = rpcService;
      this.eventBus = eventBus;
      this.display = display;
      bind();
   }

   public EndpointConfigPresenter(MainServiceAsync rpcService, HandlerManager eventBus, Display display, String id) {

      this.rpcService = rpcService;
      this.eventBus = eventBus;
      this.display = display;
      bind();

      rpcService.getEndpointReflection(id, new AsyncCallback<ParamNode>() {
         public void onSuccess(ParamNode result) {

            EndpointConfigPresenter.this.display.setData(result);
         }

         public void onFailure(Throwable caught) {

            Window.alert("Error retrieving endpoint reflections");
         }
      });

   }

   public void bind() {

      this.display.getInvokeButton().addClickHandler(new ClickHandler() {
         public void onClick(ClickEvent event) {
            Window.alert("Not implemented");
            doInvoke();

         }
      });

      this.display.getPreviewButton().addClickHandler(new ClickHandler() {
         public void onClick(ClickEvent event) {
            Window.alert("Not implemented");
            doPreview();
         }
      });

      this.display.getCancelButton().addClickHandler(new ClickHandler() {
         public void onClick(ClickEvent event) {

            eventBus.fireEvent(new CancelledEvent());
         }
      });
   }

   public void go(final HasWidgets container) {

      container.clear();
      container.add(display.asWidget());
   }

   private void doInvoke() {
      /***
       rpcService.updateContact(contact, new AsyncCallback<Contact>() {
       public void onSuccess(Contact result) {
       eventBus.fireEvent(new ContactUpdatedEvent(result));
       }

       public void onFailure(Throwable caught) {
       Window.alert("Error updating contact");
       }
       });
       ***/
   }

   private void doPreview() {

   }
}
