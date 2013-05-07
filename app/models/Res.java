package models;

import com.google.common.base.Objects;
import consts.Constants;
import models.enums.LogType;
import models.enums.Permission;
import models.enums.Skill;
import models.enums.State;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

@Entity(name = "res")
public class Res extends Model {

    public Long memberId;
    public Long villageId;
    @ManyToOne(cascade = CascadeType.DETACH)
    public Chara chara;
    public String name;
    public String body;
    public int dayCount;
    public Date postDate;
    public Permission permission;
    public Skill skill;
    public LogType logType;

    /**
     * 非ログイン時ビューを返す
     *
     * @param village  村
     * @param dayCount 日数
     * @return ログ一覧
     */
    public static List<Res> getPublicResList(Village village, int dayCount, int limit) {
        if (limit > 0) return find(
                "villageId = ?1 and (permission = ?2 or (permission = ?3 and skill = ?4 and logType = ?5)) and dayCount = ?6 order by postDate desc",
                village.villageId, Permission.Public, Permission.Group, Skill.Werewolf, LogType.Say, dayCount).fetch(limit);
        return find(
                "villageId = ?1 and (permission = ?2 or (permission = ?3 and skill = ?4 and logType = ?5)) and dayCount = ?6 order by postDate asc",
                village.villageId, Permission.Public, Permission.Group, Skill.Werewolf, LogType.Say, dayCount).fetch();
    }

    /**
     * 生存参加時ビューを返す
     *
     * @param village  村
     * @param member   参加者
     * @param dayCount 日数
     * @return ログ一覧
     */
    public static List<Res> getPersonalResList(Village village, Member member, int dayCount, int limit) {
        if (limit > 0) return find(
                "villageId = ?1 and (memberId = ?2 or (permission = ?3 and skill in (?4)) or (permission = ?3 and skill = ?5 and logType = ?6) or permission = ?7) and dayCount = ?8 order by postDate desc",
                village.villageId, member.memberId, Permission.Group,
                member.skill.getGroup().getSkills(), Skill.Werewolf, LogType.Say, Permission.Public, dayCount).fetch(limit);
        return find(
                "villageId = ?1 and (memberId = ?2 or (permission = ?3 and skill in (?4)) or (permission = ?3 and skill = ?5 and logType = ?6) or permission = ?7) and dayCount = ?8 order by postDate asc",
                village.villageId, member.memberId, Permission.Group,
                member.skill.getGroup().getSkills(), Skill.Werewolf, LogType.Say, Permission.Public, dayCount).fetch();
    }

    /**
     * 霊界参加時ビューを返す
     *
     * @param village  村
     * @param member   参加者
     * @param dayCount 日数
     * @return ログ一覧
     */
    public static List<Res> getDeadPersonalResList(Village village, Member member, int dayCount, int limit) {
        if (limit > 0) return find(
                "villageId = ?1 and (memberId = ?2 or (permission = ?3 and skill in (?4)) or (permission = ?3 and skill = ?5 and logType = ?6) or permission in (?7)) and dayCount = ?8 order by postDate desc",
                village.villageId, member.memberId, Permission.Group,
                member.skill.getGroup().getSkills(), Skill.Werewolf, LogType.Say, EnumSet.of(Permission.Public, Permission.Spirit), dayCount).fetch(limit);
        return find(
                "villageId = ?1 and (memberId = ?2 or (permission = ?3 and skill in (?4)) or (permission = ?3 and skill = ?5 and logType = ?6) or permission in (?7)) and dayCount = ?8 order by postDate asc",
                village.villageId, member.memberId, Permission.Group,
                member.skill.getGroup().getSkills(), Skill.Werewolf, LogType.Say, EnumSet.of(Permission.Public, Permission.Spirit), dayCount).fetch();
    }

    /**
     * 村終了時向けに全ログを返す
     *
     * @param village  村
     * @param dayCount 日数
     * @return ログ一覧
     */
    public static List<Res> getAllResList(Village village, int dayCount, int limit) {
        if (limit > 0)
            return find("villageId = ?1 and dayCount = ?2 order by postDate desc", village.villageId, dayCount).fetch(limit);
        return find("villageId = ?1 and dayCount = ?2 order by postDate asc", village.villageId, dayCount).fetch();
    }

    /**
     * (View)ログのデータをHTMLに吐くときに出すdom class
     * ここにgetterがないとViewから参照できないので、unusedになっていても移動や消去をしない。
     *
     * @return class
     */
    public String getLogClass() {
        String res = (logType == LogType.Say) ? "log_say" : "log_system";
        switch (permission) {
            case Personal:
                res += (logType == LogType.Say) ? " log_wisper" : "";
                break;
            case Group:
                res += " pm_" + skill.name().toLowerCase();
                break;
            case Spirit:
                res += " log_spirit";
                break;
        }
        return res;
    }


    /**
     * 村発言ログの追加
     *
     * @param village    村
     * @param member     発言、閲覧対象の参加者
     * @param permission 公開範囲
     * @param body       本文
     * @return 追加できれば<code>true</code>
     */
    private static boolean createNewRes(Village village, Member member, Permission permission, String body) {
        Res r = new Res();
        r.memberId = member.memberId;
        r.villageId = village.villageId;
        r.name = (village.state == State.Epilogue) ? String.format("%s(%s)", member.name, member.user.name) : member.name;
        r.chara = member.chara;
        r.dayCount = village.dayCount;
        r.postDate = new Date();
        r.permission = permission;
        r.skill = member.skill;
        r.body = body;
        r.logType = LogType.Say;
        return r.save() != null;
    }


    /**
     * 発言
     *
     * @param village 村
     * @param member  発言、閲覧対象の参加者
     * @param body    本文
     * @return 追加できれば<code>true</code>
     */
    public static boolean say(Village village, Member member, String body) {
        if (!member.isAlive() && !village.isFinished()) return false; // 村終了前and死亡者は霊界発言不可
        return createNewRes(village, member, Permission.Public, body);
    }

    /**
     * 独り言
     *
     * @param village 村
     * @param member  発言、閲覧対象の参加者
     * @param body    本文
     * @return 追加できれば<code>true</code>
     */
    public static boolean wisper(Village village, Member member, String body) {
        return createNewRes(village, member, Permission.Personal, body);
    }

    /**
     * 秘密会話
     *
     * @param village 村
     * @param member  発言、閲覧対象の参加者
     * @param body    本文
     * @return 追加できれば<code>true</code>
     */
    public static boolean closet(Village village, Member member, String body) {
        if (village.state != State.Night || !member.isAlive() || !member.skill.hasCloset()) return false;
        return createNewRes(village, member, Permission.Group, body);
    }

    /**
     * 霊界会話
     *
     * @param village 村
     * @param member  発言、閲覧対象の参加者
     * @param body    本文
     * @return 追加できれば<code>true</code>
     */
    public static boolean spirit(Village village, Member member, String body) {
        if (member.isAlive() || village.isFinished()) return false; // 村終了時or生存者は霊界発言不可
        return createNewRes(village, member, Permission.Spirit, body);
    }

    /**
     * システムメッセージの追加
     *
     * @param village    村
     * @param permission 公開範囲
     * @param skill      公開対象役職、無ければダミー(全員が閲覧可能)
     * @param body       本文
     * @return 追加できれば<code>true</code>
     */
    public static boolean createNewSystemMessage(Village village, Permission permission, Skill skill, String body) {
        Res r = new Res();
        r.memberId = 0L;
        r.villageId = village.villageId;
        r.chara = null;
        r.name = Constants.SYSTEM_NAME;
        r.dayCount = village.dayCount;
        r.postDate = new Date();
        r.permission = permission;
        r.skill = skill == null ? Skill.Dummy : skill;
        r.body = body;
        r.logType = LogType.System;
        return r.save() != null;
    }

    /**
     * 個人宛システムメッセージの追加
     *
     * @param village    村
     * @param member     発言、閲覧対象の参加者
     * @param permission 公開範囲
     * @param skill      公開対象役職、無ければダミー(全員が閲覧可能)
     * @param body       本文
     * @return 追加できれば<code>true</code>
     */
    public static boolean createNewPersonalMessage(Village village, Member member, Permission permission, Skill skill, String body) {
        Res r = new Res();
        r.memberId = member.memberId;
        r.villageId = village.villageId;
        r.chara = null;
        r.name = Constants.SYSTEM_NAME;
        r.dayCount = village.dayCount;
        r.postDate = new Date();
        r.permission = permission;
        r.skill = Objects.firstNonNull(skill, member.skill);
        r.body = body;
        r.logType = LogType.System;
        return r.save() != null;
    }
}
