package team.rainfall.finality.loader;

import org.junit.jupiter.api.Test;

import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @Description Main测试类
 * @Author Greyeon
 * @Date 2024/12/06 20:07
 */

public class MainTest {

    @Test
    public void testDeleteDir() {
        File dir = new File("testDir");
        dir.mkdirs();
        boolean result = Main.deleteDir(dir);
        assertTrue(result);
        assertFalse(dir.exists());
    }

}
