package org.jboss.wise.gui.tree.element;

import java.io.Serializable;

/**
 * User: rsearls
 * Date: 3/27/15
 */
public class SimpleTreeElement extends TreeElement implements Serializable {
   protected String value;

   public String getValue() {

      return value;
   }

   public void setValue(String value) {

      this.value = value;
   }

   @Override
   public TreeElement clone(){
      SimpleTreeElement clone = new SimpleTreeElement();
      clone.setKind(getKind());
      clone.setName(getName());
      clone.setClassType(getClassType());
      clone.setValue(getValue());
      return  clone;
   }
}
