package models;

import consts.Constants;
import models.enums.*;
import org.joda.time.DateTime;
import play.Logger;
import play.db.jpa.GenericModel;
import utils.CommitUtils;
import utils.MemberUtils;
import utils.SkillUtils;
import utils.VillageUtils;

import javax.persistence.*;
import java.util.*;

@Entity(name = "village")
public class Village extends GenericModel {

    @Id
    @GeneratedValue
    public Long villageId;
    public Long userId;
    public Long partyId;
    @OneToMany(cascade = CascadeType.DETACH, mappedBy = "village")
    public List<Member> members;
    // 設定
    public String name; //村名
    @Lob
    public String form = Constants.FORM_CATTLE; //編成表
    public int dayTime = 5; // 日中の時間
    public int nightTime = 3; // 夜の時間
    public Long dummyMemberId = null;
    public int maxMember;
    public int minMember;
    // 進行状況
    public State state = State.Prologue;
    public int dayCount = 1;
    public Date nextCommit = null;
    // 結果
    public Team winner = Team.Others; // 勝者不定

    @Version
    public Date updated = null;

    public static Village findById(Long villageId) {
        return find("villageId = ?1", villageId).first();
    }

    public static Village findByIdAndAdmin(Long villageId, Long userId) {
        return find("villageId = ?1 and userId = ?2", villageId, userId).first();
    }

    //public static List<Village> findByState(State state) {
    //    return find("state = ?1", state).fetch();
    //}

    /**
     * 村建て&例のシステムメッセージ
     *
     * @param user      管理者
     * @param name      村名
     * @param form      ワイドカスタム構成
     * @param dayTime   昼間時間
     * @param nightTime 夜時間
     * @param dummy     ダミーを含むかどうか
     * @param partyId   キャラセットID
     * @param time      開始予定時刻
     * @return 立った村
     */
    public static Village settle(User user, String name, String form, int dayTime, int nightTime, boolean dummy, Long partyId, Date time) {
        Village v = new Village();
        v.userId = user.userId;
        v.name = name;
        v.form = form;
        v.dayTime = dayTime;
        v.nightTime = nightTime;
        v.partyId = partyId;
        if (!v.parseOption()) return null;
        if (time != null) {
            if (time.after(new Date(System.currentTimeMillis()))) {
                v.nextCommit = time;
            } else {
                v.nextCommit = null;
            }
        }
        v.save();
        v.merge();
        Res.createNewSystemMessage(v, Permission.Public, Skill.Dummy, Constants.VILLAGE_SETTLE);
        if (!dummy) return v;
        v.enterDummy();
        return v.save();
    }

    /**
     * 村設定変更
     *
     * @param name      村名
     * @param form      ワイドカスタム構成
     * @param dayTime   昼間時間
     * @param nightTime 夜時間
     * @param dummy     ダミーを含むかどうか
     * @param time      開始予定時刻
     * @return 更新された村
     */
    public Village updateVillage(String name, String form, int dayTime, int nightTime, boolean dummy, Date time) {
        this.name = name;
        this.form = form;
        this.dayTime = dayTime;
        this.nightTime = nightTime;
        if (!this.parseOption()) return null;
        // ダミーの入退村
        if (dummy) {
            enterDummy();
        } else {
            leaveDummy();
        }
        if (time != null) {
            if (time.after(new Date(System.currentTimeMillis()))) {
                this.nextCommit = time;
            } else {
                this.nextCommit = null;
            }
        }
        return save();
    }

    /**
     * ダミーの存在確認
     *
     * @return ダミーがいれば<code>true</code>
     */
    public boolean hasDummy() {
        return dummyMemberId != null;
    }

    /**
     * ダミーがいなければ追加し、メンバーIDを返す
     *
     * @return メンバーID
     */
    private Long enterDummy() {
        if (hasDummy()) return dummyMemberId;
        if (!canEnter()) return null;
        Member m = enter(null, 0L);
        if (m == null) return null;
        dummyMemberId = m.memberId;
        return m.memberId;
    }

    /**
     * ダミーの退村
     */
    public void leaveDummy() {
        if (!canLeave() || !hasDummy()) return;
        leave(null);
        dummyMemberId = null;
    }

    /**
     * ワイドカスタム構成のパース
     *
     * @return 成功すれば<code>true</code>
     */
    private boolean parseOption() {
        try {
            Map<Integer, List<Skill>> map = VillageUtils.getSkillsMapFromFormStr(form);
            maxMember = 0;
            minMember = Integer.MAX_VALUE;
            for (Integer n : map.keySet()) {
                maxMember = Math.max(maxMember, n);
                minMember = Math.min(minMember, n);
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }


    /**
     * 村状態コミットのハンドラ
     * 更新時に全員が行動対象を選択していればコミットされる
     * 時間切れなら行動対象を自分以外にして強制的にコミット
     *
     * @return 成功すれば<code>true</code>
     */
    public boolean tryCommit() {
        Logger.info("MEMBERS IS " + (members == null ? 0 : members.size()));
        boolean force = nextCommit != null && nextCommit.before(new Date(System.currentTimeMillis()));
        Logger.info("force : " + force);
        if (state == State.Prologue) {
            if (!force) return false;
            boolean success = start();
            if (!success) {
                nextCommit = null;
                save();
            }
            return success;
        }
        List<Member> members = Member.findByVillage(this);
        if (!force) {
            for (Member m : members) {
                if (m.isAlive() && !m.isCommitable() && (m.hasAbility(dayCount) || state == State.Day)) return false;
            }
        }
        boolean success = false;
        if (state == State.Day) {
            success = commitToNight(members);
            if (!success) Logger.error("commitToDay failed!!!!!!!!!!!!!!!!!!!!!!!!");
        } else if (state == State.Night) {
            success = commitToDay(members);
            if (!success) Logger.error("commitToNight failed!!!!!!!!!!!!!!!!!!!!!!!!");
        } else if (force && state == State.Epilogue) {
            success = toClose();
        }
        return success;
    }

    /**
     * 村開始。対応する編成があれば役職を割り振り、初日夜へ
     *
     * @return 開始できれば<code>true</code>
     */
    public boolean start() {
        List<Res> resList = CommitUtils.start(this, members);
        if (resList == null || !validateAndSave()) return false;
        for (Res r : resList)
            r.create();
        MemberUtils.resetTargets(members);
        return true;
    }

    /**
     * 投票終了時、夜へ
     *
     * @return 成功すれば<code>true</code>
     */
    private boolean commitToNight(List<Member> members) {
        List<Res> resList = CommitUtils.evening(this, members);
        if (resList == null || !validateAndSave()) return false;
        for (Res r : resList)
            r.create();
        MemberUtils.resetTargets(members);
        return true;
    }

    /**
     * 能力発動と夜明け
     *
     * @return 成功すれば<code>true</code>
     */
    private boolean commitToDay(List<Member> members) {
        List<Res> resList = CommitUtils.daybreak(this, members);
        if (resList == null || !validateAndSave()) return false;
        for (Res r : resList)
            r.create();
        MemberUtils.resetTargets(members);
        return true;
    }

    private boolean toClose() {
        state = State.Closed;
        nextCommit = null;
        return save() != null;
    }

    /**
     * 勝利判定
     *
     * @return 決着ついたかどうか
     */
    public boolean endCheck() {
        EpilogueType win = SkillUtils.getWinner(members);
        if (win == null) return false;
        Res.createNewSystemMessage(this, Permission.Public, Skill.Dummy, Constants.WIN_MESSAGE.get(win));
        winner = win.getWinner();
        state = State.Epilogue;
        nextCommit = DateTime.now().plusDays(1).toDate();
        return true;
    }



    // ユーザーの行動

    /**
     * ユーザーの入村
     *
     * @param user        ユーザー
     * @param characterId キャラクターID
     * @return メンバー
     */
    public Member enter(User user, Long characterId) {
        if (!canEnter()) return null;
        Party p = Party.findById(partyId);
        Chara chara = Chara.findById(p, characterId);
        if (chara == null) return null;
        Member m = Member.enter(this, user, chara);
        if (m == null) return null;
        Res.createNewSystemMessage(this, Permission.Public, Skill.Dummy, String.format(Constants.VILLAGE_ENTER, m.name));
        return m;
    }

    /**
     * ユーザーの退村
     *
     * @param user ユーザー
     * @return メンバー
     */
    public Member leave(User user) {
        if (!canLeave()) return null;
        Member m = Member.leave(this, user);
        if (m == null) return null;
        Res.createNewSystemMessage(this, Permission.Public, Skill.Dummy, String.format(Constants.VILLAGE_LEAVE, m.name));
        return m;
    }

    /**
     * ユーザーのキック
     *
     * @param adminId 管理者
     * @return メンバー
     */
    public Member kick(Long adminId, Long memberId) {
        if (!canLeave() || adminId == null || !userId.equals(adminId)) return null;
        Member m = Member.findByIds(this, memberId);
        if (m == null) return null;
        Res.createNewSystemMessage(this, Permission.Public, Skill.Dummy, String.format(Constants.VILLAGE_LEAVE, m.name));
        return m.delete();
    }

    /**
     * ユーザーの存在確認
     *
     * @param user ユーザー
     * @return いれば<code>true</code>
     */
    public boolean exist(User user) {
        return Member.exist(this, user);
    }

    /**
     * 能力設定/投票
     *
     * @param user     ユーザー
     * @param firstId  対象1
     * @param secondId 対象2
     * @return 成功すれば<code>true</code>
     */
    public boolean setTarget(User user, Long firstId, Long secondId) {
        if (user == null || firstId == null || !isRunning()) {
            return false;
        }
        Member me = Member.findByIds(this, user);
        Member first = Member.findByIds(this, firstId);
        if (me == null || first == null || !me.isAlive() || !first.isAlive()) {
            return false;
        }

        // 昼は投票
        if (state == State.Day) {
            me.setTarget(firstId);
            Res.createNewPersonalMessage(this, me, Permission.Personal, Skill.Dummy, String.format(Constants.VOTE_SET, me.name, first.name));
            return me.save() != null;
        }
        // 夜の役職指定系
        if (!me.hasAbility(dayCount)) return false;
        if (me.skill == Skill.Cupid) {// 2人を指定しなければならず、対象を永続化するタイプの役職
            if (secondId == null || secondId.equals(firstId)) return false;
            Member second = Member.findByIds(this, secondId);
            if (second == null || !second.isAlive()) return false;
            me.setTarget(firstId, secondId);
            Res.createNewPersonalMessage(this, me, Permission.Personal, me.skill, String.format(Constants.ACTION_SELECT.get(Skill.Cupid), me.name, first.name, second.name));
        } else if (me.skill == Skill.Wooer) {// 1人を指定しなければならず、対象を永続化するタイプの役職
            me.setTarget(firstId, me.memberId);
            Res.createNewPersonalMessage(this, me, Permission.Personal, me.skill, String.format(Constants.ACTION_SELECT.get(Skill.Wooer), me.name, first.name));
        } else { // その他：だいたい毎晩1人指名系
            me.setTarget(firstId);
            Res.createNewPersonalMessage(this, me, Permission.Personal, me.skill, String.format(Constants.ACTION_SELECT.get(me.skill), me.name, first.name));
        }
        return me.save() != null;
    }


    // プロパティ確認系

    /**
     * 村の管理権限が対象ユーザーにあるかを確認
     *
     * @param user ユーザー
     * @return 管理権限の有無
     */
    public boolean isYours(User user) {
        return user != null && userId.equals(user.userId);
    }

    /**
     * @return 入村可能なら<code>true</code>
     */
    public boolean canEnter() {
        return state == State.Prologue && countMember() < maxMember;
    }

    /**
     * @return 入村者数
     */
    public int countMember() {
        return Member.countByVillage(this);
    }

    /**
     * @return 退村可能なら<code>true</code>
     */
    public boolean canLeave() {
        return state == State.Prologue;
    }

    /**
     * @return 村が進行中であれば<code>true</code>
     */
    public boolean isRunning() {
        return state == State.Day || state == State.Night;
    }

    /**
     * @return 村が決着済みであれば<code>true</code>
     */
    public boolean isFinished() {
        return state == State.Epilogue || state == State.Closed;
    }

    /**
     * (View)次回のコミット日時をUnixTime(ms)で返す
     *
     * @return コミット日時
     */
    public long getCommitTime() {
        if (nextCommit == null) return 0L;
        return nextCommit.getTime();
    }


}
