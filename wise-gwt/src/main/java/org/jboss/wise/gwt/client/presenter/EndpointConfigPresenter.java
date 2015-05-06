package org.jboss.wise.gwt.client.presenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.wise.gwt.client.MainServiceAsync;
import org.jboss.wise.gwt.client.event.BackEvent;
import org.jboss.wise.gwt.client.event.CancelledEvent;
import org.jboss.wise.gwt.client.event.InvocationEvent;
import org.jboss.wise.gwt.shared.tree.element.RequestResponse;
import org.jboss.wise.gwt.shared.tree.element.TreeElement;
import org.jboss.wise.gwt.shared.WsdlInfo;

/**
 * User: rsearls
 * Date: 3/9/15
 */
public class EndpointConfigPresenter implements Presenter {

   public interface Display {
      HasClickHandlers getInvokeButton();

      HasClickHandlers getPreviewButton();

      HasClickHandlers getCancelButton();

      HasClickHandlers getBackButton();

      Widget asWidget();

      void setData(RequestResponse data);

      TreeElement getParamsConfig();

      WsdlInfo getWsdlInfo();
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

      rpcService.getEndpointReflection(id, new AsyncCallback<RequestResponse>() {
         public void onSuccess(RequestResponse result) {

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

            doInvoke();
         }
      });

      this.display.getPreviewButton().addClickHandler(new ClickHandler() {
         public void onClick(ClickEvent event) {

            doPreview();
         }
      });

      this.display.getCancelButton().addClickHandler(new ClickHandler() {
         public void onClick(ClickEvent event) {

            eventBus.fireEvent(new CancelledEvent());
         }
      });

      this.display.getBackButton().addClickHandler(new ClickHandler() {
         public void onClick(ClickEvent event) {

            eventBus.fireEvent(new BackEvent());
         }
      });
   }

   public void go(final HasWidgets container) {

      container.clear();
      container.add(display.asWidget());
   }

   private void doInvoke() {

      TreeElement pNode = EndpointConfigPresenter.this.display.getParamsConfig();
      WsdlInfo wsdlInfo = EndpointConfigPresenter.this.display.getWsdlInfo();
      if (pNode == null) {
         Window.alert("getParamsConfig returned NULL");
      } else {
         eventBus.fireEvent(new InvocationEvent(pNode, wsdlInfo));
      }
   }

   private void doPreview() {

      TreeElement pNode = EndpointConfigPresenter.this.display.getParamsConfig();
      if (pNode == null) {
         Window.alert("getParamsConfig returned NULL");
      } else {
         rpcService.getRequestPreview(pNode, new AsyncCallback<String>() {
            public void onSuccess(String result) {

               if (result == null) {
                  Window.alert("getRequestPreview returned a NULL string ");
               } else {
                  Window.alert(result);
               }
            }

            public void onFailure(Throwable caught) {

               Window.alert("Error processing getRequestPreview");
            }
         });
      }
   }
}
