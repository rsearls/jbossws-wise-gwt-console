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

import javax.jws.WebService;
import javax.xml.ws.BindingType;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.HashMap;
import java.util.List;

/**
 * Test Endpoint
 */
@WebService(name = "Endpoint", targetNamespace = "http://ws.jboss.org/datatypes")
@BindingType(value="http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true")
public interface Endpoint
{

   public Photo echo(final Photo photo);

   //----------- rls start
   public byte[] echoByte(byte[] byteArray);
   public String echoString(String str);
   public Integer echoInteger(Integer iNumber);
   public Double echoDouble(Double dNumber);
   public Float  echoFloat(Float fNumber);
   public Duration echoDuration(Duration duration);
   public XMLGregorianCalendar echoXMLGregorianCalendar (XMLGregorianCalendar gCal);
   public List<String> echoList(List<String> list);
   public HashMap<String, String> echoMap(HashMap<String, String> map);
   public String echoPrimaryMultiArgs(String str, Integer iNumber);
   //----------- rls end
}
