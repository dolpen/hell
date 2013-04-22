package utils;

import java.util.List;
import java.util.Map;

import models.enums.Skill;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class SkillUtils {
    /**
     * 一行の役職リストをSkillのリストに分割します
     */
    public static List<Skill> getSkillsFromFormStr(String form) {
        if (Strings.isNullOrEmpty(form))
            return Lists.newArrayList();
        System.out.println(form);
        List<Skill> skills = Lists.newArrayList();
        for (char signal : form.replaceAll("[\\n\\r]+", "").toCharArray()) {
            Skill s = Skill.bySign(new String(new char[] { signal }));
            if (s == null){
                System.err.println("err in : "+String.format("%x", Integer.valueOf(signal)));
                throw new IllegalArgumentException("不正な入力");
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
            System.err.println("inv form");
            throw new IllegalArgumentException("不正な入力");
        }

        String[] forms = formSet.split("\n");
        Map<Integer, List<Skill>> skillsMap = Maps.newHashMap();
        for (String form : forms) {
            List<Skill> skills = getSkillsFromFormStr(form);
            if (skills.isEmpty())
                continue;
            if (skillsMap.containsKey(skills.size())){
                System.err.println("dup key");
                throw new IllegalArgumentException("重複する編成");
            }
            skillsMap.put(skills.size(), skills);
        }
        return skillsMap;
    }

    /**
     * 編成表と要求人数から対応する役職編成を取り出す
     * @param formSet 編成表
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