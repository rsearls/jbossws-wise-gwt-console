package org.jboss.wise.gwt.client.view;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.wise.gwt.client.presenter.InvocationPresenter;
import org.jboss.wise.gwt.shared.tree.element.ComplexTreeElement;
import org.jboss.wise.gwt.shared.tree.element.GroupTreeElement;
import org.jboss.wise.gwt.shared.tree.element.ParameterizedTreeElement;
import org.jboss.wise.gwt.shared.tree.element.RequestResponse;
import org.jboss.wise.gwt.shared.tree.element.SimpleTreeElement;
import org.jboss.wise.gwt.shared.tree.element.TreeElement;

/**
 * User: rsearls
 * Date: 3/26/15
 */
public class InvocationView extends Composite implements InvocationPresenter.Display {
   private final Button backButton;
   private final Button cancelButton;
   private final Button viewMessageButton;
   private Tree rootNode = null;
   private String responseMessage;

   public InvocationView() {

      DecoratorPanel contentDetailsDecorator = new DecoratorPanel();
      contentDetailsDecorator.setWidth("100%");
      contentDetailsDecorator.setWidth("640px");  //48em
      initWidget(contentDetailsDecorator);

      VerticalPanel contentDetailsPanel = new VerticalPanel();
      contentDetailsPanel.setStyleName("base-panel");
      contentDetailsPanel.setWidth("100%");
      rootNode = new Tree();
      rootNode.addItem(new TreeItem(SafeHtmlUtils.fromString("")));
      rootNode.setStyleName("invoke-tree");
      contentDetailsPanel.add(rootNode);

      HorizontalPanel menuPanel = new HorizontalPanel();
      menuPanel.setStyleName("menu-panel");
      backButton = new Button("Back");
      viewMessageButton = new Button("View Message");
      cancelButton = new Button("Cancel");
      menuPanel.add(backButton);
      menuPanel.add(viewMessageButton);
      menuPanel.add(cancelButton);
      contentDetailsPanel.add(menuPanel);
      contentDetailsDecorator.add(contentDetailsPanel);
   }

   public Tree getData() {

      return rootNode;
   }

   public HasClickHandlers getBackButton() {

      return backButton;
   }

   public HasClickHandlers getCancelButton() {

      return cancelButton;
   }

   public HasClickHandlers getViewMessageButton() {

      return viewMessageButton;
   }

   public Widget asWidget() {

      return this;
   }

   public String getResponseMessage() {

      return responseMessage;
   }

   public void setData(RequestResponse result) {
      responseMessage = result.getResponseMessage();
      TreeElement rootParamNode = result.getTreeElement();
      if (rootParamNode != null) {
         for (TreeElement child : rootParamNode.getChildren()) {
            TreeItem parentItem = generateDisplayObject(new TreeItem(), child);
            parentItem.setState(true);
            rootNode.addItem(parentItem.getChild(0));
         }
      }
   }


   protected TreeItem generateDisplayObject(TreeItem parentItem, TreeElement parentTreeElement) {

      if (TreeElement.SIMPLE.equals(parentTreeElement.getKind())) {
         TreeItem treeItem = new TreeItem();
         HorizontalPanel hPanel = new HorizontalPanel();
         treeItem.setWidget(hPanel);
         treeItem.setState(true);

         Label label = new Label(getClassType(parentTreeElement) + parentTreeElement.getName() + " = "
            + ((SimpleTreeElement)parentTreeElement).getValue());
         hPanel.add(label);

         parentItem.addItem(treeItem);

      } else if (parentTreeElement instanceof ComplexTreeElement) {
         TreeItem treeItem = new TreeItem();
         HorizontalPanel hPanel = new HorizontalPanel();
         treeItem.addItem(hPanel);
         treeItem.setState(true);

         treeItem.setText(getClassType(parentTreeElement) + parentTreeElement.getName());

         for (TreeElement child : parentTreeElement.getChildren()) {
            generateDisplayObject(treeItem, child);
         }

         parentItem.addItem(treeItem);

      } else if (parentTreeElement instanceof ParameterizedTreeElement) {
         TreeItem treeItem = new TreeItem();
         HorizontalPanel hPanel = new HorizontalPanel();
         treeItem.addItem(hPanel);
         treeItem.setState(true);

         treeItem.setText(getClassType(parentTreeElement) + parentTreeElement.getName());

         for (TreeElement child : parentTreeElement.getChildren()) {
            generateDisplayObject(treeItem, child);
         }

         parentItem.addItem(treeItem);

      } else if (parentTreeElement instanceof GroupTreeElement) {

         TreeItem treeItem = new TreeItem();
         HorizontalPanel gPanel = new HorizontalPanel();
         gPanel.add(new Label(getClassType(parentTreeElement)
            + "<" + getClassType(parentTreeElement) + ">"
            + " : " + parentTreeElement.getName()));
         treeItem.setWidget(gPanel);

         parentItem.addItem(treeItem);

      } else {
         TreeItem treeItem = new TreeItem();
         HorizontalPanel hPanel = new HorizontalPanel();
         treeItem.addItem(hPanel);
         treeItem.setState(true);

         treeItem.setText("UNKNOWN: " + getClassType(parentTreeElement) + parentTreeElement.getName());
         parentItem.addItem(treeItem);
      }

      return parentItem;
   }

   private String getClassType(TreeElement parentTreeElement) {
      String classTypeStr = "";
      if (parentTreeElement.getClassType() != null) {
         classTypeStr = EndpointConfigView.getBaseType(parentTreeElement.getClassType())
            + " : ";
      }
      return classTypeStr;
   }

}
