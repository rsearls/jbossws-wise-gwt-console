package org.jboss.wise.client.view;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.jboss.wise.client.presenter.EndpointsPresenter;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Button;
import org.jboss.wise.gui.Operation;
import org.jboss.wise.gui.Port;
import org.jboss.wise.gui.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: rsearls
 * Date: 3/6/15
 */
public class EndpointsView extends Composite implements EndpointsPresenter.Display {
   private final Button backButton;

   private Map<TreeItem, Operation> endpointsMap = new HashMap<TreeItem, Operation>();
   private Tree rootNode = null;

   public EndpointsView() {

      DecoratorPanel contentDetailsDecorator = new DecoratorPanel();
      contentDetailsDecorator.setWidth("100%");
      contentDetailsDecorator.setWidth("640px");  //48em
      initWidget(contentDetailsDecorator);

      VerticalPanel contentDetailsPanel = new VerticalPanel();
      contentDetailsPanel.setStyleName("base-panel");
      contentDetailsPanel.setWidth("100%");
      rootNode = new Tree();
      rootNode.setStyleName("endpts-list-tree");
      rootNode.addItem(new TreeItem(SafeHtmlUtils.fromString("")));
      contentDetailsPanel.add(rootNode);

      HorizontalPanel menuPanel = new HorizontalPanel();
      menuPanel.setStyleName("menu-panel");
      backButton = new Button("Back");
      menuPanel.add(backButton);
      contentDetailsPanel.add(menuPanel);
      contentDetailsDecorator.add(contentDetailsPanel);
   }

   public Tree getData() {

      return rootNode;
   }

   public String getId(TreeItem treeItem) {

      if (treeItem != null) {
         Operation o = endpointsMap.get(treeItem);
         if (o != null) {
            return o.getCurrentOperation();
         }
      }
      return null;
   }

   public HasClickHandlers getBackButton() {

      return backButton;
   }

   public Widget asWidget() {

      return this;
   }

   public void setData(List<Service> data) {

      endpointsMap.clear();
      rootNode.removeItems();
      if (data != null) {
         for (Service s : data) {
            TreeItem serviceItem = new TreeItem(SafeHtmlUtils.fromString(s.getName()));
            rootNode.addItem(serviceItem);

            for (Port p : s.getPorts()) {
               TreeItem portItem = new TreeItem(SafeHtmlUtils.fromString(p.getName()));
               serviceItem.addItem(portItem);
               serviceItem.setState(true);

               for (Operation o : p.getOperations()) {
                  TreeItem tItem = new TreeItem(SafeHtmlUtils.fromString(o.getFullName()));
                  portItem.addItem(tItem);
                  portItem.setState(true);
                  endpointsMap.put(tItem, o);
               }
            }
         }
      }
   }
}
