package controllers;


import com.google.common.collect.Maps;
import consts.CookieName;
import models.*;
import models.enums.Permission;
import models.enums.State;
import play.mvc.Controller;
import play.mvc.Router;
import play.mvc.Util;
import utils.CharacterUtil;
import utils.ResUtil;

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
     */
    public static void index(Long villageId, Integer day) {
        Village village = getVillage(villageId);
        village.tryCommit();
        User user = tryGetUser();
        if (day == null) day = village.dayCount;

        Member me = getMember(village, user);
        List<Member> members = Member.findByVillage(village);
        boolean exist = me != null;
        boolean alive = exist && me.isAlive();
        boolean admin = village.isYours(user);
        boolean closet = exist && me.skill.hasCloset();
        boolean ability = exist && me.skill.hasAbility(village.dayCount);
        boolean finished = village.isFinished();
        boolean now = village.dayCount == day && village.state != State.Closed;
        List<Res> logs = ResUtil.getRes(village, me, day, alive, finished);
        List<Chara> charas = CharacterUtil.getCharacters(village, me);
        render(village, logs, now, exist, alive, me, members, charas, admin, closet, ability, finished);
    }

    /**
     * 村詳細ビュー
     *
     * @param villageId 村ID
     */
    public static void detail(Long villageId) {
        Village village = getVillage(villageId);
        render(village);
    }


    // 夜能力の行使
    public static void target(Long villageId, Long firstId, Long secondId) {
        User user = getUser();
        Village village = getVillage(villageId);
        if (village.state == State.Night) {
            village.setTarget(user, firstId, secondId);
        }
        redirectToVillage(villageId);
    }

    // 投票
    public static void vote(Long villageId, Long firstId) {
        User user = getUser();
        Village village = getVillage(villageId);
        if (village.state == State.Day) {
            village.setTarget(user, firstId, null);
        }
        redirectToVillage(villageId);
    }

    public static void say(Long villageId, String text) {
        Village village = getVillage(villageId);
        User user = tryGetUser();
        if (user == null || !village.exist(user) || village.state == State.Closed) {
            redirectToVillage(villageId);
        }
        if (village.state == State.Night) wisper(villageId, text);
        Res.createNewRes(village, Member.findByIds(village, user), Permission.Public, text);
        redirectToVillage(villageId);
    }

    public static void wisper(Long villageId, String text) {
        Village village = getVillage(villageId);
        User user = tryGetUser();
        if (user == null || !village.exist(user) || village.state == State.Closed) {
            redirectToVillage(villageId);
        }
        Res.createNewRes(village, Member.findByIds(village, user), Permission.Personal, text);
        redirectToVillage(villageId);
    }

    public static void spirit(Long villageId, String text) {
        Village village = getVillage(villageId);
        User user = tryGetUser();
        if (user == null || !village.exist(user) || village.state == State.Closed) {
            redirectToVillage(villageId);
        }
        Res.createNewRes(village, Member.findByIds(village, user), Permission.Spirit, text);
        redirectToVillage(villageId);
    }

    public static void closet(Long villageId, String text) {
        Village village = getVillage(villageId);
        User user = getUser();
        if (user == null || !village.exist(user) || village.state == State.Closed) {
            redirectToVillage(villageId);
        }
        if (village.state != State.Night) wisper(villageId, text);
        Res.createNewRes(village, Member.findByIds(village, user), Permission.Group, text);
        redirectToVillage(villageId);
    }

    public static void enter(Long villageId, Long characterId) {
        User user = getUser();
        Village village = getVillage(villageId);
        village.enter(user, characterId);
        redirectToVillage(villageId);
    }

    public static void leave(Long villageId) {
        User user = getUser();
        Village village = getVillage(villageId);
        village.leave(user);
        redirectToVillage(villageId);
    }
}
