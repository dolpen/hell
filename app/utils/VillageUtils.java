package utils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import consts.Constants;
import models.Member;
import models.User;
import models.Village;
import models.enums.Skill;
import models.enums.State;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /**
     * 構成メッセージの生成
     *
     * @return 有効な構成。存在しなければ<code>null</code>
     */
    public static String getSkillFormMessage(Map<Skill, Set<Member>> work) {
        // 内訳発表
        List<String> countMessages = Lists.newArrayList();
        for (Skill s : Skill.values()) {
            if (s == Skill.Dummy) continue;
            int count = work.get(s).size();
            if (s == Skill.Villager) count += work.get(Skill.Dummy).size();
            if (count == 0) continue;
            countMessages.add(s.getLabel() + "が" + count + "人");
        }
        return String.format(Constants.VILLAGE_COUNT, Joiner.on("、").join(countMessages));
    }

    /**
     * メンバー一覧の名前リスト
     *
     * @return 名前一覧
     */
    public static List<String> getNames(Set<Member> work) {
        List<String> names = Lists.newArrayList();
        for (Member m : work)
            names.add(m.name);
        return names;
    }

    /**
     * 投票メッセージの生成
     *
     * @return 投票メッセージ
     */
    public static String getVoteMessage(List<Member> alive, Map<Long, Member> names, Member inmate) {
        List<String> voteMessages = Lists.newArrayList();
        Set<Long> memberIds = names.keySet();
        for (Member m : alive) {
            Long id = m.isCommitable() ? m.targetMemberId : CommitUtils.randomMemberId(memberIds, m.memberId, null);
            voteMessages.add(String.format(Constants.VOTE_ACTION, m.name, names.get(id).name) + (m.isCommitable() ? "" : Constants.RANDOM));
            m.targetMemberId = id;
        }
        return Joiner.on("\n").join(voteMessages) + "\n\n" + String.format(Constants.EXECUTION_ACTION, inmate.name);
    }

    /**
     * 恋人関係をBFSして自殺させる
     *
     * @param members 死者含む
     * @param names   id->memberのマッピング
     * @param dead    今回死んだ人間
     * @return [Member(死ぬ)-Member(死ぬ原因になった相方)]のリスト
     */
    public static List<Member[]> killLovers(List<Member> members, Map<Long, Member> names, Set<Long> dead) {
        Map<Long, Set<Member>> lovers = CommitUtils.loversGraph(members);
        List<Member[]> results = Lists.newArrayList();
        ArrayDeque<Long> chainQueue = new ArrayDeque<Long>();
        Set<Long> visited = Sets.newHashSet(dead);
        chainQueue.addAll(dead);
        while (!chainQueue.isEmpty()) {
            Long id = chainQueue.pollFirst();
            Member from = names.get(id);
            for (Member to : lovers.get(id)) {
                if (visited.contains(to.memberId)) continue;
                results.add(new Member[]{to, from});
                chainQueue.addLast(to.memberId);
                visited.add(to.memberId);
            }
        }
        return results;
    }
}
