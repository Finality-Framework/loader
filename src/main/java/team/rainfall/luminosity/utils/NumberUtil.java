package team.rainfall.luminosity.utils;

public class NumberUtil {
    public static boolean isBitSet(int number, int bitPosition) {
        return (number & (1 << bitPosition)) != 0;
    }
}
