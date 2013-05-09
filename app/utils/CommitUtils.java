package utils;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import consts.Constants;
import models.Member;
import models.Res;
import models.enums.Permission;
import models.enums.Skill;
import models.enums.Team;

import java.util.*;

public class CommitUtils {

    /**
     * 恋人後追いグラフを作ります
     *
     * @param members 生存者
     * @return map
     */
    public static Map<Long, Set<Member>> loversGraph(List<Member> members) {
        Map<Long, Member> memberIdMap = Maps.newHashMap();
        Map<Long, Set<Member>> ret = Maps.newHashMap();
        for (Member m : members) {
            memberIdMap.put(m.memberId, m);
            ret.put(m.memberId, new HashSet<Member>());
        }
        // id -> loversの双方向マップ
        for (Member m : members) {
            if (m.skill == Skill.Cupid && m.targetMemberId2 != null) { // QP・2人指名
                ret.get(m.targetMemberId2).add(memberIdMap.get(m.targetMemberId3));
                ret.get(m.targetMemberId3).add(memberIdMap.get(m.targetMemberId2));
            } else if (m.skill == Skill.Wooer && m.targetMemberId2 != null) { // 求愛・1人指名
                ret.get(m.targetMemberId2).add(memberIdMap.get(m.memberId));
                ret.get(m.memberId).add(memberIdMap.get(m.targetMemberId2));
            }
        }
        return ret;
    }

    /**
     * 対象メンバーで投票した時の、MemberID -> 票数のマッピングを得る
     * 狼の襲撃先選定もこれで行う
     *
     * @param members      {@link Member}
     * @param allMemberIds 全メンバーのID
     * @param nullable     棄権を許すか(ex. 投票はランダムになるが、対象をセットしなかった狼がランダムセットにならない)
     * @return map
     */
    public static Map<Long, Integer> vote(Set<Member> members, Set<Long> allMemberIds, boolean nullable) {
        Map<Long, Integer> votes = Maps.newHashMap();
        Random random = new Random(System.currentTimeMillis());
        for (Member m : members) {
            Long id = m.targetMemberId;
            if (id == -1L) {
                if (nullable) continue;
                id = randomMemberId(allMemberIds, m.memberId, random);
            }
            Integer num = Objects.firstNonNull(votes.get(id), 0);
            votes.put(id, num + 1);
        }
        return votes;
    }


    /**
     * 投票の集計&ランダム処理による当籤者の選定
     *
     * @param allMemberIds 全メンバーのID
     * @param votes        MemberID -> 票数のマッピング
     * @return 当選者のID
     */
    public static Long getElected(Set<Long> allMemberIds, Map<Long, Integer> votes) {
        Set<Long> candidates = Sets.newHashSet(allMemberIds);
        int max = 0;
        for (Long memberId : votes.keySet()) {
            int num = votes.get(memberId);
            if (num > max) {
                candidates.clear();
                max = num;
            }
            if (max > 0 && num == max) {
                candidates.add(memberId);
            }
        }
        return randomMember(candidates, null);
    }

    /**
     * 狼の襲撃先を決めるプロセス
     *
     * @param wolves        狼メンバー
     * @param allMemberIds  生存者全員のID
     * @param dummyMemberId ダミーさんのID
     * @param dayCount      日数
     * @return 襲撃対象のID
     */
    public static Long processAttack(Set<Member> wolves, Set<Long> allMemberIds, Long dummyMemberId, int dayCount) {
        if (dayCount == 1 && dummyMemberId != null) return dummyMemberId;
        Map<Long, Integer> votes = vote(wolves, null, true); // 襲撃投票
        Set<Long> attacker = Sets.newHashSet();
        for (Member wolf : wolves) attacker.add(wolf.memberId);
        return getElected(Sets.difference(allMemberIds, attacker), votes); // 投票結果 or 狼以外からランダム
    }

    /**
     * 役職ランダム(キューピッド)
     * 自分を含む2人(重複不可)に矢を打つ
     *
     * @return ランダムなら<code>true</code>
     */
    public static boolean processCupid(Member m, Map<Long, Member> names, Random random) {
        boolean isRandom = false;
        if (m.targetMemberId2 <= 0L) {
            isRandom = true;
            m.targetMemberId2 = CommitUtils.randomMemberId(names.keySet(), Sets.newHashSet(m.targetMemberId3), random);
        }
        if (m.targetMemberId3 <= 0L) {
            isRandom = true;
            m.targetMemberId3 = CommitUtils.randomMemberId(names.keySet(), Sets.newHashSet(m.targetMemberId2), random);
        }
        Member first = names.get(m.targetMemberId2);
        Member second = names.get(m.targetMemberId3);
        first.team = second.team = Team.Lovers;
        return isRandom;
    }

    /**
     * 役職ランダム(求愛)
     * 自分以外の1人に矢を打つ
     *
     * @return ランダムなら<code>true</code>
     */
    public static boolean processWooer(Member m, Map<Long, Member> names, Random random) {
        boolean isRandom = false;
        if (m.targetMemberId2 <= 0L) {
            isRandom = true;
            m.targetMemberId2 = CommitUtils.randomMemberId(names.keySet(), Sets.newHashSet(m.memberId), random);
        }
        if (m.targetMemberId3 <= 0L) m.targetMemberId3 = m.memberId;
        names.get(m.targetMemberId3).team = Team.Lovers;
        return isRandom;
    }

    /**
     * 役職ランダム
     *
     * @param allMemberIds 探索対象ID
     * @param excludeId    除外対象ID
     * @return ランダムに選ばれたID
     */
    public static Long randomMemberId(Set<Long> allMemberIds, Long excludeId, Random random) {
        if (allMemberIds == null || allMemberIds.isEmpty()) return null;
        Set<Long> excludeIds = Sets.newHashSet();
        excludeIds.add(excludeId);
        return randomMemberId(allMemberIds, excludeIds, random);
    }

    /**
     * 役職ランダム
     *
     * @param allMemberIds 探索対象ID
     * @param excludeIds   除外対象ID
     * @return ランダムに選ばれたID
     */
    public static Long randomMemberId(Set<Long> allMemberIds, Set<Long> excludeIds, Random random) {
        if (allMemberIds == null || allMemberIds.isEmpty()) return null;
        if (excludeIds == null || excludeIds.isEmpty()) excludeIds = Sets.newHashSet();
        return randomMember(Sets.difference(allMemberIds, excludeIds), random);
    }

    /**
     * 実際にMemberに対象を割り振る
     *
     * @param memberIds 選択対象IDの集合
     * @return 選ばれたID
     */
    public static Long randomMember(Set<Long> memberIds, Random random) {
        if (random == null) random = new Random(System.currentTimeMillis());
        return Lists.newArrayList(memberIds).get(random.nextInt(memberIds.size()));
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
