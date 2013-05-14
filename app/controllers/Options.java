package controllers;

import consts.Constants;
import models.User;
import models.Village;
import models.enums.State;
import play.data.binding.As;
import play.data.validation.Required;
import play.db.jpa.NoTransaction;
import play.db.jpa.Transactional;
import play.mvc.Controller;

import java.util.Date;

import static controllers.Application.getUser;
import static controllers.Application.getUserId;

public class Options extends Controller {
    /**
     * 村建て画面
     */
    @NoTransaction
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
    @Transactional
    public static void settleVillage(@Required String name, @Required String form, @Required Integer dayTime, @Required Integer nightTime, @Required Boolean dummy, @As(Constants.DATETIME_PICKER) Date time) {
        User user = getUser();
        if (user == null) Application.index();
        if (validation.hasErrors() && !(validation.errorsMap().keySet().size() == 1 && validation.errorsMap().keySet().contains("time"))) {
            settle();
        }
        Village village = Village.settle(user, name, form, dayTime, nightTime, dummy, 1L, time);
        Games.index(village.villageId, null, null);
    }

    /**
     * 村情報更新画面
     *
     * @param villageId 村ID
     */
    @Transactional(readOnly = true)
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
     * @param time      開始予定時刻
     */
    @Transactional
    public static void updateVillage(@Required Long villageId, @Required String name, @Required String form, @Required Integer dayTime, @Required Integer nightTime, @Required Boolean dummy, @As(Constants.DATETIME_PICKER) Date time) {
        if (validation.hasErrors() && !(validation.errorsMap().keySet().size() == 1 && validation.errorsMap().keySet().contains("time"))) {
            if (villageId != null) update(villageId);
            Application.index();
        }
        Village village = Village.findByIdAndAdmin(villageId, getUserId());
        if (village != null && village.state == State.Prologue)
            village.updateVillage(name, form, dayTime, nightTime, dummy, time);
        Games.index(villageId, null, null);
    }

    /**
     * 村の開始
     *
     * @param villageId 村ID
     */
    @Transactional
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
    @Transactional
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
