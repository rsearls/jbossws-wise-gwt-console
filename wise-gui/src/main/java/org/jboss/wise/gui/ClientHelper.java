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

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding;

import com.google.gwt.i18n.server.Parameter;
import org.jboss.wise.core.client.InvocationResult;
import org.jboss.wise.core.client.WSDynamicClient;
import org.jboss.wise.core.client.WSEndpoint;
import org.jboss.wise.core.client.WSMethod;
import org.jboss.wise.core.client.WSService;
import org.jboss.wise.core.client.WebParameter;
import org.jboss.wise.core.exception.ResourceNotAvailableException;
import org.jboss.wise.core.exception.WiseRuntimeException;
import org.jboss.wise.gui.model.TreeNode;
import org.jboss.wise.gui.treeElement.*;
import org.jboss.wise.gui.model.TreeNodeImpl;

import org.codehaus.jackson.map.ObjectMapper;

public class ClientHelper implements Serializable {

   private static final long serialVersionUID = 4838483183941121581L;

   public static WSMethod getWSMethod(String currentOperation, WSDynamicClient client) throws ResourceNotAvailableException {
      StringTokenizer st = new StringTokenizer(currentOperation, ";");
      String serviceName = st.nextToken();
      String portName = st.nextToken();
      String operationName = st.nextToken();
      return client.getWSMethod(serviceName, portName, operationName);
   }

   public static TreeNodeImpl convertOperationParametersToGui(WSMethod wsMethod, WSDynamicClient client) {
      WiseTreeElementBuilder builder = new WiseTreeElementBuilder(client, true);
      TreeNodeImpl rootElement = new TreeNodeImpl();
      Collection<? extends WebParameter> parameters = wsMethod.getWebParams().values();
      SOAPBinding soapBindingAnn = wsMethod.getEndpoint().getUnderlyingObjectClass().getAnnotation(SOAPBinding.class);
      boolean rpcLit = false;
      if (soapBindingAnn != null) {
         SOAPBinding.Style style = soapBindingAnn.style();
         rpcLit = style != null && SOAPBinding.Style.RPC.equals(style);
      }
      for (WebParameter parameter : parameters) {
         if (parameter.getMode() != WebParam.Mode.OUT) {
            WiseTreeElement wte = builder.buildTreeFromType(parameter.getType(), parameter.getName(),
               null, !rpcLit);
            //- rls test
            System.out.println("-## WiseTreeElement: " + wte.getClass().getName());
            System.out.println("-#### name: " + wte.getName() + "  type: " + wte.getType()
               + "  id: " + wte.getId()
               + "  classType: " + wte.getClassType().toString());

            if (wte instanceof EnumerationWiseTreeElement) {
               Map<String, String> eValuesMap = ((EnumerationWiseTreeElement)wte).getValidValue();
               if (eValuesMap != null) {
                  Set<String> eSet = eValuesMap.keySet();
                  for(String s : eSet) {
                     System.out.println("--- enum value: " + s);
                  }
               }
            }

            Iterator<Object> keys = wte.getChildrenKeysIterator();
            if (keys != null) {
               while (keys.hasNext()) {
                  WiseTreeElement tNode = (WiseTreeElement)wte.getChild(keys.next());
                  if (tNode != null) {
                     //String typeString = ( instanceof Class<?> ? ((Class<?>) type).getSimpleName() : type.toString());
                     System.out.println("-@@ TreeNode: " + tNode.getClass().getName());
                     System.out.println("-@@@@ name: " + tNode.getName() + "  type: "
                        + tNode.getType() + "  id: " + wte.getId() + "  classType: "
                        + tNode.getClassType().toString());

                     if (tNode instanceof EnumerationWiseTreeElement) {
                        Map<String, String> eValuesMap = ((EnumerationWiseTreeElement)tNode).getValidValue();
                        if (eValuesMap != null) {
                           Set<String> eSet = eValuesMap.keySet();
                           for(String s : eSet) {
                              System.out.println("-+++ enum value: " + s);
                           }
                        }
                     }
                  } else {
                     System.out.println("-@@@@ TreeNode is NULL");
                  }
               }
            }
            //-
            rootElement.addChild(wte.getId(), wte);
         }
      }
      return rootElement;
   }

   public static TreeNodeImpl convertOperationResultToGui(InvocationResult result, WSDynamicClient client) {
      WiseTreeElementBuilder builder = new WiseTreeElementBuilder(client, false);
      TreeNodeImpl rootElement = new TreeNodeImpl();
      Map<String, Type> resTypes = new HashMap<String, Type>();
      for (Entry<String, Object> res : result.getResult().entrySet()) {
         String key = res.getKey();
         if (key.startsWith(WSMethod.TYPE_PREFIX)) {
            resTypes.put(key, (Type) res.getValue());
         }
      }
      for (Entry<String, Object> res : result.getResult().entrySet()) {
         final String key = res.getKey();
         if (!key.startsWith(WSMethod.TYPE_PREFIX)) {
            Type type = resTypes.get(WSMethod.TYPE_PREFIX + key);
            if (type != void.class && type != Void.class) {
               WiseTreeElement wte = builder.buildTreeFromType(type, key, res.getValue(), true);
               rootElement.addChild(wte.getId(), wte);
            }
         }
      }
      return rootElement;
   }

   public static List<Service> convertServicesToGui(Map<String, WSService> servicesMap) {
      List<Service> services = new LinkedList<Service>();
      for (Entry<String, WSService> serviceEntry : servicesMap.entrySet()) {
         Service service = new Service();
         services.add(service);
         service.setName(serviceEntry.getKey());
         List<Port> ports = new LinkedList<Port>();
         service.setPorts(ports);
         for (Entry<String, WSEndpoint> endpointEntry : serviceEntry.getValue().processEndpoints().entrySet()) {
            Port port = new Port();
            port.setName(endpointEntry.getKey());
            ports.add(port);
            List<Operation> operations = new LinkedList<Operation>();
            port.setOperations(operations);
            for (Entry<String, WSMethod> methodEntry : endpointEntry.getValue().getWSMethods().entrySet()) {
               Operation operation = new Operation();
               operation.setId(operation.hashCode());
               operation.setName(methodEntry.getKey());
               StringBuilder sb = new StringBuilder();
               sb.append(methodEntry.getKey());
               sb.append("(");
               Iterator<? extends WebParameter> paramIt = methodEntry.getValue().getWebParams().values().iterator();
               while (paramIt.hasNext()) {
                  WebParameter param = paramIt.next();
                  Type type = param.getType();
                  String typeString = (type instanceof Class<?> ? ((Class<?>) type).getSimpleName() : type.toString());
                  sb.append(typeString);
                  sb.append(" ");
                  sb.append(param.getName());
                  if (paramIt.hasNext()) {
                     sb.append(", ");
                  }

               }
               sb.append(")");
               operation.setFullName(sb.toString());
               operations.add(operation);

               // rls added
               operation.setCurrentOperation(service.getName()
                  + ";" + port.getName()
                  + ";" + operation.getName());
            }
         }
      }
      return services;
   }

   //- rls start

   public static List<String> getOperationList(List<Service> services) {
      ArrayList<String> oList = new ArrayList<String>();
      if (services != null) {
         for (Service s : services) {
            for (Port p : s.getPorts()) {
               for (Operation o : p.getOperations()) {
                  StringBuilder sb = new StringBuilder();
                  sb.append(s.getName());
                  sb.append(";");
                  sb.append(p.getName());
                  sb.append(";");
                  sb.append(o.getName());
                  oList.add(sb.toString());
               }
            }
         }
      }
      return oList;
   }
   //- rls end
   public static String getFirstGuiOperation(List<Service> services) {
      if (services == null) {
         return null;
      }
      for (Service s : services) {
         for (Port p : s.getPorts()) {
            for (Operation o : p.getOperations()) {
               StringBuilder sb = new StringBuilder();
               sb.append(s.getName());
               sb.append(";");
               sb.append(p.getName());
               sb.append(";");
               sb.append(o.getName());
               return sb.toString();
            }
         }
      }
      return null;
   }

   public static String getOperationFullName(String currentGuiOperation, List<Service> services) {
      if (currentGuiOperation == null) {
         return null;
      }
      StringTokenizer st = new StringTokenizer(currentGuiOperation, ";");
      String serviceName = st.nextToken();
      String portName = st.nextToken();
      String operationName = st.nextToken();
      for (Service s : services) {
         if (serviceName.equals(s.getName())) {
            for (Port p : s.getPorts()) {
               if (portName.equals(p.getName())) {
                  for (Operation o : p.getOperations()) {
                     if (operationName.equals(o.getName())) {
                        return o.getFullName();
                     }
                  }
               }
            }
         }
      }
      return null;
   }

   public static Map<String, Object> processGUIParameters(TreeNodeImpl inputTree) {
      Map<String, Object> params = new HashMap<String, Object>();
      for (Iterator<Object> it = inputTree.getChildrenKeysIterator(); it.hasNext(); ) {
         WiseTreeElement wte = (WiseTreeElement) inputTree.getChild(it.next());
         params.put(wte.getName(), wte.isNil() ? null : wte.toObject());
      }
      return params;
   }

   public static void addOUTParameters(Map<String, Object> params, WSMethod wsMethod, WSDynamicClient client) {
      WiseTreeElementBuilder builder = null;
      final Collection<? extends WebParameter> parameters = wsMethod.getWebParams().values();
      for (WebParameter parameter : parameters) {
         if (parameter.getMode() == WebParam.Mode.OUT) {
            if (builder == null) {
               builder = new WiseTreeElementBuilder(client, true);
            }
            WiseTreeElement wte = builder.buildTreeFromType(parameter.getType(), parameter.getName(),
               null, true);
            params.put(wte.getName(), wte.isNil() ? null : wte.toObject());
         }
      }
   }

   public static String toErrorMessage(Exception e) {
      StringBuilder sb = new StringBuilder();
      if (e instanceof WiseRuntimeException) {
         sb.append(e.getMessage());
      } else {
         sb.append(e.toString());
      }
      if (e.getCause() != null) {
         sb.append(", caused by ");
         sb.append(e.getCause());
      }
      sb.append(". Please check logs for further information.");
      return sb.toString();
   }
}
