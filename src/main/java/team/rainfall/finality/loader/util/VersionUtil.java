package team.rainfall.finality.loader.util;

import org.semver4j.Semver;

/**
 * <p>Utility class for version comparison using semantic versioning.
 * <p>This class provides methods to compare versions to determine if one version is higher, lower, <br>
 * or equal to another version.
 * <p>Note: This class uses the Semver library for parsing and comparing versions.
 *
 * @author RedreamR
 */
@SuppressWarnings("unused")
public class VersionUtil {

    /**
     * <p>Checks if the first version is higher than the second version.
     *
     * @param version1 the first version to compare
     * @param version2 the second version to compare
     * @return true if the first version is higher, false otherwise
     * @author RedreamR
     */
    public static boolean isVersionHigher(String version1, String version2) {
        Semver semver1 = Semver.parse(version1);
        Semver semver2 = Semver.parse(version2);
        if (semver1 != null && semver2 != null) {
            return semver1.isGreaterThan(semver2);
        }
        return false;
    }

    /**
     * <p>Checks if the first version is lower than the second version.
     *
     * @param version1 the first version to compare
     * @param version2 the second version to compare
     * @return true if the first version is lower, false otherwise
     * @author RedreamR
     */
    public static boolean isVersionLower(String version1, String version2) {
        Semver semver1 = Semver.parse(version1);
        Semver semver2 = Semver.parse(version2);
        if (semver1 != null && semver2 != null) {
            return semver1.isLowerThan(semver2);
        }
        return false;
    }

    /**
     * <p>Checks if the first version is equal to the second version.
     *
     * @param version1 the first version to compare
     * @param version2 the second version to compare
     * @return true if the first version is equal, false otherwise
     * @author RedreamR
     */
    public static boolean isVersionEqual(String version1, String version2) {
        Semver semver1 = Semver.parse(version1);
        Semver semver2 = Semver.parse(version2);
        if (semver1 != null && semver2 != null) {
            return semver1.isEqualTo(semver2);
        }
        return false;
    }

}
