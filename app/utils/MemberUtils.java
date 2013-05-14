package utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import models.Member;
import models.enums.Skill;

import java.util.*;

public class MemberUtils {
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
     * 参加者に役職、初期チームを割り振る
     *
     * @param skills        役職リスト
     * @param members       参加者リスト
     * @param dummyMemberId ダミーメンバーがいればそのID
     * @return 処理が成功すれば<code>true</code>
     */
    public static boolean setSkill(List<Skill> skills, List<Member> members, Long dummyMemberId) {
        if (skills.size() != members.size())
            return false;

        // ダミーとプレイヤーの振り分け
        List<Member> players = Lists.newArrayList();
        Member dummy = null;
        for (Member m : members) {
            if (m.isDummy() || (dummyMemberId != null && m.memberId.equals(dummyMemberId))) {
                dummy = m;
            } else {
                players.add(m);
            }
        }
        // ダミーへ役職を振る
        if (dummy != null) {
            Skill forDummy = skills.get(0);
            if (forDummy != Skill.Villager) return false;
            dummy.skill = forDummy;
            dummy.team = forDummy.getInitialTeam();
            dummy.save();
        }
        // 役職の割り振り
        int l = players.size();
        int[] ind = shuffle(l);
        for (int i = 0; i < l; i++) {
            Member m = players.get(i);
            m.skill = skills.get(ind[i] + (dummy == null ? 0 : 1));
            m.team = m.skill.getInitialTeam();
            m.save();
        }
        return true;
    }

    /**
     * 聖痕者にナンバリングする
     *
     * @param stigmas 聖痕者
     * @return 成功すれば<code>true</code>
     */
    public static boolean numberingStigmata(Set<Member> stigmas) {
        if (stigmas == null || stigmas.isEmpty()) return true;
        int l = stigmas.size();
        int[] ind = shuffle(l);
        int i = 0;
        for (Member m : stigmas) {
            if (m.skill != Skill.Stigmata) return false;
            m.targetMemberId2 = Integer.valueOf(ind[i] + 1).longValue();
        }
        return true;
    }

    /**
     * 生存者をフィルタリングする
     *
     * @param members 死者含む参加者
     * @return 生存者
     */
    public static List<Member> filterAlive(Iterable<Member> members) {
        List<Member> alive = Lists.newArrayList();
        for (Member m : members)
            if (m.isAlive()) alive.add(m);
        return alive;
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

    /**
     * 能力選択や投票の状態をリセットする
     *
     * @param members メンバー
     */
    public static void resetTargets(List<Member> members) {
        for (Member m : members) {
            m.resetTarget();
        }
    }
}
