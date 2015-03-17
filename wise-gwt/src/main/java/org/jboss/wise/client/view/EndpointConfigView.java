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
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.wise.client.presenter.EndpointConfigPresenter;
import org.jboss.wise.gui.ParamNode;
import org.jboss.wise.gui.ParameterizedNode;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * User: rsearls
 * Date: 3/9/15
 */
public class EndpointConfigView extends Composite implements EndpointConfigPresenter.Display {


   private final int COL_LABEL = 0;
   private final int COL_ADDBUTTON = 1;
   private final int COL_INFIELD = 2;
   private final int COL_REMOVEBUTTON = 3;

   private final Button invokeButton;
   private final Button previewButton;
   private final Button cancelButton;
   private TextBox debugBox = new TextBox();

   private LinkedHashMap<Widget, ParamNode> paramWidgetTable = new LinkedHashMap<Widget, ParamNode>();
   private VerticalPanel baseVerticalPanel;


   public EndpointConfigView() {
      DecoratorPanel contentDetailsDecorator = new DecoratorPanel();
      contentDetailsDecorator.setWidth("48em");
      initWidget(contentDetailsDecorator);

      baseVerticalPanel = new VerticalPanel();
      baseVerticalPanel.setWidth("100%");

      HorizontalPanel menuPanel = new HorizontalPanel();
      invokeButton = new Button("Invoke");
      cancelButton = new Button("Cancel");
      previewButton = new Button("Preview Message");
      menuPanel.add(cancelButton);
      menuPanel.add(invokeButton);
      menuPanel.add(previewButton);
      baseVerticalPanel.add(menuPanel);

      contentDetailsDecorator.add(baseVerticalPanel);
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

   public Widget asWidget() {

      return this;
   }

   public void setData(ParamNode data) {
      processData(data.getChildren());
   }

   private void processData(List<ParamNode> pNodeList) {
      //- TODO handle case where no input args
      if (pNodeList.isEmpty()) {
         HorizontalPanel hPanel = createNoParamPanel();//getParamPanel(pNode);
         baseVerticalPanel.insert(hPanel, baseVerticalPanel.getWidgetCount() - 1);

      } else {
         for (ParamNode pNode : pNodeList) {

            if (pNode instanceof ParameterizedNode) {
               FlexTable fTable = getParamaterizedPanel((ParameterizedNode) pNode);
               baseVerticalPanel.insert(fTable, baseVerticalPanel.getWidgetCount() - 1);

               debugBox.setText("paramsTable RowCount: " + Integer.toString(fTable.getRowCount())
                  + "  widget cnt: " + Integer.toString(baseVerticalPanel.getWidgetCount()));

            } else {
               HorizontalPanel hPanel = getParamPanel(pNode);
               baseVerticalPanel.insert(hPanel, baseVerticalPanel.getWidgetCount() - 1);
            }
         }
      }
   }

   private FlexTable getParamaterizedPanel(ParameterizedNode pNode) {

      FlexTable fTable = new FlexTable();
      fTable.setCellSpacing(2);
      fTable.setCellPadding(2);
      fTable.setWidth("100%");

      fTable.getColumnFormatter().setWidth(COL_LABEL, "20%");
      fTable.getColumnFormatter().setWidth(COL_ADDBUTTON, "40%");
      fTable.getColumnFormatter().setWidth(COL_INFIELD, "20%");
      fTable.getColumnFormatter().setWidth(COL_REMOVEBUTTON, "20%");

      Widget widget = getWidget(pNode);
      paramWidgetTable.put(widget, pNode);

      fTable.setWidget(0, COL_LABEL, getLabel(pNode));
      fTable.setWidget(0, COL_INFIELD, widget);
      widget.setVisible(false);

      // debug
      if(widget instanceof TextBox) {
         debugBox = (TextBox)widget;
      }

      Button addButton = new Button("add");
      fTable.setWidget(0, COL_ADDBUTTON, addButton);

      debugBox.setText("paramsTable RowCount: " + Integer.toString(fTable.getRowCount())
         + "  widget cnt: " + Integer.toString(baseVerticalPanel.getWidgetCount()));

      addButton.addClickHandler(new AddParamerterizeRowClickHandler(this, paramWidgetTable,fTable));
      return fTable;
   }

   private HorizontalPanel getParamPanel(ParamNode pNode) {
      HorizontalPanel hPanel = new HorizontalPanel();

      hPanel.add(getLabel(pNode));
      Widget w = getWidget(pNode);
      hPanel.add(w);
      paramWidgetTable.put(w, pNode);
      return hPanel;
   }

   private HorizontalPanel createNoParamPanel() {
      HorizontalPanel hPanel = new HorizontalPanel();
      hPanel.add(new Label("Method has no input parameters."));
      return hPanel;
   }

   private Widget getWidget(ParamNode pNode) {
      if ("java.lang.String".endsWith(pNode.getClassType())) {
         return new TextBox();
      } else if ("java.lang.Integer".equals(pNode.getClassType())) {
         return new IntegerBox();
      } else if ("java.lang.Double".equals(pNode.getClassType())) {
         return new DoubleBox();
      }
      return new Label("UNKNOWN TYPE: " + pNode.getClassType());
   }


   private Label getLabel(ParamNode pNode) {
      String typeLabel = "";
      if ( pNode instanceof ParameterizedNode) {
         typeLabel = getBaseType(((ParameterizedNode) pNode).getRawType())
            + "<" + getBaseType(pNode.getClassType()) + ">:" + pNode.getName();
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

   private String getBaseType (String src) {

      int indx = src.lastIndexOf(".");
      String t = src;
      if (indx > -1){
         t = src.substring(indx+1);
      }
      return t;
   }

   /**
    *
    */
   public class AddParamerterizeRowClickHandler implements ClickHandler {
      private EndpointConfigView endpointConfigView;
      private FlexTable fTable;
      private LinkedHashMap<Widget, ParamNode> paramWidgetTable;

      public AddParamerterizeRowClickHandler(EndpointConfigView endpointConfigView,
                                             LinkedHashMap<Widget, ParamNode> paramWidgetTable,
                                          FlexTable fTable) {
         this.endpointConfigView = endpointConfigView;
         this.paramWidgetTable = paramWidgetTable;
         this.fTable = fTable;
      }

      public void onClick(ClickEvent event) {
         insertRowAfter();
      }

      private void insertRowAfter(){
         int lastRow = fTable.getRowCount();
         int next = fTable.insertRow(lastRow);

         debugBox.setText("lastRow: " + Integer.toString(lastRow)
            + "  added next: " + Integer.toString(next)
            + "  RowCount: " + Integer.toString(fTable.getRowCount()));  // rls
         // get ref obj
         Widget srcWidget = fTable.getWidget(0, COL_INFIELD);
         ParamNode pNode = paramWidgetTable.get(srcWidget);

         Widget widget = endpointConfigView.getWidget(pNode);

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
         fTable.setWidget(next, COL_REMOVEBUTTON, rmButton);

         rmButton.addClickHandler(new RemoveParamerterizeRowClickHandler(
            paramWidgetTable,fTable));
      }

   }

   /**
    *
    */
   public class RemoveParamerterizeRowClickHandler implements ClickHandler {
      private FlexTable fTable;
      private LinkedHashMap<Widget, ParamNode> paramWidgetTable;

      public RemoveParamerterizeRowClickHandler(LinkedHashMap<Widget, ParamNode> paramWidgetTable,
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

      private void removeRow(int rowIndex){
         Widget fieldWidget = fTable.getWidget(COL_INFIELD, rowIndex);
         paramWidgetTable.remove(fieldWidget);
         fTable.removeRow(rowIndex);
      }
   }
}
