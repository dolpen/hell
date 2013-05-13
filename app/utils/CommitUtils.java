package utils;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import models.Member;
import models.enums.EpilogueType;
import models.enums.Skill;
import models.enums.Team;

import java.util.*;

/**
 * コミット時の各役職の動きを定義します
 */
public class CommitUtils {

    // ランダム選択

    /**
     * 役職ランダム
     *
     * @param allMemberIds 探索対象メンバーID集合
     * @param excludeId    除外対象メンバーID
     * @param random       ランダマイザー
     * @return ランダムに選ばれたメンバーID
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
     * @param allMemberIds 探索対象メンバーID集合
     * @param excludeIds   除外対象メンバーID集合
     * @param random       ランダマイザー
     * @return ランダムに選ばれたメンバーID
     */
    public static Long randomMemberId(Set<Long> allMemberIds, Set<Long> excludeIds, Random random) {
        if (allMemberIds == null || allMemberIds.isEmpty()) return null;
        if (excludeIds == null || excludeIds.isEmpty()) excludeIds = Sets.newHashSet();
        return randomMember(Sets.difference(allMemberIds, excludeIds), random);
    }

    /**
     * 実際にMemberに対象を割り振る
     *
     * @param memberIds 選択対象メンバーID集合
     * @param random    ランダマイザー
     * @return 選ばれたメンバーID
     */
    public static Long randomMember(Set<Long> memberIds, Random random) {
        if (random == null) random = new Random(System.currentTimeMillis());
        return Lists.newArrayList(memberIds).get(random.nextInt(memberIds.size()));
    }

    // 昼の投票

    /**
     * 投票する
     * 狼の襲撃先選定もこれで行う
     *
     * @param members      {@link Member}
     * @param allMemberIds 全投票者のメンバーID
     * @param abstention   棄権を許すか(ex. 投票はランダムになるが、対象をセットしなかった狼がランダムセットにならない)
     * @return MemberID -> 票数のマッピングを得る
     */
    public static Map<Long, Integer> vote(Set<Member> members, Set<Long> allMemberIds, boolean abstention) {
        Map<Long, Integer> votes = Maps.newHashMap();
        Random random = new Random(System.currentTimeMillis());
        for (Member m : members) {
            Long id = m.targetMemberId;
            if (id == -1L) {
                if (abstention) continue;
                id = randomMemberId(allMemberIds, m.memberId, random);
            }
            Integer num = Objects.firstNonNull(votes.get(id), 0);
            votes.put(id, num + 1);
        }
        return votes;
    }

    /**
     * 投票結果の集計と、ランダムによる当選者の決定
     *
     * @param allMemberIds 全メンバーのID
     * @param votes        MemberID -> 票数のマッピング
     * @return 当選者のメンバーID
     */
    public static Long decisiveVote(Set<Long> allMemberIds, Map<Long, Integer> votes) {
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

    //夜の役職挙動


    /**
     * 狼の襲撃先を決めるプロセス
     *
     * @param wolves        狼メンバー
     * @param allMemberIds  生存者全員のメンバーID
     * @param dummyMemberId ダミーさんのメンバーID
     * @param dayCount      日数
     * @return 襲撃対象のメンバーID
     */
    public static Long processAttack(Set<Member> wolves, Set<Long> allMemberIds, Long dummyMemberId, int dayCount) {
        if (dayCount == 1 && dummyMemberId != null) return dummyMemberId;
        Map<Long, Integer> votes = vote(wolves, null, true); // 襲撃投票
        Set<Long> attacker = Sets.newHashSet();
        for (Member wolf : wolves) attacker.add(wolf.memberId);
        return decisiveVote(Sets.difference(allMemberIds, attacker), votes); // 投票結果 or 狼以外からランダム
    }

    /**
     * キューピッドのコミット動作(初日夜から2日目の朝まで)
     * 自分を含む2人(重複不可)に矢を打つ
     * ※ modelのメンバ書き換える
     *
     * @param m            メンバー
     * @param allMemberIds 生存者全員のメンバーID
     * @param map          メンバーID→メンバーのマッピング
     * @param random       ランダマイザー
     * @return ランダムなら<code>true</code>
     */
    public static boolean processCupid(Member m, Set<Long> allMemberIds, Map<Long, Member> map, Random random) {
        boolean isRandom = false;
        if (m.targetMemberId2 <= 0L) {
            isRandom = true;
            m.targetMemberId2 = randomMemberId(allMemberIds, Sets.newHashSet(m.targetMemberId3), random);
        }
        if (m.targetMemberId3 <= 0L) {
            isRandom = true;
            m.targetMemberId3 = randomMemberId(allMemberIds, Sets.newHashSet(m.targetMemberId2), random);
        }
        Member first = map.get(m.targetMemberId2);
        Member second = map.get(m.targetMemberId3);
        first.team = second.team = Team.Lovers;
        return isRandom;
    }

    /**
     * 求愛者のコミット動作(初日夜から2日目の朝まで)
     * 自分ともう一人(重複不可)に矢を打つ
     * ※ modelのメンバ書き換える
     *
     * @param m            メンバー
     * @param allMemberIds 生存者全員のメンバーID
     * @param map          メンバーID→メンバーのマッピング
     * @param random       ランダマイザー
     * @return ランダムなら<code>true</code>
     */
    public static boolean processWooer(Member m, Set<Long> allMemberIds, Map<Long, Member> map, Random random) {
        boolean isRandom = false;
        if (m.targetMemberId2 <= 0L) {
            isRandom = true;
            m.targetMemberId2 = randomMemberId(allMemberIds, Sets.newHashSet(m.memberId), random);
        }
        if (m.targetMemberId3 <= 0L) m.targetMemberId3 = m.memberId;
        map.get(m.targetMemberId3).team = Team.Lovers;
        return isRandom;
    }


    /**
     * その他、単一対象を決定する役職
     * ※ modelのメンバ書き換える
     *
     * @param m            メンバー
     * @param allMemberIds 生存者全員のメンバーID
     * @param random       ランダマイザー
     * @return ランダムなら<code>true</code>
     */
    public static boolean processTarget(Member m, Set<Long> allMemberIds, Random random) {
        boolean isRandom = false;
        if (m.targetMemberId <= 0L) {
            isRandom = true;
            m.targetMemberId = randomMemberId(allMemberIds, Sets.newHashSet(m.memberId), random);
        }
        return isRandom;
    }


    // 恋人処理

    /**
     * 恋人後追いグラフを作ります
     *
     * @param members 死亡者を含めた全メンバー
     * @return メンバーID→相方メンバーID一覧のマッピング
     */
    public static Map<Long, Set<Long>> loversGraph(List<Member> members) {
        Map<Long, Member> memberIdMap = Maps.newHashMap();
        Map<Long, Set<Long>> ret = Maps.newHashMap();
        for (Member m : members) {
            memberIdMap.put(m.memberId, m);
            ret.put(m.memberId, new HashSet<Long>());
        }
        // id -> loversの双方向マップ
        for (Member m : members) {
            if (m.skill == Skill.Cupid && m.targetMemberId2 != null) { // QP・2人指名
                ret.get(m.targetMemberId2).add(m.targetMemberId3);
                ret.get(m.targetMemberId3).add(m.targetMemberId2);
            } else if (m.skill == Skill.Wooer && m.targetMemberId2 != null) { // 求愛・1人指名
                ret.get(m.targetMemberId2).add(m.memberId);
                ret.get(m.memberId).add(m.targetMemberId2);
            }
        }
        return ret;
    }

    /**
     * 恋人関係をBFSして自殺させる
     *
     * @param members 死亡者を含めた全メンバー
     * @param dead    今回死んだメンバーのID一覧
     * @return [後追いメンバー-原因(先に死んだ相方)メンバー]のリストを死亡順で
     */
    public static List<Long[]> killLovers(List<Member> members, Set<Long> dead) {
        Map<Long, Set<Long>> lovers = loversGraph(members);
        List<Long[]> results = Lists.newArrayList();
        ArrayDeque<Long> chainQueue = new ArrayDeque<Long>();
        Set<Long> visited = Sets.newHashSet(dead);
        chainQueue.addAll(dead);
        while (!chainQueue.isEmpty()) {
            Long id = chainQueue.pollFirst();
            for (Long to : lovers.get(id)) {
                if (visited.contains(to)) continue;
                results.add(new Long[]{to, id});
                chainQueue.addLast(to);
                visited.add(to);
            }
        }
        return results;
    }

    // 勝敗処理

    /**
     * 勝利判定
     *
     * @param members 死亡者を含めた全メンバー(恋人探索に必要)
     * @return 決着ついたかどうか
     */
    public static EpilogueType getWinner(List<Member> members) {
        int human = 0, wolf = 0;
        boolean hamster = false;
        Team trigger;
        Set<Long> lovers = Sets.newHashSet();
        Set<Long> survivors = Sets.newHashSet();
        // カウントと特殊勝利条件の判定
        for (Member m : members) {
            if (m.skill.getInitialTeam() == Team.Lovers) {
                lovers.add(m.targetMemberId2);
                lovers.add(m.targetMemberId2);
            }
            if (!m.isAlive()) continue;
            survivors.add(m.memberId);
            switch (m.skill) {
                case Hamster:
                    hamster = true;
                    break;
                case Werewolf:
                    wolf++;
                    break;
                default:
                    human++;
            }
        }
        if (wolf + human == 0) {
            return EpilogueType.Draw;
        } else if (wolf == 0) {
            trigger = Team.Village;
        } else if (human <= wolf) {
            trigger = Team.Wolf;
        } else {
            return null;
        }
        // 生き残った恋人(!=陣営)のカウント
        if (!Sets.intersection(lovers, survivors).isEmpty()) {
            return EpilogueType.Lovers;
        } else if (hamster) {
            return trigger == Team.Village ? EpilogueType.HamsterV : EpilogueType.HamsterW;
        }
        return trigger == Team.Village ? EpilogueType.Village : EpilogueType.Wolf;
    }

    // ユーティリティ

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
