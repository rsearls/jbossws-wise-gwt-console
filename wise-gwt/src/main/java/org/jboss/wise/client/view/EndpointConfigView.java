package org.jboss.wise.client.view;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jboss.wise.client.presenter.EndpointConfigPresenter;
import org.jboss.wise.gui.tree.element.EnumerationTreeElement;
import org.jboss.wise.gui.tree.element.GroupTreeElement;
import org.jboss.wise.gui.tree.element.RequestResponse;
import org.jboss.wise.gui.tree.element.SimpleTreeElement;
import org.jboss.wise.gui.tree.element.TreeElement;
import org.jboss.wise.shared.WsdlInfo;

/**
 * User: rsearls
 * Date: 3/9/15
 */
public class EndpointConfigView extends Composite implements EndpointConfigPresenter.Display {

   private final int COL_ONE = 0;
   private final int COL_TWO = 1;
   private final int COL_THREE = 2;

   private final int COL_LABEL = 0;
   private final int COL_ADD_BUTTON = 1;
   private final int COL_INFIELD = 2;
   private final int COL_REMOVE_BUTTON = 3;

   private final Button invokeButton;
   private final Button previewButton;
   private final Button cancelButton;
   private final Button backButton;
   private TextBox debugBox = new TextBox();

   private LinkedHashMap<Widget, TreeElement> paramWidgetTable =
      new LinkedHashMap<Widget, TreeElement>();
   private VerticalPanel baseVerticalPanel;
   private TreeElement rootParamNode = null;
   private List<FlexTable> flexTableList = new ArrayList<FlexTable>();
   private Map<Widget, GroupTreeElement> groupTreeWidgetMap = new HashMap<Widget, GroupTreeElement>();
   private RequestResponse  msgInvocationResult;
   private int widgetCountOffset = 0;

   private TextBox wsdlAddress;
   private TextBox user;
   private PasswordTextBox password;


   public EndpointConfigView() {

      DecoratorPanel contentDetailsDecorator = new DecoratorPanel();
      contentDetailsDecorator.setWidth("48em");
      initWidget(contentDetailsDecorator);

      baseVerticalPanel = new VerticalPanel();
      baseVerticalPanel.setWidth("100%");

      FlexTable fTable = createCredentialOverRidePanel();
      baseVerticalPanel.add(fTable);

      HorizontalPanel menuPanel = new HorizontalPanel();
      invokeButton = new Button("Invoke");
      cancelButton = new Button("Cancel");
      previewButton = new Button("Preview Message");
      backButton = new Button("Back");
      menuPanel.add(backButton);
      menuPanel.add(previewButton);
      menuPanel.add(invokeButton);
      menuPanel.add(cancelButton);
      baseVerticalPanel.add(menuPanel);

      contentDetailsDecorator.add(baseVerticalPanel);
      widgetCountOffset = 2;
   }

   public HasClickHandlers getInvokeButton() {

      return invokeButton;
   }

   public HasClickHandlers getPreviewButton() {

      return previewButton;
   }

   public HasClickHandlers getCancelButton() {

      return cancelButton;
   }

   public HasClickHandlers getBackButton() {

      return backButton;
   }

   public Widget asWidget() {

      return this;
   }

   public void setData(RequestResponse data) {
      msgInvocationResult = data;
      rootParamNode = data.getTreeElement();
      processData(rootParamNode.getChildren());
   }


   private void processData(List<TreeElement> pNodeList) {

      baseVerticalPanel.insert(createFullnamePanel(), baseVerticalPanel.getWidgetCount() - widgetCountOffset);

      //- TODO handle case where no input args
      if (pNodeList.isEmpty()) {
         HorizontalPanel hPanel = createNoParamPanel();
         baseVerticalPanel.insert(hPanel, baseVerticalPanel.getWidgetCount() - widgetCountOffset);

      } else {
         for (TreeElement pNode : pNodeList) {

            if (pNode instanceof GroupTreeElement) {
               FlexTable fTable = createParamaterizedPanel((GroupTreeElement) pNode);
               baseVerticalPanel.insert(fTable, baseVerticalPanel.getWidgetCount() - widgetCountOffset);

               debugBox.setText("paramsTable RowCount: " + Integer.toString(fTable.getRowCount())
                  + "  widget cnt: " + Integer.toString(baseVerticalPanel.getWidgetCount()));

            } else if (pNode instanceof EnumerationTreeElement) {
               // TODO
               HorizontalPanel hPanel = createEnumerationPanel((EnumerationTreeElement) pNode);
               baseVerticalPanel.insert(hPanel, baseVerticalPanel.getWidgetCount() - widgetCountOffset);
            } else {
               if (TreeElement.SIMPLE.equals(pNode.getKind())) {
                  HorizontalPanel hPanel = createSimpleParamsPanel(pNode);
                  baseVerticalPanel.insert(hPanel, baseVerticalPanel.getWidgetCount() - widgetCountOffset);

               } else if (TreeElement.COMPLEX.equals(pNode.getKind())) {
                  FlexTable fTable = createComplexParamsPanel(pNode);
                  baseVerticalPanel.insert(fTable, baseVerticalPanel.getWidgetCount() - widgetCountOffset);
               }
            }
         }
      }
   }


   private HorizontalPanel createEnumerationPanel(EnumerationTreeElement eNode) {

      HorizontalPanel hPanel = new HorizontalPanel();
      Label label = new Label(getBaseType(eNode.getClassType()));
      hPanel.add(label);
      ListBox lBox = new ListBox();
      lBox.setSelectedIndex(-1);
      hPanel.add(lBox);
      paramWidgetTable.put(lBox, eNode);

      // put emun names in the list
      lBox.addItem(""); // unselected designator first
      for (String s : eNode.getEnumValues()) {
         lBox.addItem(s);
      }

      return hPanel;
   }

   private FlexTable createParamaterizedPanel(GroupTreeElement pNode) {

      FlexTable fTable = new FlexTable();
      flexTableList.add(fTable);
      fTable.setCellSpacing(2);
      fTable.setCellPadding(2);
      fTable.setWidth("100%");

      fTable.getColumnFormatter().setWidth(COL_LABEL, "20%");
      fTable.getColumnFormatter().setWidth(COL_ADD_BUTTON, "40%");
      fTable.getColumnFormatter().setWidth(COL_INFIELD, "20%");
      fTable.getColumnFormatter().setWidth(COL_REMOVE_BUTTON, "20%");

      Widget widget = getWidget(pNode.getProtoType());
      paramWidgetTable.put(widget, pNode.getProtoType());
      groupTreeWidgetMap.put(widget, pNode);

      fTable.setWidget(0, COL_LABEL, getLabel(pNode));
      fTable.setWidget(0, COL_INFIELD, widget);
      widget.setVisible(false);

      // debug
      if (widget instanceof TextBox) {
         debugBox = (TextBox) widget;
      }

      Button addButton = new Button("add");
      fTable.setWidget(0, COL_ADD_BUTTON, addButton);

      debugBox.setText("paramsTable RowCount: " + Integer.toString(fTable.getRowCount())
         + "  widget cnt: " + Integer.toString(baseVerticalPanel.getWidgetCount()));

      addButton.addClickHandler(new AddParamerterizeRowClickHandler(this,
         paramWidgetTable, fTable));
      return fTable;
   }

   private HorizontalPanel createSimpleParamsPanel(TreeElement pNode) {

      HorizontalPanel hPanel = new HorizontalPanel();

      hPanel.add(getLabel(pNode));
      Widget w = getWidget(pNode);
      hPanel.add(w);
      paramWidgetTable.put(w, pNode);
      return hPanel;
   }

   private FlexTable createComplexParamsPanel(TreeElement pNode) {

      FlexTable fTable = new FlexTable();
      fTable.setCellSpacing(2);
      fTable.setCellPadding(2);
      fTable.setWidth("100%");

      fTable.getColumnFormatter().setWidth(COL_LABEL, "20%");
      fTable.getColumnFormatter().setWidth(COL_ADD_BUTTON, "40%");
      fTable.getColumnFormatter().setWidth(COL_INFIELD, "20%");
      fTable.getColumnFormatter().setWidth(COL_REMOVE_BUTTON, "20%");

      Label header = new Label(getBaseType(pNode.getClassType()) + " : " + pNode.getName());

      fTable.setWidget(0, COL_ONE, header);

      int row = 0;
      for (TreeElement ste : pNode.getChildren()) {
         row++;
         //String s = getBaseType(ste.getClassType()) + " : " + ste.getName();
         Label label = new Label(getBaseType(ste.getClassType()) + " : " + ste.getName());
         Widget widget = getWidget(ste);

         fTable.setWidget(row, COL_TWO, label);
         fTable.setWidget(row, COL_THREE, widget);

         if (!(widget instanceof Label)) {
            paramWidgetTable.put(widget, ste);
         }
      }

      return fTable;
   }

   private HorizontalPanel createFullnamePanel() {
      HorizontalPanel hPanel = new HorizontalPanel();
      hPanel.add(new Label(msgInvocationResult.getOperationFullName()));
      return hPanel;
   }

   private /*VerticalPanel*/ FlexTable createCredentialOverRidePanel() {
      /*******
      VerticalPanel vPanel = new VerticalPanel();
      HorizontalPanel urlPanel = new HorizontalPanel();
      urlPanel.add(new Label("Override target address: "));
      urlPanel.add(new TextBox());

      HorizontalPanel namePanel = new HorizontalPanel();
      namePanel.add(new Label("User: "));
      namePanel.add(new TextBox());

      HorizontalPanel passwordPanel = new HorizontalPanel();
      passwordPanel.add(new Label("Password: "));
      passwordPanel.add(new PasswordTextBox());

      vPanel.add(urlPanel);
      vPanel.add(namePanel);
      vPanel.add(passwordPanel);

      return vPanel;
      ********/
      FlexTable fTable = new FlexTable();
      fTable.setCellSpacing(2);
      fTable.setCellPadding(2);
      fTable.setBorderWidth(2);
      fTable.setWidth("100%");

      fTable.getColumnFormatter().setWidth(COL_ONE, "20%");
      fTable.getColumnFormatter().setWidth(COL_TWO, "40%");

      wsdlAddress = new TextBox();
      wsdlAddress.setWidth("28em");
      user = new TextBox();
      password = new PasswordTextBox();

      fTable.setWidget(0, COL_ONE, new Label("Override target address: "));
      fTable.setWidget(0, COL_TWO, wsdlAddress);

      fTable.setWidget(1, COL_ONE, new Label("User: "));
      fTable.setWidget(1, COL_TWO, user);

      fTable.setWidget(2, COL_ONE, new Label("Password: "));
      fTable.setWidget(2, COL_TWO, password);

      return fTable;
   }

   private HorizontalPanel createNoParamPanel() {

      HorizontalPanel hPanel = new HorizontalPanel();
      hPanel.add(new Label("Method has no input parameters."));
      return hPanel;
   }

   private Widget getWidget(TreeElement pNode) {

      if ("java.lang.String".endsWith(pNode.getClassType())) {
         return new TextBox();
      } else if ("java.lang.Integer".equals(pNode.getClassType())) {
         return new IntegerBox();
      } else if ("java.lang.Double".equals(pNode.getClassType())) {
         return new DoubleBox();
      }
      return new Label("UNKNOWN TYPE: " + pNode.getClassType());
   }

   private Label getLabel(TreeElement pNode) {

      String typeLabel = "";
      if (pNode instanceof GroupTreeElement) {
         typeLabel = getBaseType(((GroupTreeElement) pNode).getClassType())
            + "<" + getBaseType(((GroupTreeElement) pNode).getProtoType().getClassType())
            + ">:" + pNode.getName();

      } else {
         if ("java.lang.String".endsWith(pNode.getClassType())) {
            typeLabel = "String:" + pNode.getName();
         } else if ("java.lang.Integer".equals(pNode.getClassType())) {
            typeLabel = "Integer:" + pNode.getName();
         } else if ("java.lang.Double".equals(pNode.getClassType())) {
            typeLabel = "Double:" + pNode.getName();
         }
      }
      return new Label(typeLabel);
   }

   private String getBaseType(String src) {

      int indx = src.lastIndexOf(".");
      String t = src;
      if (indx > -1) {
         t = src.substring(indx + 1);
      }
      return t;
   }

   public WsdlInfo getWsdlInfo() {
      return new WsdlInfo(wsdlAddress.getValue(), user.getValue(), password.getValue());
   }

   /**
    * @return
    */
   public TreeElement getParamsConfig() {

      //StringBuilder sb = new StringBuilder();

      // identify the root (reference) widget of each FlexTable
      List<Widget> flexTableRootWidget = new ArrayList<Widget>();
      for (FlexTable fTable : flexTableList) {
         flexTableRootWidget.add(fTable.getWidget(0, COL_INFIELD));
      }

      // clear all pre-existing input data
      for (GroupTreeElement gte : groupTreeWidgetMap.values()) {
         gte.getValueList().clear();
      }

      for (Map.Entry<Widget, TreeElement> entry : paramWidgetTable.entrySet()) {
         Widget widget = entry.getKey();
         TreeElement pNode = entry.getValue();

         if (!flexTableRootWidget.contains(widget)) {
            GroupTreeElement gTreeElement = groupTreeWidgetMap.get(widget);
            if (gTreeElement != null) {
               SimpleTreeElement clone = (SimpleTreeElement) pNode.clone();
               TreeElement te = getWidgetValue(widget, clone);
               gTreeElement.addValue(te);

            } else if (pNode instanceof EnumerationTreeElement) {
               ListBox lBox = (ListBox) widget;
               EnumerationTreeElement eNode = (EnumerationTreeElement) pNode;

               int index = lBox.getSelectedIndex();
               eNode.setValue(lBox.getItemText(index));
               // debug
               //Window.alert("index: " + index + "  value: " + eNode.getValue()
               //   + "  Nil: " + eNode.isNil());

            } else {
               getWidgetValue(widget, (SimpleTreeElement) pNode);
               //sb.append("name: " + pNode.getName() + "  value: "
               //   + ((SimpleTreeElement) pNode).getValue()  + "\n");
            }
         }
      }
      //Window.alert("config results: " + sb.toString() + "\n");
      return rootParamNode;
   }

   private TreeElement getWidgetValue(Widget widget, SimpleTreeElement pNode) {

      if (widget instanceof TextBox) {
         String s = ((TextBox) widget).getValue();
         if (s != null && !s.isEmpty()) {
            pNode.setValue(s);
         }

      } else if (widget instanceof IntegerBox) {
         Integer i = ((IntegerBox) widget).getValue();
         if (i != null) {
            pNode.setValue(i.toString());
         }

      } else if (widget instanceof DoubleBox) {
         Double d = ((DoubleBox) widget).getValue();
         if (d != null) {
            pNode.setValue(d.toString());
         }
      }
      return pNode;
   }

   /**
    *
    */
   public class AddParamerterizeRowClickHandler implements ClickHandler {
      private EndpointConfigView endpointConfigView;
      private FlexTable fTable;
      private LinkedHashMap<Widget, TreeElement> paramWidgetTable;

      public AddParamerterizeRowClickHandler(EndpointConfigView endpointConfigView,
                                             LinkedHashMap<Widget, TreeElement> paramWidgetTable,
                                             FlexTable fTable) {

         this.endpointConfigView = endpointConfigView;
         this.paramWidgetTable = paramWidgetTable;
         this.fTable = fTable;
      }

      public void onClick(ClickEvent event) {

         insertRowAfter();
      }

      private void insertRowAfter() {

         int lastRow = fTable.getRowCount();
         int next = fTable.insertRow(lastRow);

         debugBox.setText("lastRow: " + Integer.toString(lastRow)
            + "  added next: " + Integer.toString(next)
            + "  RowCount: " + Integer.toString(fTable.getRowCount()));  // rls
         // get ref obj
         Widget srcWidget = fTable.getWidget(0, COL_INFIELD);
         TreeElement pNode = paramWidgetTable.get(srcWidget);

         Widget widget = endpointConfigView.getWidget(pNode);

         GroupTreeElement srcPNode = groupTreeWidgetMap.get(srcWidget);
         if (srcPNode != null) {
            groupTreeWidgetMap.put(widget, srcPNode);
         }

         //- debugging only
         //if (widget instanceof TextBox) {
         //  TextBox tmpWidget = (TextBox)widget;
         //  tmpWidget.setValue(Integer.toString(next) + " next: " + Integer.toString(next)
         //     + "  RowCount: " + Integer.toString(fTable.getRowCount()));  //- debug
         //}

         fTable.setWidget(next, COL_INFIELD, widget);

         paramWidgetTable.put(widget, pNode);

         final Button rmButton = new Button("remove");
         //final Button rmButton = new Button("remove " + Integer.toString(next)); // debug
         fTable.setWidget(next, COL_REMOVE_BUTTON, rmButton);

         rmButton.addClickHandler(new RemoveParamerterizeRowClickHandler(
            paramWidgetTable, fTable));
      }

   }

   /**
    *
    */
   public class RemoveParamerterizeRowClickHandler implements ClickHandler {
      private FlexTable fTable;
      private LinkedHashMap<Widget, TreeElement> paramWidgetTable;

      public RemoveParamerterizeRowClickHandler(LinkedHashMap<Widget, TreeElement> paramWidgetTable,
                                                FlexTable fTable) {

         this.paramWidgetTable = paramWidgetTable;
         this.fTable = fTable;
      }

      public void onClick(ClickEvent event) {

         int rowIndex = fTable.getCellForEvent(event).getRowIndex();
         removeRow(rowIndex);
         debugBox.setText("remove event rowIndex: " + Integer.toString(rowIndex)
            + "  table size: " + Integer.toString(fTable.getRowCount()));  // rls
      }

      private void removeRow(int rowIndex) {

         Widget fieldWidget = fTable.getWidget(COL_INFIELD, rowIndex);

         GroupTreeElement srcPNode = groupTreeWidgetMap.get(fieldWidget);
         if (srcPNode != null) {
            groupTreeWidgetMap.remove(fieldWidget);
         }

         paramWidgetTable.remove(fieldWidget);
         fTable.removeRow(rowIndex);
      }
   }
}
