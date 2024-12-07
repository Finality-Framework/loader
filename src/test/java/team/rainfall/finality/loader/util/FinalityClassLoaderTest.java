package team.rainfall.finality.loader.util;

import org.junit.jupiter.api.Test;
import java.net.URL;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @Description FinalityClassLoader测试类
 * @Author Greyeon
 * @Date 2024/12/06 19:59
 */

public class FinalityClassLoaderTest {

    @Test
    public void testAddUrl2() throws Exception {
        URL url = new URL("file:///path/to/your/file.jar");
        FinalityClassLoader classLoader = new FinalityClassLoader(new URL[0]);
        classLoader.addUrl2(url);
        assertTrue(classLoader.getURLs().length > 0);
    }

    @Test
    public void testDefineClass2() {
        FinalityClassLoader classLoader = new FinalityClassLoader(new URL[0]);
        byte[] classBytes = new byte[0]; // Replace with actual class bytes
        assertThrows(ClassFormatError.class, () -> classLoader.defineClass2("test.ClassName", classBytes, 0, classBytes.length));
    }

}
