package org.jboss.wise.gui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: rsearls
 * Date: 3/13/15
 */
public class ParameterizedNode extends ParamNode implements Serializable {

   private static final long serialVersionUID = 1L;

   private String rawType;
   private List<String> valueList = new ArrayList<String>();

   public String getRawType() {

      return rawType;
   }

   public void setRawType(String rawType) {

      this.rawType = rawType;
   }

   public List<String> getValueList() {

      return valueList;
   }

   public void addValue(String value) {

      valueList.add(value);
   }
}
