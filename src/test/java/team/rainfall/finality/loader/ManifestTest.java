package team.rainfall.finality.loader;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @Description Manifest测试类
 * @Author Greyeon
 * @Date 2024/12/06 20:10
 */

public class ManifestTest {

    @Test
    public void testManifestInitialization() {
        String json = "{\"is_disable_steamapi\":true,\"debug_mode\":true,\"game_file\":\"game.jar\"}";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        Manifest manifest = new Manifest(inputStream);
        assertTrue(manifest.disableSteamAPI);
        assertTrue(manifest.debugMode);
        assertEquals("game.jar", manifest.gameFile);
    }

}
