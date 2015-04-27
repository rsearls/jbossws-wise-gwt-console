package org.jboss.wise.gui.tree.element;

/**
 * User: rsearls
 * Date: 4/25/15
 */
public class ComplexTreeElement extends SimpleTreeElement {

   @Override
   public TreeElement clone() {

      ComplexTreeElement clone = new ComplexTreeElement();
      clone.setKind(getKind());
      clone.setName(getName());
      clone.setClassType(getClassType());

      for (TreeElement child : getChildren()) {
         clone.addChild(child.clone());
      }

      return clone;
   }
}
