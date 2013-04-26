package controllers;

import com.google.common.base.Strings;
import models.User;
import models.Village;
import play.mvc.Controller;

import static controllers.Application.getUser;
import static controllers.Application.getUserId;

public class Options extends Controller {

    public static void settle() {
        render();
    }

    public static void update(Long villageId) {
        Village village = Village.findByIdAndAdmin(villageId, getUserId());
        if (village == null)
            Application.index();
        render(village);
    }

    public static void settleVillage(String name, String form, int dayTime, int nightTime, boolean dummy) {
        User user = getUser();
        if (user == null)
            Application.index();
        if (Strings.isNullOrEmpty(name))
            settle();
        // TODO キャラセット固定
        Village.settle(user, name, form, dayTime, nightTime, dummy,1L);
        Application.index();
    }

    public static void updateVillage(Long villageId, String name, String form, int dayTime, int nightTime, boolean dummy) {
        if (Strings.isNullOrEmpty(name))
            update(villageId);
        Village village = Village.findByIdAndAdmin(villageId, getUserId());
        if (village != null)
            village.updateVillage(name, form, dayTime, nightTime, dummy);
        Application.index();
    }

    public static void startVillage(Long villageId) {
        Village village = Village.findByIdAndAdmin(villageId, getUserId());
        if (village != null) {
            village.start();
            Games.index(village.villageId, null);
        }
        Application.index();
    }

}
