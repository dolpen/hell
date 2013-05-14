package utils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import consts.Constants;
import models.Member;
import models.enums.Skill;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * システムメッセージの動的生成に関する部分
 */
public class MessageUtils {

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
            Long id = m.isCommitable() ? m.targetMemberId : SkillUtils.randomMemberId(memberIds, m.memberId, null);
            voteMessages.add(String.format(Constants.VOTE_ACTION, m.name, names.get(id).name) + (m.isCommitable() ? "" : Constants.RANDOM));
            m.targetMemberId = id;
        }
        return Joiner.on("\n").join(voteMessages) + "\n\n" + String.format(Constants.EXECUTION_ACTION, inmate.name);
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

}
