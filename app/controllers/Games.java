package controllers;


import consts.CookieName;
import models.Character;
import models.*;
import models.enums.Permission;
import models.enums.State;
import play.mvc.Controller;
import play.mvc.Util;
import utils.CharacterUtil;
import utils.ResUtil;

import java.util.List;

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
        boolean ability = exist && me.skill.hasAbility();
        boolean finished = village.isFinished();
        List<Res> logs = ResUtil.getRes(village, me, day, alive, finished);
        List<Character> characters = CharacterUtil.getCharacters(village, me);
        render(village, logs, exist, alive, me, members, characters, admin, closet, ability, finished);
    }


    // 夜能力の行使
    public static void target(Long villageId, Long firstId, Long secondId) {
        User user = getUser();
        Village village = getVillage(villageId);
        if (village.state == State.Night) {
            village.setTarget(user, firstId, secondId);
        }
        index(villageId, null);
    }

    // 投票
    public static void vote(Long villageId, Long firstId) {
        User user = getUser();
        Village village = getVillage(villageId);
        if (village.state == State.Day) {
            village.setTarget(user, firstId, null);
        }
        index(villageId, null);
    }

    public static void say(Long villageId, String text) {
        Village village = getVillage(villageId);
        User user = tryGetUser();
        if (user == null || !village.exist(user) || village.state == State.Closed) {
            index(villageId, null);
        }
        if (village.state == State.Night) wisper(villageId, text);
        Res.createNewRes(village, Member.findByIds(village, user), Permission.Public, text);
        index(villageId, null);
    }

    public static void wisper(Long villageId, String text) {
        Village village = getVillage(villageId);
        User user = tryGetUser();
        if (user == null || !village.exist(user) || village.state == State.Closed) {
            index(villageId, null);
        }
        Res.createNewRes(village, Member.findByIds(village, user), Permission.Personal, text);
        index(villageId, null);
    }

    public static void spirit(Long villageId, String text) {
        Village village = getVillage(villageId);
        User user = tryGetUser();
        if (user == null || !village.exist(user) || village.state == State.Closed) {
            index(villageId, null);
        }
        Res.createNewRes(village, Member.findByIds(village, user), Permission.Spirit, text);
        index(villageId, null);
    }

    public static void closet(Long villageId, String text) {
        Village village = getVillage(villageId);
        User user = getUser();
        if (user == null || !village.exist(user) || village.state == State.Closed) {
            index(villageId, null);
        }
        if (village.state != State.Night) wisper(villageId, text);
        Res.createNewRes(village, Member.findByIds(village, user), Permission.Group, text);
        index(villageId, null);
    }

    public static void enter(Long villageId, Long characterId) {
        User user = getUser();
        Village village = getVillage(villageId);
        Character character = Character.findById(characterId);
        if (character == null)
            notFound();
        village.enter(user, character);
        index(villageId, null);
    }

    public static void leave(Long villageId) {
        User user = getUser();
        Village village = getVillage(villageId);
        village.leave(user);
        index(villageId, null);
    }
}
