package org.jboss.wise.gui.tree.element;

import java.util.ArrayList;
import java.util.List;

/**
 * User: rsearls
 * Date: 3/25/15
 */
public class CollectionTreeElement extends TreeElement {
   private List<String>  valueList = new ArrayList<String>();

   public List<String> getValueList() {
      return valueList;
   }

   public void addValue(String value) {
      valueList.add(value);
   }

   @Override
   public TreeElement clone () {
      return new CollectionTreeElement();
   }
}
