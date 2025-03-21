package team.rainfall.finality.loader.util;

/**
 * <p>Utility class for array operations.</p>
 *
 * @author RedreamR
 */
public class ArrayUtil {

    /**
     * <p>Merges two arrays into one.</p>
     *
     * @param array1 the first array
     * @param array2 the second array
     * @return a new array containing all elements of array1 followed by all elements of array2
     * @author RedreamR
     */
    public static String[] mergeArrays(String[] array1, String[] array2) {
        String[] mergedArray = new String[array1.length + array2.length];
        System.arraycopy(array1, 0, mergedArray, 0, array1.length);
        System.arraycopy(array2, 0, mergedArray, array1.length, array2.length);
        return mergedArray;
    }

}
