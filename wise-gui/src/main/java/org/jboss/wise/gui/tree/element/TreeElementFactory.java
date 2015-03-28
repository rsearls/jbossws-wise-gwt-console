package org.jboss.wise.gui.tree.element;

import java.util.Map;
import org.jboss.wise.gui.treeElement.EnumerationWiseTreeElement;
import org.jboss.wise.gui.treeElement.GroupWiseTreeElement;
import org.jboss.wise.gui.treeElement.SimpleWiseTreeElement;
import org.jboss.wise.gui.treeElement.WiseTreeElement;

/**
 * User: rsearls
 * Date: 3/23/15
 */
public class TreeElementFactory {

   public static TreeElement create (WiseTreeElement wte) {
      TreeElement treeElement;

      if (WiseTreeElement.GROUP.equals(wte.getKind())) {
         treeElement = new GroupTreeElement();

      } else if (WiseTreeElement.ENUMERATION.equals(wte.getKind())) {
         treeElement = new EnumerationTreeElement();

      } else {
         treeElement = new SimpleTreeElement();
      }
      return treeElement;
   }
}
