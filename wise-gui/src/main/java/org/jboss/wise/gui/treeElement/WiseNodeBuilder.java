package org.jboss.wise.gui.treeElement;

import org.jboss.logging.Logger;
import org.jboss.wise.core.client.WSDynamicClient;
import org.jboss.wise.core.exception.WiseRuntimeException;
import org.jboss.wise.core.utils.JavaUtils;
import org.jboss.wise.core.utils.ReflectionUtils;
import org.jboss.wise.gui.ParamNode;
import org.jboss.wise.gui.ParameterizedNode;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.ws.Holder;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * User: rsearls
 * Date: 3/13/15
 */
public class WiseNodeBuilder {

   private static Logger logger = Logger.getLogger(WiseNodeBuilder.class);

   private final WSDynamicClient client;
   private final boolean request;
   private final boolean trace;

   public WiseNodeBuilder(WSDynamicClient client, boolean request) {
      this.client = client;
      this.request = request;
      this.trace = logger.isTraceEnabled();
   }

   public /*WiseTreeElement*/ ParamNode buildTreeFromType(Type type, String name, Object value,
                                                          boolean nillable) {
      return buildTreeFromType(type, name, value, nillable, null, null,
         new HashMap<Type, WiseTreeElement>(), new HashSet<Type>());
   }

   private /*WiseTreeElement*/ ParamNode buildTreeFromType(Type type,
                                             String name,
                                             Object obj,
                                             boolean nillable,
                                             Class<?> scope,
                                             String namespace,
                                             Map<Type, WiseTreeElement> typeMap,
                                             Set<Type> stack) {
      System.out.println("BBB buildTreeFromType: type: "
         + ((type == null)? "NULL" : type.toString()) + "  name: "
         + ((name == null)? "NULL" : name)
         + "  obj: " + ((obj== null) ? "NULL" : obj.toString()));
      if (trace)
         logger.trace("=> Converting parameter '" + name + "', type '" + type + "'");
      if (type instanceof ParameterizedType) {
         ParameterizedType pt = (ParameterizedType) type;
         return this.buildParameterizedType(pt, name, obj, scope, namespace, typeMap, stack);
      } else {
         return this.buildFromClass((Class<?>) type, name, obj, nillable, typeMap, stack);

      }
   }

   @SuppressWarnings("rawtypes")
   private /*WiseTreeElement*/ ParamNode buildParameterizedType(ParameterizedType pt,
                                                  String name,
                                                  Object obj,
                                                  Class<?> scope,
                                                  String namespace,
                                                  Map<Type, WiseTreeElement> typeMap,
                                                  Set<Type> stack) {
      System.out.println("BBB buildParameterizedType: type: "
         + ((pt == null)? "NULL" : pt.getActualTypeArguments()[0].toString())
         + "  name: " + ((name == null)? "NULL" : name)
         + "  obj: " + ((obj == null) ? "NULL" : obj.toString())
         + "  rawType: " + ((pt == null)? "NULL" : getCleanClassName(pt.getRawType().toString())));
      Type firstTypeArg = pt.getActualTypeArguments()[0];
      if (Collection.class.isAssignableFrom((Class<?>) pt.getRawType())) {
         GroupWiseTreeElement group;
         ParameterizedNode pNode;
         if (obj != null || request) {
            /**
            WiseTreeElement prototype = this.buildTreeFromType(firstTypeArg, name,
               null, true, null, null, typeMap, stack);
            group = new GroupWiseTreeElement(pt, name, prototype);
            **/
            //
            ParamNode prototype = this.buildTreeFromType(firstTypeArg, name,
               null, true, null, null, typeMap, stack);
            pNode = new ParameterizedNode();
            pNode.setClassType(getCleanClassName(prototype.getClassType().toString()));
            pNode.setName(prototype.getName());
            pNode.setRawType(getCleanClassName(pt.getRawType().toString()));
            System.out.println("BBB buildParameterizedType: group created  classType: "
               + pNode.getClassType() + "  name: " + pNode.getName()
               + "  rawType: " + pNode.getRawType());
            /**
            if (obj != null) {
               for (Object o : (Collection) obj) {
                  System.out.println("BBB buildParameterizedType: group child: " + o.toString());
                  group.addChild(IDGenerator.nextVal(), this.buildTreeFromType(firstTypeArg, name, o,
                     true, null, null, typeMap, stack));
               }
            }
            **/
         } else {

            //group = new GroupWiseTreeElement(pt, name, null);
            //
            pNode = new ParameterizedNode();
            pNode.setClassType(getCleanClassName(pt.toString()));
            pNode.setName(name);
            pNode.setRawType(getCleanClassName(pt.getRawType().toString()));
            System.out.println("BBB buildParameterizedType: group not children  classType: "
               + pNode.getClassType() + "  name: " + pNode.getName()
               + "  rawType: " + pNode.getRawType());

         }
         //return group;
         return pNode;
      } else {
         if (obj != null && obj instanceof JAXBElement) {
            obj = ((JAXBElement) obj).getValue();
         } else if (obj != null && obj instanceof Holder) {
            obj = ((Holder) obj).value;
         }
         /**
         WiseTreeElement element = this.buildTreeFromType(firstTypeArg, name, obj,
            true, null, null, typeMap, stack);
         ParameterizedWiseTreeElement parameterized = new ParameterizedWiseTreeElement(pt,
            (Class<?>) pt.getRawType(), name, client, scope, namespace);
         parameterized.addChild(element.getId(), element);
         **/
         //
         ParamNode element = this.buildTreeFromType(firstTypeArg, name, obj,
            true, null, null, typeMap, stack);
         ParameterizedNode pNode = new ParameterizedNode();
         pNode.setRawType(getCleanClassName(((Class<?>) pt.getRawType()).getName()));
         pNode.setClassType(getCleanClassName(element.getClassType().toString()));
         pNode.setName(element.getName());
         System.out.println("BBB buildParameterizedType: create parameterized obj  classType: "
            + pNode.getClassType() + "  name: " + pNode.getName()
            + "  rawType: " + pNode.getRawType());

         //return parameterized;
         return pNode;
      }
   }

   private /*WiseTreeElement*/ ParamNode buildFromClass(Class<?> cl,
                                          String name,
                                          Object obj,
                                          boolean nillable,
                                          Map<Type, WiseTreeElement> typeMap,
                                          Set<Type> stack) {

      System.out.println("BBB buildFromClass: class: "
         + ((cl == null)? "NULL" : cl.toString()) + "  name: "
         + ((name == null)? "NULL" : name)
         + "  obj: " + ((obj == null) ? "NULL" : obj.toString()));
      if (cl.isArray()) {
         if (trace)
            logger.trace("* array, component type: " + cl.getComponentType());
         if (byte.class.equals(cl.getComponentType())) {
            ByteArrayWiseTreeElement element = new ByteArrayWiseTreeElement(cl, name, null);
            if (obj != null) {
               element.parseObject(obj);
            }
            //System.out.println("BBB buildFromClass: ByteArray created" );
            System.out.println("BBB buildFromClass: ByteArray created    ComponentType: "
               + cl.getComponentType().toString() + "  name: " + name);
            ParamNode pNode = new ParamNode();
            pNode.setName(name);
            pNode.setClassType(cl.getComponentType().toString());

            //return element;
            return pNode;
         }
         throw new WiseRuntimeException("Converter doesn't support this Object[] yet.");
      }

      if (isSimpleType(cl, client)) {
         if (trace)
            logger.trace("* simple");
         SimpleWiseTreeElement element = SimpleWiseTreeElementFactory.create(cl, name, obj);
         if (!nillable) {
            element.enforceNotNillable();
         }

         ParamNode pNode = new ParamNode();
         pNode.setName(name);
         String classType = ((Class<?>) cl).getName();
         pNode.setClassType(classType);
         System.out.println("BBB buildFromClass: SimpleType created  classType: "
            + pNode.getClassType() + "  name: " + pNode.getName());
         //return element;
         return pNode;
      } else { // complex
         if (request && stack.contains(cl)) {
            if (trace)
               logger.trace("* lazy");

            ParamNode pNode = new ParamNode();
            pNode.setName(name);
            String classType = ((Class<?>) cl).getName();
            pNode.setClassType(classType);
            System.out.println("BBB buildFromClass: LazyLoad created classType: "
               + pNode.getClassType() + "  name: " + pNode.getName());
            //return new LazyLoadWiseTreeElement(cl, name, typeMap);
            return pNode;
         }

         if (trace)
            logger.trace("* complex");

         ParamNode pNode = new ParamNode();
         pNode.setName(name);
         String classType = ((Class<?>) cl).getName();
         pNode.setClassType(classType);
         System.out.println("BBB buildFromClass: ComplexWise created classType: "
            + pNode.getClassType() + "  name: " + pNode.getName());
         ComplexWiseTreeElement complex = new ComplexWiseTreeElement(cl, name);
         if (request) {
            stack.add(cl);
         }
         for (Field field : ReflectionUtils.getAllFields(cl)) {
            XmlElement elemAnnotation = field.getAnnotation(XmlElement.class);
            XmlElementRef refAnnotation = field.getAnnotation(XmlElementRef.class);
            String fieldName = null;
            String namespace = null;
            if (elemAnnotation != null && !elemAnnotation.name().startsWith("#")) {
               fieldName = elemAnnotation.name();
            }
            if (refAnnotation != null) {
               fieldName = refAnnotation.name();
               namespace = refAnnotation.namespace();
            }
            final String xmlName = fieldName;
            if (fieldName == null) {
               fieldName = field.getName();
            }
            //String fieldName = (annotation != null && !annotation.name().startsWith("#")) ? annotation.name() : field.getName();
            Object fieldValue = null;
            if (obj != null) {
               try {
                  Method getter = cl.getMethod(ReflectionUtils.getGetter(field, xmlName),
                     (Class[]) null);
                  fieldValue = getter.invoke(obj, (Object[]) null);
               } catch (Exception e) {
                  throw new WiseRuntimeException("Error calling getter method for field " + field, e);
               }
            }

            /**
            System.out.println("BBB buildFromClass: Element created" );
            WiseTreeElement element = this.buildTreeFromType(field.getGenericType(), fieldName,
               fieldValue, true, cl, namespace, typeMap, stack);
            complex.addChild(element.getId(), element);
            **/
            //- pNode.addChild(element);
         }
         if (request) {
            stack.remove(cl);
            typeMap.put(cl, complex.clone());
         }
         if (!nillable) {
            complex.setNillable(false);
         }
         //return complex;
         return pNode;
      }
   }

   private static boolean isSimpleType(Class<?> cl, WSDynamicClient client) {
      return cl.isEnum() || cl.isPrimitive() || client.getClassLoader() != cl.getClassLoader();
   }


   private static String getDefaultValue(Class<?> cl) {
      System.out.println("BBB getDefaultValue: cl: " + cl.getName());
      if (cl.isPrimitive()) {
         cl = JavaUtils.getWrapperType(cl);
      }
      if ("java.lang.String".equalsIgnoreCase(cl.getName())) {
         return "";
      } else if ("java.lang.Boolean".equalsIgnoreCase(cl.getName())) {
         return "false";
      } else if ("java.lang.Byte".equalsIgnoreCase(cl.getName())) {
         return "0";
      } else if ("java.lang.Character".equalsIgnoreCase(cl.getName())) {
         return "";
      } else if ("java.lang.Double".equalsIgnoreCase(cl.getName())) {
         return "0.0";
      } else if ("java.lang.Float".equalsIgnoreCase(cl.getName())) {
         return "0.0";
      } else if ("java.lang.Integer".equalsIgnoreCase(cl.getName())) {
         return "0";
      } else if ("java.lang.Long".equalsIgnoreCase(cl.getName())) {
         return "0";
      } else if ("java.lang.Short".equalsIgnoreCase(cl.getName())) {
         return "0";
      } else if ("java.math.BigDecimal".equalsIgnoreCase(cl.getName())) {
         return "0.0";
      } else if ("java.math.BigInteger".equalsIgnoreCase(cl.getName())) {
         return "0";
      } else if ("java.lang.Object".equalsIgnoreCase(cl.getName())) {
         return "";
      } else {
         throw new WiseRuntimeException("Class type not supported: " + cl);
      }
   }

   private static String getCleanClassName(String src) {
      String tmpStr = src;
      int index = src.trim().lastIndexOf(" ");
      if (index > -1) {
         tmpStr = src.substring(index + 1);
      }
      return tmpStr;
   }
}
