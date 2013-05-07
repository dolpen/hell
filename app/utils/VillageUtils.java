package utils;

import models.User;
import models.Village;
import models.enums.State;

/**
 * 村に関するユーティリティ
 */
public class VillageUtils {
    public static boolean canVote(Village village){
        return village.state == State.Day;
    }
    public static boolean canUseAbility(Village village){
        return village.state == State.Day;
    }
    public static boolean canSay(Village village, User user){
        return village.state == State.Closed && user != null && village.exist(user);
    }
}
