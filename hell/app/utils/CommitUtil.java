package utils;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import models.Member;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class CommitUtil {

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
        for (Member m : members) {
            Long id = m.targetMemberId;
            if (id == -1L) {
                if (nullable) continue;
                id = randomMemberId(allMemberIds, m.memberId);
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
            if (num == max) {
                candidates.add(memberId);
            }
        }
        return randomMember(candidates);
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
     * 役職ランダム
     *
     * @param allMemberIds 探索対象ID
     * @param excludeId    除外対象ID
     * @return ランダムに選ばれたID
     */
    public static Long randomMemberId(Set<Long> allMemberIds, Long excludeId) {
        if (allMemberIds == null || allMemberIds.isEmpty()) return null;
        Set<Long> excludeIds = Sets.newHashSet();
        excludeIds.add(excludeId);
        return randomMemberId(allMemberIds, excludeIds);
    }

    /**
     * 役職ランダム
     *
     * @param allMemberIds 探索対象ID
     * @param excludeIds   除外対象ID
     * @return ランダムに選ばれたID
     */
    public static Long randomMemberId(Set<Long> allMemberIds, Set<Long> excludeIds) {
        if (allMemberIds == null || allMemberIds.isEmpty()) return null;
        if (excludeIds == null || excludeIds.isEmpty()) excludeIds = Sets.newHashSet();
        return randomMember(Sets.difference(allMemberIds, excludeIds));
    }

    /**
     * 実際にMemberに対象を割り振る
     *
     * @param memberIds 選択対象IDの集合
     * @return 選ばれたID
     */
    public static Long randomMember(Set<Long> memberIds) {
        Random random = new Random(System.currentTimeMillis());
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
