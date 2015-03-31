/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the 
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.wise.gui;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.jboss.logging.Logger;
import org.jboss.wise.core.client.InvocationResult;
import org.jboss.wise.core.client.WSDynamicClient;
import org.jboss.wise.core.client.WSEndpoint;
import org.jboss.wise.core.client.WSMethod;
import org.jboss.wise.core.client.builder.WSDynamicClientBuilder;
import org.jboss.wise.core.client.impl.reflection.builder.ReflectionBasedWSDynamicClientBuilder;
import org.jboss.wise.core.exception.InvocationException;
import org.jboss.wise.core.utils.JBossLoggingOutputStream;
import org.jboss.wise.gui.model.TreeNodeImpl;
//import org.jboss.wise.gui.tree.element.CollectionTreeElement;
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
import org.richfaces.component.UITree;
import org.richfaces.event.ItemChangeEvent;

//import sun.util.logging.resources.logging.Logger;

//@Named
//@ConversationScoped
public class ClientConversationBean implements Serializable {

   private static final long serialVersionUID = -3778997821476776895L;

   private static final int CONVERSATION_TIMEOUT = 15 * 60 * 1000; //15 mins instead of default 30 mins
   private static CleanupTask<WSDynamicClient> cleanupTask = new CleanupTask<WSDynamicClient>(true);
   private static Logger logger = Logger.getLogger(ClientConversationBean.class);
   private static PrintStream ps = new PrintStream(new JBossLoggingOutputStream(logger, Logger.Level.DEBUG), true);

   //@Inject
   //Conversation conversation;
   private WSDynamicClient client;
   private String wsdlUrl;
   private String wsdlUser;
   private String wsdlPwd;
   private String invocationUrl;
   private String invocationUser;
   private String invocationPwd;
   private List<Service> services;
   private String currentOperation;
   private String currentOperationFullName;
   private TreeNodeImpl inputTree;
   private TreeNodeImpl outputTree;
   private String error;
   private UITree inTree;
   private String requestPreview;
   private String responseMessage;
   private String requestActiveTab;
   private Map<String, WiseTreeElement> treeElementMap =
      new HashMap<String, WiseTreeElement>();

   //@PostConstruct
   public void init() {
      //this is called each time a new browser tab is used and whenever the conversation expires (hence a new bean is created)
      //conversation.begin();
      //conversation.setTimeout(CONVERSATION_TIMEOUT);
   }

   public void readWsdl() {

      cleanup();
      //restart conversation
      //conversation.end();
      //conversation.begin();
      /**
      System.out.println("---------- start test ----------");
      WsdlFinder wsdlFinder = new WsdlFinder(); // rls test
      System.out.println("---------- end   test ----------");
      **/
      try {
         System.out.println("## wsdlUrl: " + getWsdlUrl());
         WSDynamicClientBuilder builder = new ReflectionBasedWSDynamicClientBuilder().verbose(true).messageStream(ps)
            .keepSource(true).excludeNonSOAPPorts(true).maxThreadPoolSize(1);
         builder.userName(wsdlUser);
         invocationUser = wsdlUser;
         builder.password(wsdlPwd);
         invocationPwd = wsdlPwd;
         client = builder.wsdlURL(getWsdlUrl()).build();
         cleanupTask.addRef(client, System.currentTimeMillis() + CONVERSATION_TIMEOUT,
            new CleanupTask.CleanupCallback<WSDynamicClient>() {
               @Override
               public void cleanup(WSDynamicClient data) {

                  data.close();
               }
            });
      } catch (Exception e) {
         error = "Could not read WSDL from specified URL. Please check credentials and see logs for further information.";
         logException(e);
      }
      if (client != null) {
         try {
            services = ClientHelper.convertServicesToGui(client.processServices());
            currentOperation = ClientHelper.getFirstGuiOperation(services);
         } catch (Exception e) {
            error = "Could not parse WSDL from specified URL. Please check logs for further information.";
            logException(e);
         }
      }
   }
   /***********
   //- rls start
   public void debugOprParams() {

      List<String> rlsOperList = ClientHelper.getOperationList(services);
      for (String cOper : rlsOperList) {
         try {
            ClientHelper.convertOperationParametersToGui(ClientHelper.getWSMethod(cOper, client), client);
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }
  **********/
   public RequestResponse myParseOperationParameters(String curOperation) {

      try {
         currentOperationFullName = ClientHelper.getOperationFullName(currentOperation, services);
         inputTree = ClientHelper.convertOperationParametersToGui(ClientHelper.getWSMethod(curOperation, client), client);
      } catch (Exception e) {
         e.printStackTrace();
      }

      TreeElement treeElement = generateTreeElementHierarchy((TreeNodeImpl)inputTree);

      RequestResponse invResult = new RequestResponse();
      invResult.setOperationFullName(currentOperationFullName);
      invResult.setTreeElement(treeElement);

      return invResult;
   }

   //------ rls start
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
            //treeElement.setClassType(treeElement.getCleanClassName(wte.getClassType().toString()));

         } else if (wte instanceof ComplexWiseTreeElement) {
            ComplexWiseTreeElement cNode = (ComplexWiseTreeElement)wte;
            Iterator<Object> keyIt = cNode.getChildrenKeysIterator();
            while (keyIt.hasNext()) {
               WiseTreeElement child = (WiseTreeElement) cNode.getChild(keyIt.next());
               TreeElement te = genTreeElement(child);
               te.setId(child.getId().toString());  // rls test id.
               treeElement.addChild(te);
               System.out.println("ComplexWiseTreeElement child name: " + child.getName()
                  + "  te name: " + te.getName() + "  te id: " + te.getId());
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
   //------ rls end

   public void parseOperationParameters() {

      outputTree = null;
      responseMessage = null;
      error = null;
      try {
         currentOperationFullName = ClientHelper.getOperationFullName(currentOperation, services);
         inputTree = ClientHelper.convertOperationParametersToGui(ClientHelper.getWSMethod(currentOperation, client), client);
      } catch (Exception e) {
         error = ClientHelper.toErrorMessage(e);
         logException(e);
      }
   }

   //-------- rls start
   public RequestResponse myPerformInvocationOutputTree(TreeElement root) {

      transferInputValues(root);
      performInvocation();

      TreeElement treeE = null;
      if (outputTree != null) {
         treeE = generateOutputTreeElementHierarchy(outputTree);
         dumpOutputTree();
      }

      RequestResponse invResult = new RequestResponse();
      invResult.setOperationFullName(getCurrentOperationFullName());
      invResult.setResponseMessage(responseMessage);
      invResult.setTreeElement(treeE);
      invResult.setErrorMessage(error);

      System.out.println("myPerformInvocationOutputTree responseMessage: " + responseMessage);
      System.out.println("myPerformInvocationOutputTree error: " + error);

      return invResult;
   }


   //------ rls start
   private TreeElement generateOutputTreeElementHierarchy(TreeNodeImpl tNode) {

      if(tNode == null){
         System.out.println("generateOutputTreeElementHierarchy tNode is NULL");
      }

      SimpleTreeElement treeElement = new SimpleTreeElement();
      List<TreeElement> children = treeElement.getChildren();

      Iterator<Object> keyIt = tNode.getChildrenKeysIterator();
      while (keyIt.hasNext()) {
         WiseTreeElement child = (WiseTreeElement) tNode.getChild(keyIt.next());
         TreeElement te = genOutputTreeElement(child);
         children.add(te);
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

          //------ isOutputMessageProcessing  start
         GroupWiseTreeElement gChild = (GroupWiseTreeElement)wte;
         Iterator<Object> childKeyIt = gChild.getChildrenKeysIterator();
         while (childKeyIt.hasNext()) {
            Object c = gChild.getChild(childKeyIt.next());
            if (c instanceof SimpleWiseTreeElement) {
               SimpleWiseTreeElement simpleChild = (SimpleWiseTreeElement)c;
               SimpleTreeElement ste = new SimpleTreeElement();
               ste.setValue(simpleChild.getValue());
               gTreeElement.addValue(ste);
               /***
               System.out.println("outputTree Group child name: " + simpleChild.getName()
                  + "  value: " + simpleChild.getValue());

               sb.append(simpleChild.getType() + " : " + simpleChild.getName() + " = "
                  + simpleChild.getValue() + "\n");
               ****/
            }
         }
         //------ isOutputMessageProcessing  start
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
            //treeElement.setClassType(treeElement.getCleanClassName(wte.getClassType().toString()));

         } else if (wte instanceof ComplexWiseTreeElement) {
            ComplexWiseTreeElement cNode = (ComplexWiseTreeElement)wte;
            Iterator<Object> keyIt = cNode.getChildrenKeysIterator();
            while (keyIt.hasNext()) {
               WiseTreeElement child = (WiseTreeElement) cNode.getChild(keyIt.next());
               TreeElement te = genOutputTreeElement(child);
               treeElement.addChild(te);
               System.out.println("ComplexWiseTreeElement child name: " + child.getName()
                  + "  te name: " + te.getName());
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
   //------ rls end

   private String dumpOutputTree() {
      StringBuilder sb = new StringBuilder();

      if (outputTree != null) {

         Iterator<Object> keyIt = outputTree.getChildrenKeysIterator();
         while (keyIt.hasNext()) {
            WiseTreeElement child = (WiseTreeElement) outputTree.getChild(keyIt.next());
            sb.append(getCurrentOperationFullName() + "\n");

            if (WiseTreeElement.GROUP.equals(child.getKind())) {
               GroupWiseTreeElement gChild = (GroupWiseTreeElement)child;
               sb.append(child.getType() + "[" + gChild.getSize() + "]"  + "\n");
               // #{node.type}[#{node.size}]

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
               // child.getType() : child.getName() = ((SimpleWiseTreeElement)child).getValue()
            }
         }
      }

      return sb.toString();
   }

   //-------- rls end

   public void performInvocation() {

      outputTree = null;
      error = null;
      responseMessage = null;
      try {
         WSMethod wsMethod = ClientHelper.getWSMethod(currentOperation, client);
         InvocationResult result = null;
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         try {
            Map<String, Object> params = ClientHelper.processGUIParameters(inputTree);
            ClientHelper.addOUTParameters(params, wsMethod, client);
            final WSEndpoint endpoint = wsMethod.getEndpoint();

            System.out.println("invocation  url: " + invocationUrl + "  user: "
               + invocationUser + "  password: " + invocationPwd);

            endpoint.setTargetUrl(invocationUrl);
            endpoint.setPassword(invocationPwd);
            endpoint.setUsername(invocationUser);
            endpoint.addHandler(new ResponseLogHandler(os));
            result = wsMethod.invoke(params);
         } catch (InvocationException e) {
            logException(e);
            error = "Unexpected fault / error received from target endpoint";
         } finally {
            responseMessage = os.toString("UTF-8");
            if (responseMessage.trim().length() == 0) {
               responseMessage = null;
            }
         }
         if (result != null) {
            outputTree = ClientHelper.convertOperationResultToGui(result, client);
            error = null;
         }
      } catch (Exception e) {
         error = ClientHelper.toErrorMessage(e);
         logException(e);
      }
   }

   public void generateRequestPreview() {

      requestPreview = null;
      try {
         WSMethod wsMethod = ClientHelper.getWSMethod(currentOperation, client);
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         wsMethod.getEndpoint().setTargetUrl(null);
         wsMethod.writeRequestPreview(ClientHelper.processGUIParameters(inputTree), os);
         requestPreview = os.toString("UTF-8");
      } catch (Exception e) {
         requestPreview = ClientHelper.toErrorMessage(e);
         logException(e);
      }
   }

   //---------- rls start
   public String myGenerateRequestPreview(TreeElement rootTreeElement) {
      String str = transferInputValues(rootTreeElement);
      generateRequestPreview();
      /***
      return "currentOperation: " + currentOperation
         + "---------\n" + str + "---------\n" + requestPreview;
      ***/
      return requestPreview;
      //return dumpTree(rootTreeElement) + "###" + str;
   }


   private String transferInputValues(TreeElement root) {
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

            WiseTreeElement wte = treeElementMap.get(te.getId());
            if (wte == null) {
               sb.append("wte is NULL\n");

            } else {
               System.out.println("wte classname: " + wte.getClass().toString());
               sb.append("wte classname: " + wte.getClass().toString() + "\n");
               System.out.println("treeElement kind: " + te.getKind());
               sb.append("treeElement kind: " + te.getKind() + "\n");

               if (TreeElement.SIMPLE.equals(te.getKind())) {
                  System.out.println("child: arg name: " + te.getName()
                     + "  classType: " + te.getClassType()
                     + "  kind: " + te.getKind());
                  sb.append("child: arg name: " + te.getName()
                     + "  classType: " + te.getClassType()
                     + "  kind: " + te.getKind() + "\n");

                  ((SimpleWiseTreeElement)wte).setValue(((SimpleTreeElement)te).getValue());
                  wte.setNil(false);
                  //wte.setNillable(false);
                  System.out.println("transferred value: " + ((SimpleWiseTreeElement)wte).getValue()
                     + " Nil: " + wte.isNil() + "  Nillable: " + wte.isNillable());
                  sb.append("transferred value: " + ((SimpleWiseTreeElement)wte).getValue()
                     + " Nil: " + wte.isNil() + "  Nillable: " + wte.isNillable()
                     + "\n");

               } else if (TreeElement.GROUP.equals(te.getKind())) {
                  System.out.println("child: arg name: " + te.getName()
                     + "  classType: " + te.getClassType()
                     + "  rawType: " + ((GroupTreeElement) te).getProtoType().getClassType()
                     + "  kind: " + te.getKind());
                  sb.append("child: arg name: " + te.getName()
                     + "  classType: " + te.getClassType()
                     + "  rawType: " + ((GroupTreeElement) te).getProtoType().getClassType()
                     + "  kind: " + te.getKind() + "\n");

                  // for (String s : ((GroupTreeElement) te).getProtoType().getValueList()) {
                  for (TreeElement s : ((GroupTreeElement) te).getValueList()) {
                     System.out.println("value: " + ((SimpleTreeElement)s).getValue());
                     sb.append("value: " + ((SimpleTreeElement)s).getValue() + "\n");
                  }

                  System.out.println("GroupWiseTreeElement Nil: " + wte.isNil()
                     + "  Nillable: " + wte.isNillable());
                  sb.append("GroupWiseTreeElement Nil: " + wte.isNil()
                     + "  Nillable: " + wte.isNillable() + "\n");


                  //System.out.println("before: getChildrenKeysIterator size: " + ((GroupWiseTreeElement) wte).getSize());
                  //sb.append("before: getChildrenKeysIterator size: " + ((GroupWiseTreeElement) wte).getSize() + "\n");

                  // A separate key list is required to successfully remove child-key pairs.
                  Iterator<Object> childKeyIt = wte.getChildrenKeysIterator();
                  List<Object> keyList = new ArrayList<Object>();
                  while (childKeyIt.hasNext()) {
                     keyList.add(childKeyIt.next());
                  }

                  for(Object key : keyList) {
                     Object value = wte.getChild(key);
                     System.out.println("rmChild key: " + key.toString() + "  value: " + value.getClass().getName());
                     sb.append("rmChild key: " + key.toString() + "  value: " + value.getClass().getName() + "\n");
                     wte.removeChild(key);
                  }

                 // System.out.println("after: getChildrenKeysIterator size: " + ((GroupWiseTreeElement) wte).getSize());
                 // sb.append("after: getChildrenKeysIterator size: " + ((GroupWiseTreeElement) wte).getSize() + "\n");

                  for (TreeElement s : ((GroupTreeElement) te).getValueList()) {
                     WiseTreeElement childWte = ((GroupWiseTreeElement) wte).incrementChildren();

                     if (childWte instanceof  SimpleWiseTreeElement){
                        ((SimpleWiseTreeElement)childWte).setValue(((SimpleTreeElement)s).getValue());
                        childWte.setNil(false);
                        System.out.println("transferred value: " + ((SimpleTreeElement)s).getValue());
                        sb.append("transferred value: " + ((SimpleTreeElement)s).getValue() + "\n");
                     }
                  }

                  System.out.println("added: getChildrenKeysIterator size: " + ((GroupWiseTreeElement) wte).getSize());
                  sb.append("added: getChildrenKeysIterator size: " + ((GroupWiseTreeElement) wte).getSize() + "\n");

               } else if (TreeElement.COMPLEX.equals(te.getKind())) {

                  // debugging
                  Iterator<Object> childKeyIt = wte.getChildrenKeysIterator();
                  while (childKeyIt.hasNext()) {
                     System.out.println("transferInputValues COMPLEX id: " + childKeyIt.next().toString());
                  }

                  /*Iterator<Object>*/ childKeyIt = wte.getChildrenKeysIterator();
                  Map<String, Object> keyMap = new HashMap<String, Object>();
                  while (childKeyIt.hasNext()) {
                     Object obj = childKeyIt.next();
                     keyMap.put(obj.toString(), obj);
                  }

                  //------
                  // clear previous values
                  /*Iterator<Object>*/ childKeyIt = wte.getChildrenKeysIterator();
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

                     System.out.println("transferInputValues COMPLEX transfered name: " + teChild.getName()
                        + "  value: "  +((SimpleTreeElement) teChild).getValue() + "  id: " + teChild.getId());
                  }

                  if (!te.getChildren().isEmpty()) {
                     wte.setNil(false);
                  }

               } else if (TreeElement.ENUMERATION.equals(te.getKind())) {
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

                  ((EnumerationWiseTreeElement)wte).setValue(((EnumerationTreeElement)te).getValue());
                  wte.setNil(te.isNil());

                  System.out.println("EnumerationWiseTreeElement Nil: " + wte.isNil()
                     + "  Nillable: " + wte.isNillable());
                  sb.append("EnumerationWiseTreeElement Nil: " + wte.isNil()
                     + "  Nillable: " + wte.isNillable() + "\n");


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
                     //+ "  rawType: " + ((GroupTreeElement)te).getRawType()
                     + "  rawType: " + ((GroupTreeElement) te).getProtoType().getClassType()
                     + "  kind: " + te.getKind());
                  sb.append("child: arg name: " + te.getName()
                     + "  classType: " + te.getClassType()
                     //+ "  rawType: " + ((GroupTreeElement)te).getRawType()
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
   //---------- rls end

   public void addChild(GroupWiseTreeElement el) {

      el.incrementChildren();
   }

   public void removeChild(WiseTreeElement el) {

      ((GroupWiseTreeElement) el.getParent()).removeChild(el.getId());
   }

   public void lazyLoadChild(LazyLoadWiseTreeElement el) {

      try {
         el.resolveReference();
      } catch (Exception e) {
         error = ClientHelper.toErrorMessage(e);
         logException(e);
      }
   }

   public void onInputFocus(WiseTreeElement el) {

      el.setNotNil(true);
   }

   public void changePanel(ItemChangeEvent event) {

      String oldName = event.getOldItemName();
      String newName = event.getNewItemName();
      if (oldName != null && newName != null) {
         if (oldName.endsWith("step1")) {
            if (newName.endsWith("step2")) {
               readWsdl();
            }
         } else if (oldName.endsWith("step2")) {
            if (newName.endsWith("step3")) {
               parseOperationParameters();
            } else if (newName.endsWith("step1")) {
               this.error = null;
            }
         } else if (oldName.endsWith("step3")) {
            if (newName.endsWith("step4")) {
               performInvocation();
            } else if (newName.endsWith("step2")) {
               this.error = null;
            }
         } else if (oldName.endsWith("step4")) {
            if (newName.endsWith("step3")) {
               this.error = null;
            }
         }
      }
   }

   public void updateCurrentOperation(ItemChangeEvent event) {

      String ev = event.getNewItemName();
      //skip empty/null operation values as those comes from expansion/collapse of the menu panel
      if (ev != null && ev.length() > 0) {
         setCurrentOperation(ev);
      }
   }

   public boolean isResponseAvailable() {

      return outputTree != null || responseMessage != null;
   }

   private void cleanup() {

      if (client != null) {
         cleanupTask.removeRef(client);
         client.close();
         client = null;
      }
      services = null;
      currentOperation = null;
      currentOperationFullName = null;
      inputTree = null;
      outputTree = null;
      if (inTree != null) {
         inTree.clearInitialState();
      }
      inputTree = null;
      error = null;
      responseMessage = null;
      invocationUrl = null;
      treeElementMap.clear();
   }

   public String getWsdlUrl() {

      return wsdlUrl;
   }

   public void setWsdlUrl(String wsdlUrl) {

      this.wsdlUrl = wsdlUrl;
   }

   public String getWsdlUser() {

      return wsdlUser;
   }

   public void setWsdlUser(String wsdlUser) {

      if (wsdlUser != null && wsdlUser.length() == 0) {
         this.wsdlUser = null;
      } else {
         this.wsdlUser = wsdlUser;
      }
   }

   public String getWsdlPwd() {

      return wsdlPwd;
   }

   public void setWsdlPwd(String wsdlPwd) {

      if (wsdlPwd != null && wsdlPwd.length() == 0) {
         this.wsdlPwd = null;
      } else {
         this.wsdlPwd = wsdlPwd;
      }
   }

   public String getInvocationUrl() {

      return invocationUrl;
   }

   public void setInvocationUrl(String invocationUrl) {

      if (invocationUrl != null && invocationUrl.length() == 0) {
         this.invocationUrl = null;
      } else {
         this.invocationUrl = invocationUrl;
      }
   }

   public String getInvocationUser() {

      return invocationUser;
   }

   public void setInvocationUser(String invocationUser) {

      if (invocationUser != null && invocationUser.length() == 0) {
         this.invocationUser = null;
      } else {
         this.invocationUser = invocationUser;
      }
   }

   public String getInvocationPwd() {

      return invocationPwd;
   }

   public void setInvocationPwd(String invocationPwd) {

      if (invocationPwd != null && invocationPwd.length() == 0) {
         this.invocationPwd = null;
      } else {
         this.invocationPwd = invocationPwd;
      }
   }

   public List<Service> getServices() {

      return services;
   }

   public void setServices(List<Service> services) {

      this.services = services;
   }

   public String getCurrentOperation() {

      return currentOperation;
   }

   public String getCurrentOperationFullName() {

      return currentOperationFullName;
   }

   public void setCurrentOperation(String currentOperation) {

      this.currentOperation = currentOperation;
   }

   public UITree getInTree() {

      return inTree;
   }

   public void setInTree(UITree inTree) {

      this.inTree = inTree;
   }

   public TreeNodeImpl getInputTree() {

      return inputTree;
   }

   public void setInputTree(TreeNodeImpl inputTree) {

      this.inputTree = inputTree;
   }

   public TreeNodeImpl getOutputTree() {

      return outputTree;
   }

   public void setOutputTree(TreeNodeImpl outputTree) {

      this.outputTree = outputTree;
   }

   public String getError() {

      return error;
   }

   public void setError(String error) {

      this.error = error;
   }

   public String getRequestPreview() {

      return requestPreview;
   }

   public void setRequestPreview(String requestPreview) {

      this.requestPreview = requestPreview;
   }

   public String getRequestActiveTab() {

      return requestActiveTab;
   }

   public void setRequestActiveTab(String requestActiveTab) {

      this.requestActiveTab = requestActiveTab;
   }

   public String getResponseMessage() {

      return responseMessage;
   }

   public void setResponseMessage(String responseMessage) {

      this.responseMessage = responseMessage;
   }

   private static void logException(Exception e) {

      logger.error("", e);
   }
}
