package utils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import consts.Constants;
import models.Member;
import models.Res;
import models.Village;
import models.enums.EpilogueType;
import models.enums.Permission;
import models.enums.Skill;
import models.enums.State;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * コミット動作の外出し
 *
 * @author dolpen
 */
public class CommitUtils {
    /**
     * 村の開始処理
     *
     * @param village 村(状態変わるが未セーブ)
     * @param members 死亡者含むメンバー一覧(状態変わるが未セーブ)
     * @return 追加すべきログ一覧(未セーブ)
     */
    public static List<Res> start(Village village, List<Member> members) {
        if (village.state != State.Prologue) return null;
        List<Skill> skills = VillageUtils.getValidSkillSet(village);
        if (skills == null) return null;
        if (!MemberUtils.setSkill(skills, members, village.dummyMemberId)) return null;
        Map<Skill, Set<Member>> work = MemberUtils.skillMembers(members);
        // 聖痕者のナンバリング
        if (!MemberUtils.numberingStigmata(work.get(Skill.Stigmata))) return null;

        List<Res> resList = Lists.newArrayList();
        // 内訳発表
        resList.add(Res.buildSystemMessage(village, Permission.Public, Skill.Dummy, MessageUtils.getSkillFormMessage(work)));
        // 役職決定
        for (Member m : members) {
            if (m.isDummy()) continue;
            resList.add(Res.buildPersonalMessage(village, m, Permission.Personal, m.skill, String.format(Constants.SKILL_SET, m.name, m.getLabel())));
        }
        // 仲間発表：狼(狂信者、C狂、狼に見える)
        resList.add(Res.buildSystemMessage(village, Permission.Group, Skill.Fanatic, String.format(Constants.SKILL_WOLF, Joiner.on("、").join(MessageUtils.getNames(work.get(Skill.Werewolf))))));
        // 仲間発表：共有者
        List<String> freemasons = (MessageUtils.getNames(work.get(Skill.Freemason)));
        if (freemasons.size() == 1) {
            resList.add(Res.buildSystemMessage(village, Permission.Group, Skill.Freemason, String.format(Constants.SKILL_FREEMASON_SINGLE, freemasons.get(0))));
        } else if (!freemasons.isEmpty()) {
            resList.add(Res.buildSystemMessage(village, Permission.Group, Skill.Freemason, String.format(Constants.SKILL_FREEMASON, Joiner.on("、").join(freemasons))));
        }
        // 日暮れ
        village.state = State.Night;
        village.nextCommit = DateTime.now().plusMinutes(village.nightTime).toDate();
        resList.add(Res.buildSystemMessage(village, Permission.Public, Skill.Dummy, Constants.TWILIGHT));
        return resList;
    }

    /**
     * 日暮れ
     *
     * @param village 村(状態変わるが未セーブ)
     * @param members 死亡者含むメンバー一覧(状態変わるが未セーブ)
     * @return 追加すべきログ一覧(未セーブ)
     */
    public static List<Res> evening(Village village, List<Member> members) {

        if (village.state != State.Day) return null;

        // 生存メンバーの振り分け
        List<Member> alive = MemberUtils.filterAlive(members);

        // 生存者から投票の集計
        Map<Long, Member> names = MemberUtils.memberMap(alive); // id -> object
        Set<Long> memberIds = names.keySet();

        // 投票と処刑対象の決定
        Map<Long, Integer> votes = SkillUtils.vote(Sets.newHashSet(alive), memberIds, false); // id -> 票数
        Long inmateId = SkillUtils.decisiveVote(memberIds, votes);
        Member inmate = names.get(inmateId);

        // 処刑メッセージと処刑
        List<Res> resList = Lists.newArrayList();
        resList.add(Res.buildSystemMessage(village, Permission.Public, Skill.Dummy, MessageUtils.getVoteMessage(alive, names, inmate)));
        inmate.execute();

        // 霊メッセージ
        resList.add(Res.buildSystemMessage(village, Permission.Group, Skill.Mystic, String.format(Constants.EXECUTION_MYSTIC, inmate.name, inmate.skill.getAppearance())));
        // 恋人連鎖

        for (Long[] lover : SkillUtils.killLovers(members, Sets.newHashSet(inmateId))) {
            resList.add(Res.buildSystemMessage(village, Permission.Public, Skill.Dummy, String.format(Constants.SUICIDE, names.get(lover[0]).name, names.get(lover[1]).name)));
            names.get(lover[0]).suicide();
        }

        Res fin = checkWinner(village, members);
        if (fin != null) {
            resList.add(fin);
            return resList;
        }
        // 無事なら続行
        village.state = State.Night;
        village.nextCommit = DateTime.now().plusMinutes(village.nightTime).toDate();
        resList.add(Res.buildSystemMessage(village, Permission.Public, Skill.Dummy, Constants.TWILIGHT));
        return resList;
    }

    /**
     * 夜明け
     *
     * @param village 村(状態変わるが未セーブ)
     * @param members 死亡者含むメンバー一覧(状態変わるが未セーブ)
     * @return 追加すべきログ一覧(未セーブ)
     */
    public static List<Res> daybreak(Village village, List<Member> members) {

        if (village.state != State.Night) return null;
        // 生存メンバーの振り分け
        List<Member> alive = MemberUtils.filterAlive(members);
        Map<Skill, Set<Member>> work = MemberUtils.skillMembers(alive);
        Map<Long, Member> names = MemberUtils.memberMap(alive); // id -> object
        Set<Long> memberIds = names.keySet();
        Random random = new Random(System.currentTimeMillis());
        // 夜明け

        List<Res> resList = Lists.newArrayList();
        int yesterday = village.dayCount; // 状態判定用
        village.dayCount++; // ログの表示日時
        village.state = State.Day;
        village.nextCommit = DateTime.now().plusMinutes(village.dayTime).toDate();

        // 恋関連処理
        if (Skill.Cupid.hasAbility(yesterday)) resList.addAll(initLovers(village, members, work, names, memberIds, random));
        Set<Long> horrible = Sets.newHashSet(); // (理由を問わず)無残各位
        // 占い結果
        for (Member m : work.get(Skill.Augur)) {
            boolean isRandom = SkillUtils.processTarget(m, memberIds, random);
            Member target = names.get(m.targetMemberId);
            resList.add(Res.buildPersonalMessage(village, m, Permission.Personal, m.skill, String.format(Constants.FORTUNE_ACTION, target.name, target.skill.getAppearance()) + (isRandom ? Constants.RANDOM : "")));
            if (target.skill == Skill.Hamster)
                horrible.add(m.memberId); // 無残入り
        }
        // 護衛
        Set<Long> guardIds = Sets.newHashSet();
        if (Skill.Hunter.hasAbility(yesterday)) { // 働くのは3日目夜明けより
            for (Member m : work.get(Skill.Hunter)) {
                boolean isRandom = SkillUtils.processTarget(m, memberIds, random);
                Member target = names.get(m.targetMemberId);
                guardIds.add(target.memberId);
                resList.add(Res.buildPersonalMessage(village, m, Permission.Personal, m.skill, String.format(Constants.ACTION_MESSAGE.get(Skill.Hunter), target.name) + (isRandom ? Constants.RANDOM : "")));
            }
        }

        // 狼：襲撃先の選定
        Long victimId = SkillUtils.processAttack(work.get(Skill.Werewolf), names.keySet(), village.dummyMemberId, yesterday);
        Member victim = names.get(victimId);

        // 襲撃
        resList.add(Res.buildSystemMessage(village, Permission.Group, Skill.Werewolf, String.format(Constants.ACTION_MESSAGE.get(Skill.Werewolf), victim.name)));
        if (!guardIds.contains(victimId) && victim.skill.isAttackable()) horrible.add(victimId); // 護衛がない&噛める役なら無残行き

        // 無残メッセージ
        for (Long hid : horrible) {
            Member h = names.get(hid);
            resList.add(Res.buildSystemMessage(village, Permission.Public, Skill.Dummy, String.format(Constants.HORRIBLE, h.name)));
            h.attack();
        }

        // 無残0名
        if (horrible.isEmpty())
            resList.add(Res.buildSystemMessage(village, Permission.Public, Skill.Dummy, Constants.ATTACK_FAILED));

        // 恋人連鎖
        for (Long[] lover : SkillUtils.killLovers(members, horrible)) {
            resList.add(Res.buildSystemMessage(village, Permission.Public, Skill.Dummy, String.format(Constants.SUICIDE, names.get(lover[0]).name, names.get(lover[1]).name)));
            names.get(lover[0]).suicide();
        }
        Res fin = checkWinner(village, members);
        if (fin != null) resList.add(fin);
        return resList;
    }

    /**
     * 勝敗チェック
     *
     * @param village 村(状態変わるが未セーブ)
     * @param members 死亡者含むメンバー一覧(状態変わるが未セーブ)
     * @return 追加すべきログ一覧(未セーブ)
     */
    public static Res checkWinner(Village village, List<Member> members) {
        EpilogueType win = SkillUtils.getWinner(members);
        if (win == null) return null;
        village.winner = win.getWinner();
        village.state = State.Epilogue;
        village.nextCommit = DateTime.now().plusDays(1).toDate();
        return Res.buildSystemMessage(village, Permission.Public, Skill.Dummy, Constants.WIN_MESSAGE.get(win));
    }

    private static List<Res> initLovers(Village village, List<Member> members, Map<Skill, Set<Member>> work, Map<Long, Member> names, Set<Long> memberIds, Random random) {
        List<Res> resList = Lists.newArrayList();
        for (Member m : work.get(Skill.Cupid)) {
            boolean isRandom = SkillUtils.processCupid(m, memberIds, names, random);
            resList.add(Res.buildPersonalMessage(village, m, Permission.Personal, m.skill, String.format(Constants.ACTION_MESSAGE.get(Skill.Cupid), m.name, names.get(m.targetMemberId2).name, names.get(m.targetMemberId3).name) + (isRandom ? Constants.RANDOM : "")));
        }
        for (Member m : work.get(Skill.Wooer)) {
            boolean isRandom = SkillUtils.processWooer(m, memberIds, names, random);
            resList.add(Res.buildPersonalMessage(village, m, Permission.Personal, m.skill, String.format(Constants.ACTION_MESSAGE.get(Skill.Wooer), m.name, names.get(m.targetMemberId2).name) + (isRandom ? Constants.RANDOM : "")));
        }
        Map<Long, Set<Long>> lovers = SkillUtils.loversGraph(members);
        for (Long id : lovers.keySet()) {
            Member m = names.get(id);
            for (Long l : lovers.get(id)) {
                resList.add(Res.buildPersonalMessage(village, m, Permission.Personal, Skill.Cupid, String.format(Constants.FALL_IN_LOVE, m.name, names.get(l).name)));
            }
        }
        return resList;

    }
}