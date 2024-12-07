package team.rainfall.finality.loader.plugin;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

/**
* @Description LoaderPluginData测试类
* @Author Greyeon
* @Date 2024/12/06 19:03
*/

public class PluginDataTest {

    @Test
    public void testPluginDataInitialization() {
        // 修改为自己本地任意一个插件的路径，不要直接使用原有的路径
        File file = new File("src/test/plugin/testPlugin-1.0-SNAPSHOT-all.jar");
        PluginData pluginData = new PluginData(file);
        assertNotNull(pluginData.jarFile);
        assertNotNull(pluginData.manifest);
    }

    @Test
    public void testBuildDefaultManifestForOldPlugin() {
        String json = "{\"sdkVersion\":2,\"tweakClasses\":[\"class1\",\"class2\"],\"publicizedClasses\":[\"class3\",\"class4\"],\"packageName\":\"testPackage\"}";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        TweakManifest tweakManifest = new TweakManifest(inputStream);
        PluginManifest manifest = PluginData.buildDefaultManifestForOldPlugin(tweakManifest);
        assertEquals("testPackage", manifest.id);
        assertFalse(manifest.hasTweaker);
        assertEquals(0, manifest.version);
        assertTrue(manifest.useLuminosity);
        assertEquals(2, manifest.sdkVersion);
    }

}
