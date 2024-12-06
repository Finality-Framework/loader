package team.rainfall.finality.loader;

import org.junit.jupiter.api.Test;
import team.rainfall.finality.FinalityLogger;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @Description ParamParser测试类
 * @Author Greyeon
 * @Date 2024/12/06 20:11
 */

public class ParamParserTest {

    @Test
    public void testParse() {
        ParamParser parser = new ParamParser();
        String[] args = {"-debug", "-disableSteamAPI", "-gamePath", "testGamePath", "-launchMode", "only-gen"};
        parser.parse(args);
        assertTrue(FinalityLogger.isDebug);
        assertTrue(parser.disableSteamAPI);
        assertEquals("testGamePath", parser.gameFilePath);
        assertEquals(LaunchMode.ONLY_GEN, parser.mode);
    }

}