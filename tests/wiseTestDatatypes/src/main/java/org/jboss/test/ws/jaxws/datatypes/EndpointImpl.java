/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.ws.jaxws.datatypes;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import javax.jws.WebService;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceException;
import javax.activation.DataHandler;

//import sun.util.logging.resources.logging.Logger;

/**
 * Test Endpoint
 */
@WebService(name = "Endpoint", serviceName = "EndpointService", targetNamespace = "http://ws.jboss.org/datatypes", endpointInterface = "org.jboss.test.ws.jaxws.datatypes.Endpoint")
@BindingType(value = "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true")
public class EndpointImpl implements Endpoint {

   //private static final Logger log = Logger.getLogger(EndpointImpl.class);

   public Photo echo(Photo photo) {

      DataHandler dh = photo.getImage();
      String contentType = dh.getContentType();
      //log.info("Actual content-type " + contentType);
      String expectedContentType = photo.getExpectedContentType();
      //log.info("Expected content-type " + expectedContentType);

      if (expectedContentType.equals(contentType) == false) {
         throw new WebServiceException("Expected content-type '" + expectedContentType + "' Actual content-type '" + contentType + "'");
      }

      try {
         Object content = dh.getContent();
         //log.info("Content - " + content.toString());
         if (!(content instanceof Image) && !(content instanceof InputStream)) {
            throw new WebServiceException("Unexpected content '" + content.getClass().getName() + "'");
         }
      } catch (IOException e) {
         throw new WebServiceException("Unable to getContent()", e);
      }

      return photo;
   }


   //----------- rls start
   public byte[] echoByte(byte[] byteArray) {

      return byteArray;
   }

   public String echoString(String str) {

      return str;
   }

   public Integer echoInteger(Integer iNumber) {

      return iNumber;
   }

   public Double echoDouble(Double dNumber) {

      return dNumber;
   }

   public Float echoFloat(Float fNumber) {

      return fNumber;
   }

   public Duration echoDuration(Duration duration) {

      return duration;
   }

   public XMLGregorianCalendar echoXMLGregorianCalendar(XMLGregorianCalendar gCal) {

      return gCal;
   }

   public List<String> echoList(List<String> list) {

      return list;
   }

   public HashMap<String, String> echoMap(HashMap<String, String> map) {

      return map;
   }

   public String echoPrimaryMultiArgs(String str, Integer iNumber) {

      StringBuilder sb = new StringBuilder();
      if (str != null && !str.isEmpty()) {
         sb.append("String found\n");
      }
      if (iNumber != null) {
         sb.append("Integer found\n");
      }
      return sb.toString();
   }
   //----------- rls end
}
