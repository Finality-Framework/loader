package team.rainfall.finality.loader.plugin;

import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @Description LoaderPluginManager测试类
 * @Author Greyeon
 * @Date 2024/12/06 19:11
 */

public class PluginManagerTest {

    @Test
    public void testFindPlugins() {
        PluginManager pluginManager = new PluginManager();
        // 修改为自己本地任意一个插件的folder路径，不要直接使用原有的路径
        File folder = new File("src/test/plugin/folder");
        pluginManager.findPlugins(folder);
        assertFalse(pluginManager.pluginDataList.isEmpty());
    }

}
