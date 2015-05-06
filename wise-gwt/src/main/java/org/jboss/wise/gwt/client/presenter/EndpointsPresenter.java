package org.jboss.wise.gwt.client.presenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import java.util.List;
import org.jboss.wise.gwt.client.MainServiceAsync;
import org.jboss.wise.gwt.client.event.BackEvent;
import org.jboss.wise.gwt.client.event.EndpointConfigEvent;
import org.jboss.wise.gwt.shared.Service;
import org.jboss.wise.gwt.shared.WsdlInfo;

/**
 * User: rsearls
 * Date: 3/6/15
 */
public class EndpointsPresenter implements Presenter {
   public interface Display {
      HasClickHandlers getBackButton();

      Tree getData();

      String getId(TreeItem tItem);

      Widget asWidget();

      void setData(List<Service> data);
   }

   private final MainServiceAsync rpcService;
   private final HandlerManager eventBus;
   private final Display display;

   public EndpointsPresenter(MainServiceAsync rpcService, HandlerManager eventBus, Display display) {

      this.rpcService = rpcService;
      this.eventBus = eventBus;
      this.display = display;
      bind();
   }

   public EndpointsPresenter(MainServiceAsync rpcService, HandlerManager eventBus, Display display, WsdlInfo wsdlInfo) {

      this.rpcService = rpcService;
      this.eventBus = eventBus;
      this.display = display;
      bind();

      rpcService.getEndpoints(wsdlInfo, new AsyncCallback<List<Service>>() {
         public void onSuccess(List<Service> result) {

            EndpointsPresenter.this.display.setData(result);
         }

         public void onFailure(Throwable caught) {

            Window.alert("Error retrieving endpoints");
         }
      });
   }

   public void bind() {

      this.display.getBackButton().addClickHandler(new ClickHandler() {
         public void onClick(ClickEvent event) {

            eventBus.fireEvent(new BackEvent());
         }
      });

      this.display.getData().addSelectionHandler(new SelectionHandler<TreeItem>() {
         public void onSelection(SelectionEvent<TreeItem> event) {

            TreeItem tItem = event.getSelectedItem();
            String id = display.getId(tItem);
            if (id != null) {
               eventBus.fireEvent(new EndpointConfigEvent(id));
            }
         }
      });
   }

   public void go(final HasWidgets container) {

      container.clear();
      container.add(display.asWidget());
   }

}
