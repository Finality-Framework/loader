package team.rainfall.finality.loader;

import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @Description FileManager测试类
 * @Author Greyeon
 * @Date 2024/12/06 20:04
 */

public class FileManagerTest {

    @Test
    public void getSteamWSFolder() {
        File steamWSFolder = FileManager.INSTANCE.getSteamWSFolder();
        assertTrue(steamWSFolder.exists());
    }

    @Test
    public void testGetModsOffFile() throws IOException {
        Files.write(Paths.get("settings/ModsOff.txt"), "mod1;mod2".getBytes());
        String[] modsOff = FileManager.INSTANCE.getModsOffFile();
        assertArrayEquals(new String[]{"mod1", "mod2"}, modsOff);
    }

    @Test
    public void testFindGameFile() {
        String gameFile = FileManager.INSTANCE.findGameFile();
        assertNotNull(gameFile);
    }

}
