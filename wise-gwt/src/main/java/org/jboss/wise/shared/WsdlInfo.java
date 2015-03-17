package org.jboss.wise.shared;

import java.io.Serializable;

/**
 * User: rsearls
 * Date: 3/5/15
 */
public class WsdlInfo implements Serializable {
   public String wsdl;
   public String user;
   public String password;

   public WsdlInfo() {

   }

   public WsdlInfo(String wsdl, String user, String password) {

      this.wsdl = wsdl;
      this.user = user;
      this.password = password;
   }

   public void setWsdl(String wsdl) {

      this.wsdl = wsdl;
   }

   public String getWsdl() {

      return wsdl;
   }

   public void setUser(String user) {

      this.user = user;
   }

   public String getUser() {

      return user;
   }

   public void setPassword(String password) {

      this.password = password;
   }

   public String getPassword() {

      return password;
   }
}
