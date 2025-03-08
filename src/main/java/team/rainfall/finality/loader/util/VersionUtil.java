package team.rainfall.finality.loader.util;

import org.semver4j.Semver;

@SuppressWarnings("unused")
public class VersionUtil {
    public static boolean isVersionHigher(String version1, String version2) {
        Semver semver1 = Semver.parse(version1);
        Semver semver2 = Semver.parse(version2);
        if (semver1 != null && semver2 != null) {
            return semver1.isGreaterThan(semver2);
        }
        return false;
    }
    public static boolean isVersionLower(String version1, String version2) {
        Semver semver1 = Semver.parse(version1);
        Semver semver2 = Semver.parse(version2);
        if (semver1 != null && semver2 != null) {
            return semver1.isLowerThan(semver2);
        }
        return false;
    }
    public static boolean isVersionEqual(String version1, String version2) {
        Semver semver1 = Semver.parse(version1);
        Semver semver2 = Semver.parse(version2);
        if (semver1 != null && semver2 != null) {
            return semver1.isEqualTo(semver2);
        }
        return false;
    }

}
