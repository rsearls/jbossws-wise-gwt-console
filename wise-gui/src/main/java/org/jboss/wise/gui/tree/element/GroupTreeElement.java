package org.jboss.wise.gui.tree.element;

import java.util.ArrayList;
import java.util.List;
import org.jboss.wise.gui.tree.element.TreeElement;

/**
 * User: rsearls
 * Date: 3/21/15
 */
public class GroupTreeElement extends TreeElement {

   private String rawType;
   private /*Collection*/ TreeElement protoType;
   //private List<String> values = new ArrayList<String>();
   private List<TreeElement> valueList = new ArrayList<TreeElement>();

   public List<TreeElement> getValueList() {

      return valueList;
   }

   public void addValue(TreeElement value) {

      valueList.add(value);
   }

   public String getRawType() {

      return rawType;
   }

   public void setRawType(String rawType) {

      this.rawType = rawType;
   }
   /**
   public List<String> getValues() {

      return values;
   }
   **/
   /**
    * public void addValue(String value) {
    * <p/>
    * this.values.add(value);
    * }
    * <p/>
    * public void setValues(List<String> values) {
    * <p/>
    * this.values = values;
    * }
    * *
    */


   public /*Collection*/ TreeElement getProtoType() {

      return protoType;
   }

   public void setProtoType(/*Collection*/ TreeElement protoType) {

      this.protoType = protoType;
   }

   @Override
   public TreeElement clone(){
      GroupTreeElement clone = new GroupTreeElement();
      clone.setKind(getKind());
      clone.setName(getName());
      clone.setClassType(getClassType());
      clone.setProtoType(protoType.clone());
      //clone.getValueList().addAll(getValueList());
      return  clone;
   }
}
