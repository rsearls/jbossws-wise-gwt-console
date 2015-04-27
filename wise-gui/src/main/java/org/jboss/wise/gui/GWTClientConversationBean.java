package org.jboss.wise.gui;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.wise.core.client.builder.WSDynamicClientBuilder;
import org.jboss.wise.core.client.impl.reflection.builder.ReflectionBasedWSDynamicClientBuilder;
import org.jboss.wise.gui.model.TreeNode;
import org.jboss.wise.gui.model.TreeNodeImpl;
import org.jboss.wise.gui.tree.element.ComplexTreeElement;
import org.jboss.wise.gui.tree.element.EnumerationTreeElement;
import org.jboss.wise.gui.tree.element.GroupTreeElement;
import org.jboss.wise.gui.tree.element.RequestResponse;
import org.jboss.wise.gui.tree.element.SimpleTreeElement;
import org.jboss.wise.gui.tree.element.TreeElement;
import org.jboss.wise.gui.tree.element.TreeElementFactory;
import org.jboss.wise.gui.treeElement.ComplexWiseTreeElement;
import org.jboss.wise.gui.treeElement.EnumerationWiseTreeElement;
import org.jboss.wise.gui.treeElement.GroupWiseTreeElement;
import org.jboss.wise.gui.treeElement.LazyLoadWiseTreeElement;
import org.jboss.wise.gui.treeElement.SimpleWiseTreeElement;
import org.jboss.wise.gui.treeElement.WiseTreeElement;

/**
 * User: rsearls
 * Date: 4/2/15
 */
public class GWTClientConversationBean extends ClientConversationBean {

   private Map<String, WiseTreeElement> treeElementMap =
      new HashMap<String, WiseTreeElement>();

   private HashMap<String, WiseTreeElement> lazyLoadMap = new HashMap<String, WiseTreeElement>();


   @Override
   public void readWsdl() {

      cleanup();

      try {
         System.out.println("--## wsdlUrl: " + getWsdlUrl());
         WSDynamicClientBuilder builder = new ReflectionBasedWSDynamicClientBuilder()
            .verbose(true).messageStream(ps).keepSource(true).excludeNonSOAPPorts(true)
            .maxThreadPoolSize(1);

         String wsdlUser = getWsdlUser();
         String wsdlPwd = getWsdlPwd();

         builder.userName(wsdlUser);
         setInvocationUser(wsdlUser);
         builder.password(wsdlPwd);
         setInvocationPwd(wsdlPwd);
         client = builder.wsdlURL(getWsdlUrl()).build();

      } catch (Exception e) {
         setError("Could not read WSDL from specified URL. Please check credentials and see logs for further information.");
         logException(e);
      }
      if (client != null) {
         try {
            List<Service> services = ClientHelper.convertServicesToGui(client.processServices());
            String currentOperation = ClientHelper.getFirstGuiOperation(services);

            setServices(services);
            setCurrentOperation(currentOperation);

         } catch (Exception e) {
            setError("Could not parse WSDL from specified URL. Please check logs for further information.");
            logException(e);
         }
      }
   }

   public RequestResponse parseOperationParameters(String curOperation) {

      try {
         List<Service> services = getServices();
         String currentOperation = getCurrentOperation();

         String currentOperationFullName = ClientHelper.getOperationFullName(currentOperation, services);
         TreeNodeImpl inputTree = ClientHelper.convertOperationParametersToGui(ClientHelper.getWSMethod(curOperation, client), client);

         setInputTree(inputTree);
         setCurrentOperationFullName(currentOperationFullName);

      } catch (Exception e) {
         e.printStackTrace();
      }

      TreeElement treeElement = wiseDataPostProcess((TreeNodeImpl)getInputTree());

      RequestResponse invResult = new RequestResponse();
      invResult.setOperationFullName(getCurrentOperationFullName());
      invResult.setTreeElement(treeElement);

      return invResult;
   }

   public String generateRequestPreview(TreeElement rootTreeElement) {
      userDataPostProcess(rootTreeElement);
      generateRequestPreview();
      return getRequestPreview();
   }

   public RequestResponse performInvocation(TreeElement root) {
      userDataPostProcess(root);
      performInvocation();

      TreeElement treeE = null;
      TreeNodeImpl outputTree = getOutputTree();
      if (outputTree != null) {
         treeE = wiseOutputPostProcess(outputTree);
         //dumpOutputTree();
      }

      RequestResponse invResult = new RequestResponse();
      invResult.setOperationFullName(getCurrentOperationFullName());
      invResult.setResponseMessage(getResponseMessage());
      invResult.setTreeElement(treeE);
      invResult.setErrorMessage(getError());

      return invResult;
   }

   /**
    * Generate GWT objects that correspond to WISE objects
    * @param tNode
    * @return
    */
   private TreeElement wiseDataPostProcess(TreeNodeImpl tNode) {

      lazyLoadMap.clear();

      SimpleTreeElement treeElement = new SimpleTreeElement();
      List<TreeElement> children = treeElement.getChildren();

      Iterator<Object> keyIt = tNode.getChildrenKeysIterator();
      while (keyIt.hasNext()) {
         WiseTreeElement child = (WiseTreeElement) tNode.getChild(keyIt.next());
         TreeElement te = wiseDataTransfer(child);
         children.add(te);
      }

      return treeElement;
   }

   private TreeElement wiseDataTransfer(WiseTreeElement wte) {

      TreeElement treeElement = TreeElementFactory.create(wte);

      if (treeElement instanceof GroupTreeElement) {
         GroupTreeElement gTreeElement = (GroupTreeElement)treeElement;
         WiseTreeElement protoType =  ((GroupWiseTreeElement) wte).getPrototype();

         TreeElement tElement = wiseDataTransfer(protoType);
         gTreeElement.setProtoType(tElement);

         String rType = gTreeElement.getCleanClassName(
            ((ParameterizedType)wte.getClassType()).getRawType().toString());
         gTreeElement.setClassType(rType);


      } else if (wte instanceof ComplexWiseTreeElement) {
         ComplexWiseTreeElement cNode = (ComplexWiseTreeElement)wte;
         Iterator<Object> keyIt = cNode.getChildrenKeysIterator();
         while (keyIt.hasNext()) {
            WiseTreeElement child = (WiseTreeElement) cNode.getChild(keyIt.next());
            TreeElement te = wiseDataTransfer(child);
            te.setId(child.getId().toString());
            treeElement.addChild(te);

         }
         treeElement.setClassType(treeElement.getCleanClassName(wte.getClassType().toString()));
         lazyLoadMap.put(cNode.getClassType().toString(), cNode);

      } else if (treeElement instanceof EnumerationTreeElement) {
         EnumerationTreeElement eTreeElement = (EnumerationTreeElement)treeElement;
         Map<String, String> eValuesMap = ((EnumerationWiseTreeElement)wte).getValidValue();
         if (eValuesMap != null) {
            eTreeElement.getEnumValues().addAll(eValuesMap.keySet());
         }
         eTreeElement.setValue(((SimpleWiseTreeElement) wte).getValue());
         eTreeElement.setClassType(treeElement.getCleanClassName(wte.getClassType().toString()));

      } else {
         if (wte instanceof SimpleWiseTreeElement) {
            ((SimpleTreeElement) treeElement).setValue(((SimpleWiseTreeElement) wte).getValue());

         }
         treeElement.setClassType(treeElement.getCleanClassName(wte.getClassType().toString()));
      }

      treeElement.setName(wte.getName());
      treeElement.setKind(wte.getKind());
      treeElement.setId(Integer.toString(((Object)wte).hashCode()));
      treeElementMap.put(treeElement.getId(), wte);

      return treeElement;
   }


   /**
    * Generate GWT objects from WISE response objects
    * @param tNode
    * @return
    */
   private TreeElement wiseOutputPostProcess(TreeNodeImpl tNode) {

      SimpleTreeElement treeElement = new SimpleTreeElement();

      if(tNode == null){
         System.out.println("wiseOutputPostProcess tNode is NULL");

      } else {
         List<TreeElement> children = treeElement.getChildren();

         Iterator<Object> keyIt = tNode.getChildrenKeysIterator();
         while (keyIt.hasNext()) {
            WiseTreeElement child = (WiseTreeElement) tNode.getChild(keyIt.next());
            TreeElement te = wiseOutputTransfer(child);
            children.add(te);
         }
      }
      return treeElement;
   }

   private TreeElement wiseOutputTransfer(WiseTreeElement wte) {

      TreeElement treeElement = TreeElementFactory.create(wte);

      if (treeElement instanceof GroupTreeElement) {
         GroupTreeElement gTreeElement = (GroupTreeElement)treeElement;
         WiseTreeElement protoType =  ((GroupWiseTreeElement) wte).getPrototype();

         TreeElement pElement = wiseOutputTransfer(protoType);
         gTreeElement.setProtoType(pElement);

         String rType = gTreeElement.getCleanClassName(
            ((ParameterizedType)wte.getClassType()).getRawType().toString());
         gTreeElement.setClassType(rType);


         GroupWiseTreeElement gChild = (GroupWiseTreeElement)wte;
         Iterator<Object> childKeyIt = gChild.getChildrenKeysIterator();
         while (childKeyIt.hasNext()) {
            Object c = gChild.getChild(childKeyIt.next());
            if (c instanceof SimpleWiseTreeElement) {
               SimpleWiseTreeElement simpleChild = (SimpleWiseTreeElement)c;
               SimpleTreeElement ste = new SimpleTreeElement();
               ste.setValue(simpleChild.getValue());
               gTreeElement.addValue(ste);
            }
         }

      } else if (wte instanceof ComplexWiseTreeElement) {
         ComplexWiseTreeElement cNode = (ComplexWiseTreeElement) wte;
         Iterator<Object> keyIt = cNode.getChildrenKeysIterator();
         while (keyIt.hasNext()) {
            WiseTreeElement child = (WiseTreeElement) cNode.getChild(keyIt.next());
            TreeElement te = wiseOutputTransfer(child);
            treeElement.addChild(te);
         }

      } else if (treeElement instanceof EnumerationTreeElement) {
         EnumerationTreeElement eTreeElement = (EnumerationTreeElement)treeElement;
         Map<String, String> eValuesMap = ((EnumerationWiseTreeElement)wte).getValidValue();
         if (eValuesMap != null) {
            eTreeElement.getEnumValues().addAll(eValuesMap.keySet());
         }
         eTreeElement.setValue(((SimpleWiseTreeElement) wte).getValue());
         eTreeElement.setClassType(treeElement.getCleanClassName(wte.getClassType().toString()));

      } else {
         if (wte instanceof SimpleWiseTreeElement) {
            ((SimpleTreeElement) treeElement).setValue(((SimpleWiseTreeElement) wte).getValue());

         }
         treeElement.setClassType(treeElement.getCleanClassName(wte.getClassType().toString()));

      }

      treeElement.setName(wte.getName());
      treeElement.setKind(wte.getKind());
      treeElement.setId(Integer.toString(((Object)wte).hashCode()));
      treeElementMap.put(treeElement.getId(), wte);

      return treeElement;
   }

   /**
    * Transfer user input data from GWT objects to WISE objects
    * @param root
    */
   private void userDataPostProcess (TreeElement root) {
      if (root != null) {
         for(TreeElement te : root.getChildren()) {
            WiseTreeElement wte = treeElementMap.get(te.getId());
            if (wte == null) {
               // This should never happen
               System.out.println("ERROR: not WiseTreeElement for TreeElement");

            } else {
               userDataTransfer(te, wte);
            }
         }
      }
   }


   private void userDataTransfer(TreeElement treeElement, WiseTreeElement wte) {

      if (TreeElement.SIMPLE.equals(treeElement.getKind())) {

         if (wte instanceof SimpleWiseTreeElement) {
            ((SimpleWiseTreeElement) wte).setValue(((SimpleTreeElement) treeElement).getValue());
            wte.setNil(false);

         } else {
            System.out.println("ERROR: incompatible types. TreeElement: " + treeElement.getKind()
               + "  WiseTreeElement: " + wte.getClass().getName());
         }

      } else  if (treeElement instanceof ComplexTreeElement) {

         if (wte instanceof ComplexWiseTreeElement) {
            ComplexTreeElement cte = (ComplexTreeElement) treeElement;
            ComplexWiseTreeElement cWise = (ComplexWiseTreeElement) wte;

            Iterator<Object> childKeyIt = cWise.getChildrenKeysIterator();
            // create structure for node lookup by variable name
            HashMap<String, TreeNode> wiseChildren = new HashMap<String, TreeNode>();
            while (childKeyIt.hasNext()) {
               TreeNode tNode = cWise.getChild(childKeyIt.next());
               wiseChildren.put(((WiseTreeElement)tNode).getName(), tNode);
            }

            int cnt = cte.getChildren().size();
            if (cnt == wiseChildren.size()) {
               for (int i = 0; i < cnt; i++) {
                  TreeNode tNode = wiseChildren.get(cte.getChildren().get(i).getName());

                  if (tNode != null) {
                     userDataTransfer(cte.getChildren().get(i), (WiseTreeElement) tNode);
                  } else {
                     System.out.println("ERROR: No Wise treeNode found for name: " + cte.getChildren().get(i).getName());
                  }
               }

            } else {
               System.out.println("ERROR: incompatable child count: ComplexTreeElement cnt: "
                  + cte.getChildren().size() + "  ComplexWiseTreeElement cnt: " + wiseChildren.size());
            }

         } else {
            System.out.println("ERROR: incompatible types. TreeElement: " + treeElement.getKind()
               + "  WiseTreeElement: " + wte.getClass().getName());
         }

      } else if (treeElement instanceof GroupTreeElement) {

         if (wte instanceof GroupWiseTreeElement) {
            GroupTreeElement cte = (GroupTreeElement)treeElement;
            GroupWiseTreeElement cWise = (GroupWiseTreeElement)wte;

            // replace deferred class with actual class.
            if (cWise.getPrototype() instanceof LazyLoadWiseTreeElement) {
               WiseTreeElement protoChildWte = lazyLoadMap.get(cWise.getPrototype().getClassType().toString());
               WiseTreeElement clone = protoChildWte.clone();
               cWise.setPrototype(clone);
               String test="";  // debug
            }

            for(TreeElement child : cte.getValueList()) {
               WiseTreeElement protoChildWte = cWise.incrementChildren();
               userDataTransfer(child, protoChildWte);
            }

         } else {
            System.out.println("ERROR: incompatible types. TreeElement: " + treeElement.getKind()
               + "  WiseTreeElement: " + wte.getClass().getName());
         }

      } else if (treeElement instanceof EnumerationTreeElement) {
         if (wte instanceof EnumerationWiseTreeElement) {

            ((EnumerationWiseTreeElement) wte).setValue(((EnumerationTreeElement) treeElement).getValue());
            wte.setNil(treeElement.isNil());

         } else {
            System.out.println("ERROR: incompatible types. TreeElement: " + treeElement.getKind()
               + "  WiseTreeElement: " + wte.getClass().getName());
         }

      }
   }


   /**
    * Debug only
    * @param root
    * @return
    */
   private String dumpTree(TreeElement root) {
      StringBuilder sb = new StringBuilder();
      if (root == null) {
         System.out.println("root is NULL");

      } else {
         System.out.println("---");
         System.out.println("root: arg name: " + root.getName()
            + "  classType: " + root.getClassType()
            + "  kind: " + root.getKind());

         sb.append("---" + "\n");
         sb.append("root: arg name: " + root.getName()
            + "  classType: " + root.getClassType()
            + "  kind: " + root.getKind() + "\n");

         for(TreeElement te : root.getChildren()) {

            if (TreeElement.SIMPLE.equals(te.getKind())) {
               System.out.println("child: arg name: " + te.getName()
                  + "  classType: " + te.getClassType()
                  + "  kind: " + te.getKind());
               sb.append("child: arg name: " + te.getName()
                  + "  classType: " + te.getClassType()
                  + "  kind: " + te.getKind() + "\n");


            } else if (TreeElement.GROUP.equals(te.getKind())) {
               System.out.println("child: arg name: " + te.getName()
                  + "  classType: " + te.getClassType()
                  + "  rawType: " + ((GroupTreeElement) te).getProtoType().getClassType()
                  + "  kind: " + te.getKind());
               sb.append("child: arg name: " + te.getName()
                  + "  classType: " + te.getClassType()
                  + "  rawType: " + ((GroupTreeElement) te).getProtoType().getClassType()
                  + "  kind: " + te.getKind() + "\n");

               for (TreeElement s : ((GroupTreeElement) te).getValueList()) {
                  System.out.println("value: " + ((SimpleTreeElement)s).getValue());
                  sb.append("value: " + s + "\n");
               }


            } else if (TreeElement.ENUMERATION.equals(te.getKind())) {
               System.out.println("Enum child: arg name: " + te.getName()
                  + "  classType: " + te.getClassType()
                  + "  kind: " + te.getKind());
               sb.append("Enum child: arg name: " + te.getName()
                  + "  classType: " + te.getClassType()
                  + "  kind: " + te.getKind()
                  + "  value: " + ((SimpleTreeElement)te).getValue() + "\n");

               for (String v : ((EnumerationTreeElement) te).getEnumValues()) {
                  System.out.println("Enum child: value" + v);
                  sb.append("Enum child: value" + v + "\n");
               }


            } else {
               System.out.println("UNKNOW Kind: child: arg name: " + te.getName()
                  + "  classTypeAsString: " + te.getClassType()
                  + "  kind: " + te.getKind());
               sb.append("UNKNOW Kind: child: arg name: " + te.getName()
                  + "  classTypeAsString: " + te.getClassType()
                  + "  kind: " + te.getKind() + "\n");

            }
         }
      }
      return sb.toString();
   }

   /**
    * Debug only
    * @return
    */
   private String dumpOutputTree() {
      StringBuilder sb = new StringBuilder();
      TreeNodeImpl outputTree = getOutputTree();

      if (outputTree != null) {

         Iterator<Object> keyIt = outputTree.getChildrenKeysIterator();
         while (keyIt.hasNext()) {
            WiseTreeElement child = (WiseTreeElement) outputTree.getChild(keyIt.next());
            sb.append(getCurrentOperationFullName() + "\n");

            if (WiseTreeElement.GROUP.equals(child.getKind())) {
               GroupWiseTreeElement gChild = (GroupWiseTreeElement)child;
               sb.append(child.getType() + "[" + gChild.getSize() + "]"  + "\n");

               Iterator<Object> childKeyIt = gChild.getChildrenKeysIterator();
               while (childKeyIt.hasNext()) {
                  Object c = gChild.getChild(childKeyIt.next());
                  if (c instanceof SimpleWiseTreeElement) {
                     SimpleWiseTreeElement simpleChild = (SimpleWiseTreeElement)c;
                     System.out.println("outputTree Group child name: " + simpleChild.getName()
                        + "  value: " + simpleChild.getValue());

                     sb.append(simpleChild.getType() + " : " + simpleChild.getName() + " = "
                        + simpleChild.getValue() + "\n");
                  }
               }
            } else if (child instanceof ComplexWiseTreeElement) {
               System.out.println("dumpOutputTree: ComplexWiseTreeElement: not implemented");
            } else if (child instanceof EnumerationWiseTreeElement) {
               System.out.println("dumpOutputTree: EnumerationWiseTreeElement: not implemented");
            } else if (child instanceof SimpleWiseTreeElement) {

               sb.append(child.getType() + " : " + child.getName() + " = "
                  + ((SimpleWiseTreeElement) child).getValue() + "\n");

            } else {

            }
         }
      }

      return sb.toString();
   }


   @Override
   protected void cleanup() {
      super.cleanup();
      treeElementMap.clear();
   }
}
