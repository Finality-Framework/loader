package team.rainfall.finality.installer;

/**
 * <p>Utility class for converting Steam ID64 to friend code.
 *
 * @author RedreamR
 */
public class SteamIdConverter {

    private static final long USER_ID64_IDENTIFIER = 76561197960265728L;
    private static final long GROUP_ID64_IDENTIFIER = 103582791429521408L;

    /**
     * <p>Converts a Steam ID64 to a friend code.
     *
     * @param id64Str the Steam ID64 as a string
     * @param isGroup true if the ID64 is for a group, false if it is for a user
     * @return the friend code as a string
     * @throws NumberFormatException if the id64Str is not a valid long
     * @author RedreamR
     */
    public static String id64ToFriendCode(String id64Str, boolean isGroup) {
        long id64 = Long.parseLong(id64Str);
        long accountNumber;

        if (isGroup) {
            accountNumber = id64 - GROUP_ID64_IDENTIFIER;
        } else {
            accountNumber = id64 - USER_ID64_IDENTIFIER;
        }

        // 计算id3格式的账号号码部分
        long id3Number = accountNumber / 2;
        boolean lastBitIs1 = (accountNumber % 2) == 1;
        if (lastBitIs1) {
            id3Number += 1;
        }

        // 构造id3格式
        return String.valueOf(id3Number);
    }

}
