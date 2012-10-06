package org.jbpm.designer.test.web.preprocessing;

import static junit.framework.Assert.*;
import static org.jbpm.designer.web.preprocessing.impl.JbpmPreprocessingUnit.*;

import java.net.URL;
import java.util.*;

import javax.servlet.ServletContext;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jbpm.designer.test.web.AbstractGuvnorIntegrationTest;
import org.jbpm.designer.test.web.util.GuvnorInterfaceUtil;
import org.jbpm.designer.web.preprocessing.impl.JbpmPreprocessingUnit;
import org.jbpm.designer.web.preprocessing.impl.JbpmPreprocessingUnit.ThemeInfo;
import org.jbpm.designer.web.profile.IDiagramProfile;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;


@RunWith(Arquillian.class)
@RunAsClient
public class JbpmPreprocessingUnitTest extends AbstractGuvnorIntegrationTest {

    /**
     * Tests<ul>
     * <li>{@link JbpmPreprocessingUnit#setupDefaultWorkitemConfigs(String, List, org.jbpm.designer.web.profile.IDiagramProfile)}</li>
     * <li>{@link JbpmPreprocessingUnit#findWorkitemInfoForUUID(String, List, org.jbpm.designer.web.profile.IDiagramProfile)}</li>
     * <li>{@link JbpmPreprocessingUnit#getWorkitemConfigContent(Map, org.jbpm.designer.web.profile.IDiagramProfile)}</li>
     * </ul>
     * @throws Exception If something goes wrong. 
     */
    @Test
    @InSequence(1)
    public void workItemConfigMethodsTest(@ArquillianResource URL url) throws Exception { 
        runWorkItemConfigMethodsTest(guvnorUrl, profile, servletContext);
    }
    
    public static void runWorkItemConfigMethodsTest(URL url, IDiagramProfile profile, ServletContext servletContext) throws Exception { 
        setupGuvnor(url, profile);
        
        JbpmPreprocessingUnit preProcUnit = new JbpmPreprocessingUnit(new MockServletContext("guvnor-integration"));

        // Setup
        String packageName = packageNameList[1];
        String uuid = packageToAssetUuidListMap.get(packageName).get(0);
        assertNotNull(uuid);
        List<String> packageNames = new ArrayList<String>();
        packageNames.add(packageName);
        
        // Run methods
        preProcUnit.setupDefaultWorkitemConfigs(uuid, packageNames, profile);
        Map<String, List<String>> configInfo = preProcUnit.findWorkitemInfoForUUID(uuid, packageNames, profile);
        assertTrue( "Returned work item info is empty.", configInfo != null && ! configInfo.isEmpty());
        
        assertTrue( "Work item info does not contain the correct package (" + packageName + ")", 
                configInfo.keySet().contains(packageName) );
        Map<String, String> workItemConfig = preProcUnit.getWorkitemConfigContent(configInfo, profile);
        assertTrue( "Work item configuration is empty.", workItemConfig != null & ! workItemConfig.isEmpty() );
        String key = workItemConfig.keySet().iterator().next();
        assertEquals( "Work item contains unexpected key: " + key, "WorkDefinitions", key);
        String val = workItemConfig.get(key);
        assertTrue( "Work item configuration does not contain expected string: " + val, 
                val.contains("This String Verifies That The Correct wid was Loaded"));
        
    }
    
    @Test
    @InSequence(2)
    public void setupCustomEditorsTest() throws Exception {
        runSetupCustomEditorsTest(guvnorUrl, profile, servletContext);
    }
    
    public static void runSetupCustomEditorsTest(URL url, IDiagramProfile profile, ServletContext servletContext) throws Exception {
        JbpmPreprocessingUnit preProcUnit = new JbpmPreprocessingUnit(new MockServletContext("guvnor-integration"));

        GuvnorInterfaceUtil guvnor = GuvnorInterfaceUtil.instance(url);
        guvnor.deleteAsset(GLOBAL_AREA, JbpmPreprocessingUnit.CUSTOMEDITORS_NAME, false);
       
        // Run method
        preProcUnit.setupCustomEditors(profile);
        
        // Test that method created expected things
        String customEditorJsonSource = guvnor.getAssetSource(GLOBAL_AREA, JbpmPreprocessingUnit.CUSTOMEDITORS_NAME);
        assertTrue( "Custom editor JSON is empty.", customEditorJsonSource != null && ! customEditorJsonSource.isEmpty());
        assertTrue( "Custom editor JSON does not contain expected string.",
                customEditorJsonSource.contains("This String Verifies That The Correct Custom Editor was Loaded"));

        // Retrieve published date of asset
        String assetInfo = guvnor.getAssetInfo(GLOBAL_AREA, JbpmPreprocessingUnit.CUSTOMEDITORS_NAME);
        String publishedDate = getPublishedDate(assetInfo);
        
        // Run method
        preProcUnit.setupCustomEditors(profile);
        
        // Test that it hasn't been overwritten
        assetInfo = guvnor.getAssetInfo(GLOBAL_AREA, JbpmPreprocessingUnit.CUSTOMEDITORS_NAME);
        String newPublishedDate = getPublishedDate(assetInfo);
        assertEquals(publishedDate, newPublishedDate);
    }

    
    @Test
    @InSequence(3)
    public void setupThemesTest() throws Exception {
        runSetupThemesTest(guvnorUrl, profile, servletContext);
    }
    
    public static void runSetupThemesTest(URL guvnorUrl, IDiagramProfile profile, ServletContext servletContext) throws Exception {
        JbpmPreprocessingUnit preProcUnit = new JbpmPreprocessingUnit(servletContext);
        String packageName = GLOBAL_AREA;
        
        // setup
        GuvnorInterfaceUtil guvnor = GuvnorInterfaceUtil.instance(guvnorUrl);
        guvnor.deleteAsset(packageName, THEME_NAME, false);
        
        // Retrieve original information
        String themesJsonStr = readFile("/defaults/themes.json").toString();
        Map<String, ThemeInfo> originThemeData = preProcUnit.convertJsonToThemeInfoMap(themesJsonStr, DEFAULT_THEME_NAME);
                
        // Run method
        preProcUnit.setupThemes(profile, new MockHttpServletRequest(servletContext));

        // Check if method created asset
        guvnor.checkIfAssetExists(packageName, THEME_NAME);
        
        // Retrieve stored theme data..
        themesJsonStr = guvnor.getAssetSource(packageName, THEME_NAME);
        Map<String, ThemeInfo> storedThemeData = preProcUnit.convertJsonToThemeInfoMap(themesJsonStr, DEFAULT_THEME_NAME);
        
        //.. and compare to what setupThemes() should have stored (original data)
        for( String key : originThemeData.keySet() ) { 
            ThemeInfo orig = originThemeData.get(key);
            ThemeInfo stored = storedThemeData.get(key);
            assertNotNull("ThemeInfo not found for " + key, stored);
            assertEquals("BgColor for " + key + " is not the same.", orig.getBgColor(), stored.getBgColor());
            assertEquals("BorderColor for " + key + " is not the same.", orig.getBorderColor(), stored.getBorderColor());
            assertEquals("FontColor for " + key + " is not the same.", orig.getFontColor(), stored.getFontColor());
        }
    }
    
    @Test
    @InSequence(3)
    public void setupDefaultIconsTest() throws Exception { 
        runSetupDefaultIconsTest(guvnorUrl, profile, servletContext);
    }
    
    public static void runSetupDefaultIconsTest(URL guvnorUrl, IDiagramProfile profile, ServletContext servletContext) throws Exception { 
        String emailIcon = "defaultemailicon";
        String logIcon = "defaultlogicon";
        String serviceNodeIcon = "defaultservicenodeicon";
        String [] icons = { emailIcon, logIcon, serviceNodeIcon };
        
        // Setup
        JbpmPreprocessingUnit preProcUnit = new JbpmPreprocessingUnit(servletContext);
        GuvnorInterfaceUtil guvnor = GuvnorInterfaceUtil.instance(guvnorUrl);
       
        String packageName = "defIcons" + sdf.format(new Date());
        guvnor.createPackageViaAtom(packageName);
        
        // Run method (create icons in specified package)
        String [] info = { packageName, "" };
        String today = sdfPublished.format(new Date());
        preProcUnit.setupDefaultIcons(info, profile);
        
        // Test that method created expected icons
        HashMap<String, String> iconPubDateMap = new HashMap<String, String>();
        for( String icon : icons ) { 
            String assetXmlInfo = guvnor.getAssetInfo(packageName, icon);
            String pubDate = getPublishedDate(assetXmlInfo);
            assertTrue( "Asset was not created today but on " + pubDate, pubDate.startsWith(today) );
            iconPubDateMap.put(icon, pubDate);
        }
            
        // Rerun method
        preProcUnit.setupDefaultIcons(info, profile);
        
        // Test that icon hasn't been overwritten
        for( String icon : icons ) { 
            String assetXmlInfo = guvnor.getAssetInfo(packageName, icon);
            String pubDate = getPublishedDate(assetXmlInfo);
            assertEquals( icon + " was published on unexpected date.", iconPubDateMap.get(icon), pubDate);
        }
    }
    
    @Test
    @InSequence(4)
    public void findPackagesTest() throws Exception { 
        runFindPackagesTest(guvnorUrl, profile, servletContext);
    }
    
    public static void runFindPackagesTest(URL guvnorUrl, IDiagramProfile profile, ServletContext servletContext) throws Exception { 
        JbpmPreprocessingUnit preProcUnit = new JbpmPreprocessingUnit(servletContext);

        // Run method
        List<String> initialPkgsList = preProcUnit.findPackages(profile);
        
        // Run method and retrieve newly created methods
        List<String> pkgsRetrievedList = preProcUnit.findPackages(profile);
        
        // Check that there have been packages created
        assertTrue(pkgsRetrievedList.size() > initialPkgsList.size() );
        assertTrue(pkgsRetrievedList.size() >= packageNameList.length );
        
        for( String pkg : packageNameList ) { 
            assertTrue( "Package name not retrieved: " + pkg, pkgsRetrievedList.contains(pkg));
        }
    }

}
