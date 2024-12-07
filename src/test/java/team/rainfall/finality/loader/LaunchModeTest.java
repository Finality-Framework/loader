package team.rainfall.finality.loader;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @Description LaunchMode测试类
 * @Author Greyeon
 * @Date 2024/12/06 20:06
 */

public class LaunchModeTest {

    @Test
    public void testLaunchModeValues() {
        LaunchMode[] modes = LaunchMode.values();
        assertEquals(3, modes.length);
    }

    @Test
    public void testLaunchModeValueOf() {
        LaunchMode mode = LaunchMode.valueOf("ONLY_LAUNCH");
        assertEquals(LaunchMode.ONLY_LAUNCH, mode);
    }

}
