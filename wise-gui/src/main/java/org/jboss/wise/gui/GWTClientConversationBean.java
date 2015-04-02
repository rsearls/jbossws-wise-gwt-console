package org.jboss.wise.gui;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.wise.core.client.builder.WSDynamicClientBuilder;
import org.jboss.wise.core.client.impl.reflection.builder.ReflectionBasedWSDynamicClientBuilder;
import org.jboss.wise.gui.model.TreeNodeImpl;
import org.jboss.wise.gui.tree.element.EnumerationTreeElement;
import org.jboss.wise.gui.tree.element.GroupTreeElement;
import org.jboss.wise.gui.tree.element.RequestResponse;
import org.jboss.wise.gui.tree.element.SimpleTreeElement;
import org.jboss.wise.gui.tree.element.TreeElement;
import org.jboss.wise.gui.tree.element.TreeElementFactory;
import org.jboss.wise.gui.treeElement.ComplexWiseTreeElement;
import org.jboss.wise.gui.treeElement.EnumerationWiseTreeElement;
import org.jboss.wise.gui.treeElement.GroupWiseTreeElement;
import org.jboss.wise.gui.treeElement.SimpleWiseTreeElement;
import org.jboss.wise.gui.treeElement.WiseTreeElement;

/**
 * User: rsearls
 * Date: 4/2/15
 */
public class GWTClientConversationBean extends ClientConversationBean {

   private Map<String, WiseTreeElement> treeElementMap =
      new HashMap<String, WiseTreeElement>();

   @Override
   public void readWsdl() {

      cleanup();

      /**
       System.out.println("---------- start test ----------");
       WsdlFinder wsdlFinder = new WsdlFinder(); // rls test
       System.out.println("---------- end   test ----------");
       **/
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

      TreeElement treeElement = generateTreeElementHierarchy((TreeNodeImpl)getInputTree());

      RequestResponse invResult = new RequestResponse();
      invResult.setOperationFullName(getCurrentOperationFullName());
      invResult.setTreeElement(treeElement);

      return invResult;
   }

   public String generateRequestPreview(TreeElement rootTreeElement) {
      String str = transferInputValues(rootTreeElement);
      generateRequestPreview();
      return getRequestPreview();
   }

   public RequestResponse performInvocation(TreeElement root) {

      transferInputValues(root);
      performInvocation();

      TreeElement treeE = null;
      TreeNodeImpl outputTree = getOutputTree();
      if (outputTree != null) {
         treeE = generateOutputTreeElementHierarchy(outputTree);
         dumpOutputTree();
      }

      RequestResponse invResult = new RequestResponse();
      invResult.setOperationFullName(getCurrentOperationFullName());
      invResult.setResponseMessage(getResponseMessage());
      invResult.setTreeElement(treeE);
      invResult.setErrorMessage(getError());

      //System.out.println("myPerformInvocationOutputTree responseMessage: " + getResponseMessage());
      //System.out.println("myPerformInvocationOutputTree error: " + getError());

      return invResult;
   }


   private TreeElement generateTreeElementHierarchy(TreeNodeImpl tNode) {

      SimpleTreeElement treeElement = new SimpleTreeElement();
      List<TreeElement> children = treeElement.getChildren();

      Iterator<Object> keyIt = tNode.getChildrenKeysIterator();
      while (keyIt.hasNext()) {
         WiseTreeElement child = (WiseTreeElement) tNode.getChild(keyIt.next());
         TreeElement te = genTreeElement(child);
         children.add(te);
      }

      return treeElement;
   }

   private TreeElement genTreeElement(WiseTreeElement wte) {

      TreeElement treeElement = TreeElementFactory.create(wte);

      if (treeElement instanceof GroupTreeElement) {
         GroupTreeElement gTreeElement = (GroupTreeElement)treeElement;
         WiseTreeElement protoType =  ((GroupWiseTreeElement) wte).getPrototype();

         TreeElement tElement = genTreeElement(protoType);
         gTreeElement.setProtoType(tElement);

         String rType = gTreeElement.getCleanClassName(
            ((ParameterizedType)wte.getClassType()).getRawType().toString());
         gTreeElement.setClassType(rType);

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

         } else if (wte instanceof ComplexWiseTreeElement) {
            ComplexWiseTreeElement cNode = (ComplexWiseTreeElement)wte;
            Iterator<Object> keyIt = cNode.getChildrenKeysIterator();
            while (keyIt.hasNext()) {
               WiseTreeElement child = (WiseTreeElement) cNode.getChild(keyIt.next());
               TreeElement te = genTreeElement(child);
               te.setId(child.getId().toString());
               treeElement.addChild(te);
               //System.out.println("ComplexWiseTreeElement child name: " + child.getName()
               //   + "  te name: " + te.getName() + "  te id: " + te.getId());
            }
         }
         treeElement.setClassType(treeElement.getCleanClassName(wte.getClassType().toString()));
      }

      treeElement.setName(wte.getName());
      treeElement.setKind(wte.getKind());
      treeElement.setId(Integer.toString(((Object)wte).hashCode()));
      treeElementMap.put(treeElement.getId(), wte);

      return treeElement;
   }



   private TreeElement generateOutputTreeElementHierarchy(TreeNodeImpl tNode) {

      SimpleTreeElement treeElement = new SimpleTreeElement();

      if(tNode == null){
         System.out.println("generateOutputTreeElementHierarchy tNode is NULL");

      } else {
         List<TreeElement> children = treeElement.getChildren();

         Iterator<Object> keyIt = tNode.getChildrenKeysIterator();
         while (keyIt.hasNext()) {
            WiseTreeElement child = (WiseTreeElement) tNode.getChild(keyIt.next());
            TreeElement te = genOutputTreeElement(child);
            children.add(te);
         }
      }
      return treeElement;
   }

   private TreeElement genOutputTreeElement(WiseTreeElement wte) {

      TreeElement treeElement = TreeElementFactory.create(wte);

      if (treeElement instanceof GroupTreeElement) {
         GroupTreeElement gTreeElement = (GroupTreeElement)treeElement;
         WiseTreeElement protoType =  ((GroupWiseTreeElement) wte).getPrototype();

         TreeElement pElement = genOutputTreeElement(protoType);
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

         } else if (wte instanceof ComplexWiseTreeElement) {
            ComplexWiseTreeElement cNode = (ComplexWiseTreeElement)wte;
            Iterator<Object> keyIt = cNode.getChildrenKeysIterator();
            while (keyIt.hasNext()) {
               WiseTreeElement child = (WiseTreeElement) cNode.getChild(keyIt.next());
               TreeElement te = genOutputTreeElement(child);
               treeElement.addChild(te);
               //System.out.println("ComplexWiseTreeElement child name: " + child.getName()
               //   + "  te name: " + te.getName());
            }
         }
         treeElement.setClassType(treeElement.getCleanClassName(wte.getClassType().toString()));

      }

      treeElement.setName(wte.getName());
      treeElement.setKind(wte.getKind());
      treeElement.setId(Integer.toString(((Object)wte).hashCode()));
      treeElementMap.put(treeElement.getId(), wte);

      return treeElement;
   }


   private String transferInputValues(TreeElement root) {
      StringBuilder sb = new StringBuilder();
      if (root == null) {
         System.out.println("root is NULL");

      } else {
         /**
         System.out.println("---");
         System.out.println("root: arg name: " + root.getName()
            + "  classType: " + root.getClassType()
            + "  kind: " + root.getKind());

         sb.append("---" + "\n");
         sb.append("root: arg name: " + root.getName()
            + "  classType: " + root.getClassType()
            + "  kind: " + root.getKind() + "\n");
         ***/
         for(TreeElement te : root.getChildren()) {

            WiseTreeElement wte = treeElementMap.get(te.getId());
            if (wte == null) {
               sb.append("wte is NULL\n");

            } else {
               /***
               System.out.println("wte classname: " + wte.getClass().toString());
               sb.append("wte classname: " + wte.getClass().toString() + "\n");
               System.out.println("treeElement kind: " + te.getKind());
               sb.append("treeElement kind: " + te.getKind() + "\n");
               ***/
               if (TreeElement.SIMPLE.equals(te.getKind())) {
                  /***
                  System.out.println("child: arg name: " + te.getName()
                     + "  classType: " + te.getClassType()
                     + "  kind: " + te.getKind());
                  sb.append("child: arg name: " + te.getName()
                     + "  classType: " + te.getClassType()
                     + "  kind: " + te.getKind() + "\n");
                   ***/
                  ((SimpleWiseTreeElement)wte).setValue(((SimpleTreeElement)te).getValue());
                  wte.setNil(false);
                  //wte.setNillable(false);
                  /***
                  System.out.println("transferred value: " + ((SimpleWiseTreeElement)wte).getValue()
                     + " Nil: " + wte.isNil() + "  Nillable: " + wte.isNillable());
                  sb.append("transferred value: " + ((SimpleWiseTreeElement)wte).getValue()
                     + " Nil: " + wte.isNil() + "  Nillable: " + wte.isNillable()
                     + "\n");
                  ***/
               } else if (TreeElement.GROUP.equals(te.getKind())) {
                  /***
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
                     sb.append("value: " + ((SimpleTreeElement)s).getValue() + "\n");
                  }

                  System.out.println("GroupWiseTreeElement Nil: " + wte.isNil()
                     + "  Nillable: " + wte.isNillable());
                  sb.append("GroupWiseTreeElement Nil: " + wte.isNil()
                     + "  Nillable: " + wte.isNillable() + "\n");
                   ***/

                  // A separate key list is required to successfully remove child-key pairs.
                  Iterator<Object> childKeyIt = wte.getChildrenKeysIterator();
                  List<Object> keyList = new ArrayList<Object>();
                  while (childKeyIt.hasNext()) {
                     keyList.add(childKeyIt.next());
                  }

                  for(Object key : keyList) {
                     Object value = wte.getChild(key);
                     //System.out.println("rmChild key: " + key.toString() + "  value: " + value.getClass().getName());
                     //sb.append("rmChild key: " + key.toString() + "  value: " + value.getClass().getName() + "\n");
                     wte.removeChild(key);
                  }

                  for (TreeElement s : ((GroupTreeElement) te).getValueList()) {
                     WiseTreeElement childWte = ((GroupWiseTreeElement) wte).incrementChildren();

                     if (childWte instanceof  SimpleWiseTreeElement){
                        ((SimpleWiseTreeElement)childWte).setValue(((SimpleTreeElement)s).getValue());
                        childWte.setNil(false);
                        //System.out.println("transferred value: " + ((SimpleTreeElement)s).getValue());
                        //sb.append("transferred value: " + ((SimpleTreeElement)s).getValue() + "\n");
                     }
                  }

                  //System.out.println("added: getChildrenKeysIterator size: " + ((GroupWiseTreeElement) wte).getSize());
                  //sb.append("added: getChildrenKeysIterator size: " + ((GroupWiseTreeElement) wte).getSize() + "\n");

               } else if (TreeElement.COMPLEX.equals(te.getKind())) {
                  /***
                  // debugging
                  Iterator<Object> xchildKeyIt = wte.getChildrenKeysIterator();
                  while (xchildKeyIt.hasNext()) {
                     System.out.println("transferInputValues COMPLEX id: " + xchildKeyIt.next().toString());
                  }
                  ***/
                  Iterator<Object> childKeyIt = wte.getChildrenKeysIterator();
                  Map<String, Object> keyMap = new HashMap<String, Object>();
                  while (childKeyIt.hasNext()) {
                     Object obj = childKeyIt.next();
                     keyMap.put(obj.toString(), obj);
                  }

                  // clear previous values
                  childKeyIt = wte.getChildrenKeysIterator();
                  while (childKeyIt.hasNext()) {
                     SimpleWiseTreeElement value = (SimpleWiseTreeElement)wte.getChild(childKeyIt.next());
                     value.setValue(null);
                     value.setNil(true);
                  }

                  for (TreeElement teChild : te.getChildren()) {
                     Object key = keyMap.get(teChild.getId());
                     SimpleWiseTreeElement value = (SimpleWiseTreeElement)wte.getChild(key);
                     value.setValue(((SimpleTreeElement) teChild).getValue());
                     value.setNil(teChild.isNil());

                     //System.out.println("transferInputValues COMPLEX transfered name: " + teChild.getName()
                     //   + "  value: "  +((SimpleTreeElement) teChild).getValue() + "  id: " + teChild.getId());
                  }

                  if (!te.getChildren().isEmpty()) {
                     wte.setNil(false);
                  }

               } else if (TreeElement.ENUMERATION.equals(te.getKind())) {
                  /***
                  System.out.println("Enum child: arg name: " + te.getName()
                     + "  classType: " + te.getClassType()
                     + "  kind: " + te.getKind());
                  sb.append("Enum child: arg name: " + te.getName()
                     + "  classType: " + te.getClassType()
                     + "  kind: " + te.getKind()
                     + "  value: " + ((EnumerationTreeElement)te).getValue() + "\n");

                  for (String v : ((EnumerationTreeElement) te).getEnumValues()) {
                     System.out.println("Enum child: value" + v);
                     sb.append("Enum child: value" + v + "\n");
                  }
                  ***/
                  ((EnumerationWiseTreeElement)wte).setValue(((EnumerationTreeElement)te).getValue());
                  wte.setNil(te.isNil());
                  /**
                  System.out.println("EnumerationWiseTreeElement Nil: " + wte.isNil()
                     + "  Nillable: " + wte.isNillable());
                  sb.append("EnumerationWiseTreeElement Nil: " + wte.isNil()
                     + "  Nillable: " + wte.isNillable() + "\n");
                  **/

               } else {
                  System.out.println("UNKNOW Kind: child: arg name: " + te.getName()
                     + "  classTypeAsString: " + te.getClassType()
                     + "  kind: " + te.getKind());
                  /***
                  sb.append("UNKNOW Kind: child: arg name: " + te.getName()
                     + "  classTypeAsString: " + te.getClassType()
                     + "  kind: " + te.getKind() + "\n");
                  ***/
               }

            }
         }
      }
      return sb.toString();
   }


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
            } else {

               sb.append(child.getType() + " : " + child.getName() + " = "
                  + ((SimpleWiseTreeElement) child).getValue() + "\n");

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
