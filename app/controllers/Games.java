package controllers;


import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import consts.Constants;
import consts.CookieName;
import models.*;
import models.enums.State;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Router;
import play.mvc.Util;
import utils.CharacterUtils;
import utils.ResUtils;
import utils.VillageUtils;

import java.util.List;
import java.util.Map;

public class Games extends Controller {

    @Util
    static User tryGetUser() {
        return User.findByIdString(session.get(CookieName.USER_ID));
    }

    @Util
    static Village getVillage(Long villageId) {
        if (villageId == null)
            notFound();
        Village village = Village.findById(villageId);
        notFoundIfNull(village);
        return village;
    }

    @Util
    static User getUser() {
        User user = User.findByIdString(session.get(CookieName.USER_ID));
        notFoundIfNull(user);
        return user;
    }

    @Util
    static Member getMember(Village village, User user) {
        return user == null ? null : Member.findByIds(village, user);
    }


    /**
     * アンカーつきで村ビューに戻す
     *
     * @param villageId 村ID
     */
    @Transactional(readOnly = true)
    private static void redirectToVillage(Long villageId) {
        if (villageId == null) {
            Application.index();
        }
        Map<String, Object> args = Maps.newHashMap();
        args.put("villageId", villageId);
        redirect(Router.getFullUrl("Games.index", args) + "#form");
    }

    /**
     * 村ビュー
     *
     * @param villageId 村ID
     * @param day       日数
     * @param all       ログ全件閲覧フラグ
     */
    @Transactional
    public static void index(Long villageId, Integer day, Boolean all) {
        Village village = getVillage(villageId);
        village.tryCommit();
        User user = tryGetUser();
        if (day == null) day = village.dayCount;
        if (day > village.dayCount) notFound();
        if (all == null) all = false;
        Member me = getMember(village, user);
        List<Member> members = Member.findByVillage(village);
        boolean exist = me != null;
        boolean alive = exist && me.isAlive();
        boolean admin = village.isYours(user);
        boolean closet = exist && me.skill.hasCloset();
        boolean ability = exist && me.skill.hasAbility(village.dayCount);
        boolean finished = village.isFinished();
        boolean now = village.dayCount == day && village.state != State.Closed;
        // ログ
        List<Res> logs = ResUtils.getRes(village, me, day, alive, finished, all ? 0 : Constants.LOG_LIMIT);
        boolean skipped = !all && logs.size() > Constants.LOG_LIMIT;
        if (skipped) logs.remove(0);
        // ここまでログ
        List<Chara> charas = CharacterUtils.getCharacters(village, me);
        render(village, logs, now, day, exist, alive, me, members, charas, admin, closet, ability, finished);
    }

    /**
     * 村詳細ビュー
     *
     * @param villageId 村ID
     */
    @Transactional(readOnly = true)
    public static void detail(Long villageId) {
        Village village = getVillage(villageId);
        render(village);
    }


    /**
     * 夜能力の更新
     *
     * @param villageId 村ID
     * @param firstId   対象1
     * @param secondId  対象2
     */
    @Transactional
    public static void target(Long villageId, Long firstId, Long secondId) {
        User user = getUser();
        Village village = getVillage(villageId);
        village.tryCommit();
        if (VillageUtils.canUseAbility(village)) {
            village.setTarget(user, firstId, secondId);
        }
        redirectToVillage(villageId);
    }

    /**
     * 投票
     *
     * @param villageId 村ID
     * @param firstId   投票先参加者ID
     */
    @Transactional
    public static void vote(Long villageId, Long firstId) {
        User user = getUser();
        Village village = getVillage(villageId);
        village.tryCommit();
        if (VillageUtils.canVote(village)) {
            village.setTarget(user, firstId, null);
        }
        redirectToVillage(villageId);
    }

    /**
     * 公開発言
     *
     * @param villageId 村ID
     * @param text      内容
     */
    @Transactional
    public static void say(Long villageId, String text) {
        if (Strings.isNullOrEmpty(text)) {
            redirectToVillage(villageId);
        }
        Village village = getVillage(villageId);
        village.tryCommit();
        User user = tryGetUser();
        if (VillageUtils.canSay(village, user)) {
            Res.say(village, Member.findByIds(village, user), text);
        }
        redirectToVillage(villageId);
    }

    /**
     * 非公開発言
     *
     * @param villageId 村ID
     * @param text      内容
     */
    @Transactional
    public static void wisper(Long villageId, String text) {
        if (Strings.isNullOrEmpty(text)) {
            redirectToVillage(villageId);
        }
        Village village = getVillage(villageId);
        village.tryCommit();
        User user = tryGetUser();
        if (VillageUtils.canSay(village, user)) {
            Res.wisper(village, Member.findByIds(village, user), text);
        }
        redirectToVillage(villageId);
    }

    /**
     * 霊界発言
     *
     * @param villageId 村ID
     * @param text      内容
     */
    @Transactional
    public static void spirit(Long villageId, String text) {
        if (Strings.isNullOrEmpty(text)) {
            redirectToVillage(villageId);
        }
        Village village = getVillage(villageId);
        village.tryCommit();
        User user = tryGetUser();
        if (VillageUtils.canSay(village, user)) {
            Res.spirit(village, Member.findByIds(village, user), text);
        }
        redirectToVillage(villageId);
    }

    /**
     * 秘密発言
     *
     * @param villageId 村ID
     * @param text      内容
     */
    @Transactional
    public static void closet(Long villageId, String text) {
        if (Strings.isNullOrEmpty(text)) {
            redirectToVillage(villageId);
        }
        Village village = getVillage(villageId);
        village.tryCommit();
        User user = tryGetUser();
        if (VillageUtils.canSay(village, user)) {
            Res.closet(village, Member.findByIds(village, user), text);
        }
        redirectToVillage(villageId);
    }

    /**
     * 入村
     *
     * @param villageId   村ID
     * @param characterId 使用キャラクター
     */
    @Transactional
    public static void enter(Long villageId, Long characterId) {
        User user = getUser();
        Village village = getVillage(villageId);
        village.tryCommit();
        village.enter(user, characterId);
        redirectToVillage(villageId);
    }

    /**
     * 退村
     *
     * @param villageId 村ID
     */
    @Transactional
    public static void leave(Long villageId) {
        User user = getUser();
        Village village = getVillage(villageId);
        village.tryCommit();
        village.leave(user);
        redirectToVillage(villageId);
    }
}
