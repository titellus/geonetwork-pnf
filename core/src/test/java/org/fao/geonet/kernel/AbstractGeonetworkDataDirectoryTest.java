package org.fao.geonet.kernel;

import jeeves.server.ServiceConfig;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Abstract class for GeonetworkDataDirectory tests where the data directory layout is a default layout but the
 * location of the root data directory is configurable by implementing {@link #getDataDir()}.
 *
 * User: Jesse
 * Date: 11/14/13
 * Time: 9:07 AM
 */
public abstract class AbstractGeonetworkDataDirectoryTest extends AbstractCoreIntegrationTest {
    @Autowired
    private GeonetworkDataDirectory dataDirectory;

    protected abstract Path getDataDir();

    @Test
    public void testInit() throws Exception {
        // make sure it exists
        Files.createDirectories(getDataDir());

        // reinitialize data directory so that it uses the defaults
        dataDirectory.setSystemDataDir(null);
        final ArrayList<Element> serviceConfigParameterElements = getServiceConfigParameterElements();
        final ServiceConfig handlerConfig = new ServiceConfig(serviceConfigParameterElements);
        final Path webappDir = getWebappDir(getClass());
        dataDirectory.init("geonetwork", webappDir, handlerConfig, null);

        assertEquals(getGeonetworkNodeId(), dataDirectory.getNodeId());
        final Path expectedDataDir = getDataDir();
        assertEquals(expectedDataDir, dataDirectory.getSystemDataDir());
        assertEquals(webappDir.toAbsolutePath().normalize(), dataDirectory.getWebappDir().toAbsolutePath().normalize());
        assertSystemDirSubFolders(expectedDataDir);
    }

    private void assertSystemDirSubFolders(Path expectedDataDir) {
        final Path expectedConfigDir = expectedDataDir.resolve("config");
        assertEquals(expectedConfigDir, dataDirectory.getConfigDir());
        assertEquals(expectedDataDir.resolve("index"), dataDirectory.getLuceneDir());
        assertEquals(expectedDataDir.resolve("spatialindex"), dataDirectory.getSpatialIndexPath());
        assertEquals(expectedDataDir.resolve("data").resolve("metadata_data"), dataDirectory.getMetadataDataDir());
        assertEquals(expectedDataDir.resolve("data").resolve("metadata_subversion"), dataDirectory.getMetadataRevisionDir());
        final Path expectedResourcesDir = expectedDataDir.resolve("data").resolve("resources");
        assertEquals(expectedResourcesDir, dataDirectory.getResourcesDir());
        assertEquals(expectedResourcesDir.resolve("htmlcache"), dataDirectory.getHtmlCacheDir());
        assertEquals(expectedConfigDir.resolve("schema_plugins"), dataDirectory.getSchemaPluginsDir());
        assertEquals(expectedConfigDir.resolve("codelist"), dataDirectory.getThesauriDir());
    }
}
