package utils;

import com.google.common.collect.Maps;
import models.Member;
import models.enums.Skill;

import java.util.*;

public class MemberUtil {
    /**
     * 役職ごとのメンバー一覧をmapで得る
     *
     * @param members {@link Member}
     * @return map
     */
    public static EnumMap<Skill, Set<Member>> skillMembers(List<Member> members) {
        EnumMap<Skill, Set<Member>> map = Maps.newEnumMap(Skill.class);
        for (Skill sk : Skill.values())
            map.put(sk, new HashSet<Member>());
        for (Member m : members)
            map.get(m.skill).add(m);
        return map;
    }

    /**
     * MemberID -> Memberのマッピングを得る
     *
     * @param members {@link Member}
     * @return map
     */
    public static Map<Long, Member> memberMap(List<Member> members) {
        Map<Long, Member> names = Maps.newHashMap();
        for (Member m : members) names.put(m.memberId, m);
        return names;
    }

    /**
     * 参加者に役職を割り振る
     *
     * @param skills        役職リスト
     * @param members       参加者リスト
     * @param dummyMemberId ダミーメンバーがいればそのID
     * @return 処理が成功すれば<code>true</code>
     */
    public static boolean setSkill(List<Skill> skills, List<Member> members, Long dummyMemberId) {
        if (skills.size() != members.size())
            return false;
        int l = members.size();
        // ダミーの探知と役職の設定

        if (dummyMemberId != null) {
            Skill forDummy = skills.remove(0);
            Member dummy = null;
            for (int i = 0; i < l; i++) {
                Member m = members.get(i);
                if (m.memberId.equals(dummyMemberId)) {
                    dummy = members.remove(i);
                    break;
                }
            }
            if (dummy == null)
                return false;
            dummy.skill = forDummy;
            dummy.save();
        }
        // 役職の割り振り
        l = members.size();
        int[] ind = shuffle(l);
        for (int i = 0; i < l; i++) {
            Member m = members.get(i);
            m.skill = skills.get(ind[i]);
            m.save();
        }
        return true;
    }

    /**
     * 順番シャッフル
     *
     * @param n 人数
     * @return シャッフルされた配列
     */
    private static int[] shuffle(int n) {
        int[] ret = new int[n];
        for (int i = 0; i < n; i++) {
            ret[i] = i;
        }
        for (int i = 0; i < n; i++) {
            int j = Long.valueOf(Math.round(Math.random() * (n - 1))).intValue();
            if (i != j) {
                int t = ret[i];
                ret[i] = ret[j];
                ret[j] = t;
            }
        }
        return ret;
    }
}
