package team.rainfall.finality.loader.tweaker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import team.rainfall.finality.loader.plugin.PluginData;
import team.rainfall.finality.loader.plugin.PluginManifest;
import team.rainfall.finality.loader.util.FinalityClassLoader;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @Description TweakerManager测试类
 * @Author Greyeon
 * @Date 2024/12/06 19:48
 */

public class TweakerManagerTest {

    private TweakerManager tweakerManager;
    private FinalityClassLoader classLoader;

    @BeforeEach
    public void setUp() {
        tweakerManager = new TweakerManager();
        classLoader = mock(FinalityClassLoader.class);
        tweakerManager.classLoader = classLoader;
    }

    @Test
    public void testAddTweakerFromPlugin() throws Exception {
        PluginManifest manifest = new PluginManifest();
        manifest.hasTweaker = true;
        manifest.tweaker = "team.rainfall.finality.loader.tweaker.Tweaker";
        // 修改为自己本地任意一个插件的路径，不要直接使用原有的路径
        PluginData data = new PluginData(new File("src/test/plugin/testPlugin-1.0-SNAPSHOT-all.jar"));
        data.manifest = manifest;

        Tweaker tweaker = mock(Tweaker.class);
        when(classLoader.loadClass(manifest.tweaker)).thenReturn((Class) tweaker.getClass());

        tweakerManager.addTweakerFromPlugin(data);

        assertFalse(tweakerManager.getTweakers().isEmpty());
    }

    @Test
    public void testTweak() {
        Tweaker tweaker = mock(Tweaker.class);
        tweakerManager.getTweakers().add(tweaker);

        byte[] classBytes = new byte[0];
        tweakerManager.tweak("className", classBytes);

        verify(tweaker).transform("className", classBytes);
    }

}
