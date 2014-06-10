package com.advancedtools.webservices.rt.xfire;

import org.codehaus.xfire.XFireFactory;
import org.codehaus.xfire.XFire;
import org.codehaus.xfire.service.binding.ObjectServiceFactory;
import org.codehaus.xfire.service.ServiceFactory;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.invoker.ObjectInvoker;

import javax.xml.namespace.QName;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: Maxim
 * Date: Oct 17, 2006
 * Time: 6:56:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class WsdlGenerator {
  public static void main(String[] args) throws IOException {
    final String webServiceClassName = args[0];
    final String webServiceName = args[1];
    final String webServiceNs = args[2];
    final String webServiceUrl = args[3];
    final String wsdlOutFileName = args[4];
    Class serviceClazz;

    try {
      serviceClazz = Class.forName(webServiceClassName);
    } catch (ClassNotFoundException e) {
      System.err.println("Class " + webServiceClassName + " not found");
      System.exit(-1);
      return;
    }

    XFire xfire = XFireFactory.newInstance().getXFire();
    ServiceFactory factory = new ObjectServiceFactory(xfire.getTransportManager());
    Service service = factory.create(serviceClazz, webServiceName, webServiceNs, null);

    service.setProperty(ObjectInvoker.SERVICE_IMPL_CLASS, serviceClazz);

    service.getWSDLWriter().write(new BufferedOutputStream(new FileOutputStream(wsdlOutFileName)));
    
  }
}
