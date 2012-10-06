package org.jbpm.designer.test.assets;

import java.net.URL;

import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jbpm.designer.assets.domain.AssetXml;
import org.jbpm.designer.assets.persistence.AssetEntity;
import org.jbpm.designer.assets.services.AssetResource;
import org.jbpm.designer.assets.services.AssetResourceBean;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Ignore
public class AssetResourceBeanTest
{
   private static final String REST_PATH = "rest";
   
   @Deployment(testable = false)
   public static Archive<?> createDeployment()
   {
      return ShrinkWrap.create(WebArchive.class)
            .addClass(AssetXml.class)
            .addClass(AssetEntity.class)
            .addClasses(AssetResource.class)
            .addClasses(AssetResourceBean.class)
            .addAsManifestResource("persistence.xml")
            .setWebXML("web.xml");
   }

   @Test
   public void testGetCustomerByIdUsingClientRequest(@ArquillianResource URL base) throws Exception
   {
      // GET http://localhost:8080/test/rest/customer/1
      ClientRequest request = new ClientRequest(new URL(base, REST_PATH + "/customer/1").toExternalForm());
      request.header("Accept", MediaType.APPLICATION_XML);

      // we're expecting a String back
      ClientResponse<String> responseObj = request.get(String.class);

      Assert.assertEquals(200, responseObj.getStatus());
      
      Assert.assertEquals(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
       "<customer><id>1</id><name>Acme Corporation</name></customer>",
       responseObj.getEntity());
   }
}