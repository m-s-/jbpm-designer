package org.jbpm.designer.test.web.server;

import static junit.framework.Assert.*;

import java.net.URL;
import java.util.Date;

import javax.servlet.ServletContext;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jbpm.designer.test.web.AbstractGuvnorIntegrationTest;
import org.jbpm.designer.test.web.util.GuvnorInterfaceUtil;
import org.jbpm.designer.web.profile.IDiagramProfile;
import org.jbpm.designer.web.server.TransformerServlet;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@RunAsClient
public class TransformerServletTest extends AbstractGuvnorIntegrationTest {

    private static final String TO_PNG = "png";
        
    /**
     * This tests the following classes and methods: 
     * <ul>
     * <li>{@link TransformerServlet}</li>
     * </ul>
     * @throws Exception
     */
    @Test
    public void guvnorStoreTest() throws Exception { 
        runGuvnorStoreTest(guvnorUrl, profile, servletContext);
    }
    
    public static void runGuvnorStoreTest(URL guvnorUrl, IDiagramProfile profile, ServletContext servletContext) throws Exception { 
        // Setup
        GuvnorInterfaceUtil guvnor = GuvnorInterfaceUtil.instance(guvnorUrl);
        String packageName = "svgPkg" + sdf.format(new Date());
        guvnor.createPackageViaAtom(packageName);
        String rawSvg = readFile("/guvnor-integration/example.svg").toString();
        String assetName = "svg";

        assertFalse( "Asset shouldn't exist yet: " + assetName + "-image", 
                guvnor.checkIfAssetExists(packageName, assetName + "image"));
        
        // Run method
        TransformerServlet.guvnorStore(packageName, assetName, profile, rawSvg, TO_PNG);
       
        // Test results from method
        String assetXmlInfo = guvnor.getAssetInfo(packageName, assetName + "-image");
        assertTrue( assetXmlInfo != null && ! assetXmlInfo.isEmpty() );
        String origPublishedDate = getPublishedDate(assetXmlInfo);
        assertTrue( origPublishedDate != null && ! origPublishedDate.isEmpty() );
        
        // Rerun method
        TransformerServlet.guvnorStore(packageName, assetName, profile, rawSvg, TO_PNG);
       
        assetXmlInfo = guvnor.getAssetInfo(packageName, assetName + "-image");
        assertTrue( assetXmlInfo != null && ! assetXmlInfo.isEmpty() );
        String newPublishedDate = getPublishedDate(assetXmlInfo);
        
        assertTrue( "Published date should not be the same: " + origPublishedDate, ! origPublishedDate.equals(newPublishedDate) );
    }
    
}
