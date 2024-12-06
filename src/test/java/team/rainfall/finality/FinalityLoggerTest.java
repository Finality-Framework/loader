package team.rainfall.finality;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Description FinalityLogger测试类
 * @Author Greyeon
 * @Date 2024/12/06 20:16
 */

public class FinalityLoggerTest {

    @BeforeEach
    public void setUp() {
        Locale.setDefault(Locale.getDefault());
        FinalityLogger.init();
    }

    @Test
    public void testInit() throws IOException {
        File logFile = new File("./loader.log");
        assertTrue(logFile.exists());
        String content = new String(Files.readAllBytes(Paths.get("./loader.log")));
        assertTrue(content.contains("Logger initiated at"));
    }

    @Test
    public void testInfo() throws IOException {
        FinalityLogger.info("Test info message");
        String content = new String(Files.readAllBytes(Paths.get("./loader.log")));
        assertTrue(content.contains("[Info] Test info message"));
    }

    @Test
    public void testError() throws IOException {
        FinalityLogger.error("Test error message");
        String content = new String(Files.readAllBytes(Paths.get("./loader.log")));
        assertTrue(content.contains("[Error] Test error message"));
    }

    @Test
    public void testDebug() throws IOException {
        FinalityLogger.isDebug = true;
        FinalityLogger.debug("Test debug message");
        String content = new String(Files.readAllBytes(Paths.get("./loader.log")));
        assertTrue(content.contains("[Debug] Test debug message"));
    }

    @Test
    public void testWarn() throws IOException {
        FinalityLogger.warn("Test warning message");
        String content = new String(Files.readAllBytes(Paths.get("./loader.log")));
        assertTrue(content.contains("[Warning] Test warning message"));
    }

}
