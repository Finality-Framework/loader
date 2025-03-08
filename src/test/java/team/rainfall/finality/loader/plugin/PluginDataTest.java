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



}
