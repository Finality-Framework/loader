

import org.apache.logging.log4j.LogManager;
import java.util.logging.Logger;
import org.junit.platform.suite.api.IncludePackages;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/**
 * @Description 测试总启动类
 * @Author Greyeon
 * @Date 2024/12/06 20:21
 */

@Suite
@SelectPackages({"org", "team"})
@IncludePackages({"org", "team"})
public class AllTests {
    private static final Logger logger = (Logger) LogManager.getLogger(AllTests.class);

    static {
        logger.info("AllTests start.");
    }

}
