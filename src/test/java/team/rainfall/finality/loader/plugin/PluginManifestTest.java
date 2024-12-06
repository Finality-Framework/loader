package team.rainfall.finality.loader.plugin;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @Description LoaderPluginManifest测试类
 * @Author Greyeon
 * @Date 2024/12/06 19:18
 */

public class PluginManifestTest {

    @Test
    public void testPluginManifestInitialization() {
        String json = "{\"sdkVersion\":2,\"version\":1,\"id\":\"testId\",\"useLuminosity\":true,\"hasTweaker\":false,\"tweaker\":\"testTweaker\"}";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        PluginManifest manifest = new PluginManifest(inputStream);
        assertEquals(2, manifest.sdkVersion);
        assertEquals(1, manifest.version);
        assertEquals("testId", manifest.id);
        assertTrue(manifest.useLuminosity);
        assertFalse(manifest.hasTweaker);
        assertEquals("testTweaker", manifest.tweaker);
    }

}
