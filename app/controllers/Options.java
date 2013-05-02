package controllers;

import models.User;
import models.Village;
import models.enums.State;
import play.data.validation.Required;
import play.mvc.Controller;

import static controllers.Application.getUser;
import static controllers.Application.getUserId;

public class Options extends Controller {
    /**
     * 村建て画面
     */
    public static void settle() {
        if (getUserId() <= 0L)
            Application.index();
        render();
    }

    /**
     * 村建て
     *
     * @param name      村名
     * @param form      構成
     * @param dayTime   昼の時間
     * @param nightTime 夜の時間
     * @param dummy     ダミー有無
     */
    public static void settleVillage(@Required String name, @Required String form, @Required Integer dayTime, @Required Integer nightTime, @Required Boolean dummy) {
        User user = getUser();
        if (user == null) Application.index();
        if (validation.hasErrors()) settle();
        Village village = Village.settle(user, name, form, dayTime, nightTime, dummy, 1L);
        Games.index(village.villageId, null, null);
    }

    /**
     * 村情報更新画面
     *
     * @param villageId 村ID
     */
    public static void update(@Required Long villageId) {
        if (validation.hasErrors())
            Application.index();
        Village village = Village.findByIdAndAdmin(villageId, getUserId());
        if (village == null)
            Application.index();
        if (village.state != State.Prologue)
            Games.index(village.villageId, null, null);
        render(village);
    }

    /**
     * 村情報の更新
     *
     * @param villageId 村ID
     * @param name      村名
     * @param form      構成
     * @param dayTime   昼の時間
     * @param nightTime 夜の時間
     * @param dummy     ダミー有無
     */
    public static void updateVillage(@Required Long villageId, @Required String name, @Required String form, @Required Integer dayTime, @Required Integer nightTime, @Required Boolean dummy) {
        if (validation.hasErrors()) update(villageId);
        Village village = Village.findByIdAndAdmin(villageId, getUserId());
        if (village != null && village.state == State.Prologue)
            village.updateVillage(name, form, dayTime, nightTime, dummy);
        Games.index(villageId, null, null);
    }

    /**
     * 村の開始
     *
     * @param villageId 村ID
     */
    public static void startVillage(@Required Long villageId) {
        if (validation.hasErrors()) Application.index();
        Village village = Village.findByIdAndAdmin(villageId, getUserId());
        if (village == null) Application.index();
        if (village.state == State.Prologue)
            village.start();
        Games.index(village.villageId, null, null);
    }

    /**
     * 村参加者のキック
     *
     * @param villageId 村ID
     * @param memberId  参加者ID
     */
    public static void kick(@Required Long villageId, @Required Long memberId) {
        if (validation.hasErrors()) Application.index();
        Long userId = getUserId();
        Village village = Village.findByIdAndAdmin(villageId, userId);
        if (village == null) Application.index();
        if (village.state == State.Prologue)
            village.kick(userId, memberId);
        Games.index(village.villageId, null, null);
    }

}
