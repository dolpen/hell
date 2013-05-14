package utils;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
        Map<Integer, List<Skill>> map = getSkillsMapFromFormStr(village.form);
        return map.get(village.countMember());
    }

    /**
     * 一行の役職リストをSkillのリストに分割します
     */
    public static List<Skill> getSkillsFromFormStr(String form) {
        if (Strings.isNullOrEmpty(form))
            return Lists.newArrayList();
        List<Skill> skills = Lists.newArrayList();
        for (char signal : form.replaceAll("[\\n\\r]+", "").toCharArray()) {
            Skill s = Skill.bySign(new String(new char[]{signal}));
            if (s == null) {
                throw new IllegalArgumentException("不正な入力");
            } else if (!s.isImplimented()) {
                throw new IllegalArgumentException("未実装役職を使用しようとしました");
            }
            skills.add(s);
        }
        return skills;
    }

    /**
     * 編成表をSkillのリストと人数とのマッピングに分割します
     */
    public static Map<Integer, List<Skill>> getSkillsMapFromFormStr(String formSet) {
        if (Strings.isNullOrEmpty(formSet)) {
            throw new IllegalArgumentException("入力がありません");
        }
        String[] forms = formSet.split("\n");
        Map<Integer, List<Skill>> skillsMap = Maps.newHashMap();
        for (String form : forms) {
            List<Skill> skills = getSkillsFromFormStr(form);
            if (skills.isEmpty())
                continue;
            if (skillsMap.containsKey(skills.size())) {
                throw new IllegalArgumentException("重複する編成です");
            }
            skillsMap.put(skills.size(), skills);
        }
        return skillsMap;
    }

    /**
     * 編成表と要求人数から対応する役職編成を取り出す
     *
     * @param formSet     編成表
     * @param memberCount 人数
     * @return 編成表が正常かつ、対応するものがあれば返す。
     */
    public static List<Skill> getSkillListFromMapAndKey(String formSet, int memberCount) {
        try {
            Map<Integer, List<Skill>> map = getSkillsMapFromFormStr(formSet);
            if (map.containsKey(memberCount))
                return map.get(memberCount);
        } catch (IllegalArgumentException e) {
            return null;
        }
        return null;
    }
}
