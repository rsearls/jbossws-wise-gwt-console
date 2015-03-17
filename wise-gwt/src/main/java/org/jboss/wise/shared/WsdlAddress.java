package org.jboss.wise.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class WsdlAddress implements Serializable {
   private String id;
   private String displayName;

   public WsdlAddress() {

      new WsdlAddress("0", "");
   }

   public WsdlAddress(String id, String displayName) {

      this.id = id;
      this.displayName = displayName;
   }

   public String getId() {

      return id;
   }

   public void setId(String id) {

      this.id = id;
   }

   public String getDisplayName() {

      return displayName;
   }

   public void setDisplayName(String displayName) {

      this.displayName = displayName;
   }
}
