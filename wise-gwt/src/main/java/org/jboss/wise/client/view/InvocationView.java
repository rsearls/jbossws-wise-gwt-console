package org.jboss.wise.client.view;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.wise.client.presenter.InvocationPresenter;
import org.jboss.wise.gui.tree.element.GroupTreeElement;
import org.jboss.wise.gui.tree.element.RequestResponse;
import org.jboss.wise.gui.tree.element.SimpleTreeElement;
import org.jboss.wise.gui.tree.element.TreeElement;

/**
 * User: rsearls
 * Date: 3/26/15
 */
public class InvocationView extends Composite implements InvocationPresenter.Display {
   private final Button backButton;
   private final Button viewMessageButton;
   private Tree rootNode = null;
   private String responseMessage;

   public InvocationView() {

      DecoratorPanel contentDetailsDecorator = new DecoratorPanel();
      contentDetailsDecorator.setWidth("48em");
      initWidget(contentDetailsDecorator);

      VerticalPanel contentDetailsPanel = new VerticalPanel();
      contentDetailsPanel.setWidth("100%");
      rootNode = new Tree();
      rootNode.addItem(new TreeItem(SafeHtmlUtils.fromString("")));
      contentDetailsPanel.add(rootNode);

      HorizontalPanel menuPanel = new HorizontalPanel();
      backButton = new Button("Back");
      viewMessageButton = new Button("View Message");
      menuPanel.add(backButton);
      menuPanel.add(viewMessageButton);
      contentDetailsPanel.add(menuPanel);
      contentDetailsDecorator.add(contentDetailsPanel);
   }

   public Tree getData() {

      return rootNode;
   }

   public HasClickHandlers getBackButton() {

      return backButton;
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
      TreeElement root = result.getTreeElement();

      if (root != null) {
         TreeItem opNameItem = new TreeItem(
            SafeHtmlUtils.fromString(result.getOperationFullName()));
         rootNode.addItem(opNameItem);

         for (TreeElement child : root.getChildren()) {
            if (child instanceof GroupTreeElement) {
               GroupTreeElement gChild = (GroupTreeElement) child;
               TreeElement cte = gChild.getProtoType();

               TreeItem resultItem = new TreeItem(
                  SafeHtmlUtils.fromString(getBaseName(gChild.getClassType())
                     + "[" + gChild.getValueList().size() + "]"));
               opNameItem.addItem(resultItem);

               for (TreeElement ste : gChild.getValueList()) {
                  TreeItem detailItem = new TreeItem(
                     SafeHtmlUtils.fromString(getBaseName(cte.getClassType()) + " : " + cte.getName() + " = "
                        + ((SimpleTreeElement) ste).getValue()));
                  resultItem.addItem(detailItem);
               }
               resultItem.setState(true);

            } else {
               TreeItem resultItem = new TreeItem(
                  SafeHtmlUtils.fromString(getBaseName(child.getClassType()) + " : " + child.getName()
                     + " = " + ((SimpleTreeElement) child).getValue()));
               opNameItem.addItem(resultItem);
            }
         }
         opNameItem.setState(true);
      } else {
         // display error
         TreeItem opNameItem = new TreeItem(SafeHtmlUtils.fromString(result.getOperationFullName()));
         rootNode.addItem(opNameItem);

         TreeItem errorItem = new TreeItem(
            SafeHtmlUtils.fromString(result.getErrorMessage()));
         opNameItem.addItem(errorItem);
         opNameItem.setState(true);
      }
   }

   private String getBaseName(String name) {

      String basename = name;
      int indx = name.lastIndexOf(".");
      if (indx > -1) {
         basename = name.substring(indx + 1);
      }
      return basename;
   }
}
