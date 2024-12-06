package team.rainfall.finality.loader.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @Description FinalityException测试类
 * @Author Greyeon
 * @Date 2024/12/06 20:00
 */

public class FinalityExceptionTest {

    @Test
    public void testFinalityExceptionMessage() {
        String message = "Test exception message";
        FinalityException exception = new FinalityException(message);
        assertEquals(message, exception.getMessage());
    }

}
