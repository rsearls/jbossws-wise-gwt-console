package org.jboss.wise.gui;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * User: rsearls
 * Date: 3/11/15
 */
public class ParamNode implements Serializable {

   private static final long serialVersionUID = 1L;

   private String name;
   private String classType;
   private List<ParamNode> children = new LinkedList<ParamNode>();
   private String value;

   public String getName(){
      return name;
   }
   public void setName(String name) {
      this.name = name;
   }
   public String getClassType() {
      return classType;
   }
   public void setClassType(String classType) {
      this.classType = classType;
   }
   public void addChild(ParamNode child) {
      children.add(child);
   }
   public List<ParamNode> getChildren() {
      return children;
   }

   public String getValue() {
      return value;
   }

   public void setValue(String value) {
      this.value = value;
   }
}
