package utils;

import models.User;
import models.Village;
import models.enums.Skill;
import models.enums.State;

import java.util.List;
import java.util.Map;

/**
 * 村に関するユーティリティ
 */
public class VillageUtils {
    public static boolean canVote(Village village) {
        return village.state == State.Day;
    }

    public static boolean canUseAbility(Village village) {
        return village.state == State.Night;
    }

    public static boolean canSay(Village village, User user) {
        return village.state != State.Closed && user != null && village.exist(user);
    }

    /**
     * 有効な構成を取得する
     *
     * @return 有効な構成。存在しなければ<code>null</code>
     */
    public static List<Skill> getValidSkillSet(Village village) {
        Map<Integer, List<Skill>> map = SkillUtils.getSkillsMapFromFormStr(village.form);
        return map.get(village.countMember());
    }


}
