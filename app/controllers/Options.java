package controllers;

import models.User;
import models.Village;
import models.enums.State;
import play.data.validation.Required;
import play.mvc.Controller;

import static controllers.Application.getUser;
import static controllers.Application.getUserId;

public class Options extends Controller {

    public static void settle() {
        if (getUserId() <= 0L)
            Application.index();
        render();
    }

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

    public static void settleVillage(@Required String name, @Required String form, @Required Integer dayTime, @Required Integer nightTime, @Required Boolean dummy) {
        User user = getUser();
        if (user == null) Application.index();
        if (validation.hasErrors()) settle();
        Village village = Village.settle(user, name, form, dayTime, nightTime, dummy, 1L);
        Games.index(village.villageId, null, null);
    }

    public static void updateVillage(@Required Long villageId, @Required String name, @Required String form, @Required Integer dayTime, @Required Integer nightTime, @Required Boolean dummy) {
        if (validation.hasErrors()) update(villageId);
        Village village = Village.findByIdAndAdmin(villageId, getUserId());
        if (village != null && village.state == State.Prologue)
            village.updateVillage(name, form, dayTime, nightTime, dummy);
        Games.index(villageId, null, null);
    }

    public static void startVillage(@Required Long villageId) {
        if (validation.hasErrors()) Application.index();
        Village village = Village.findByIdAndAdmin(villageId, getUserId());
        if (village == null) Application.index();
        if (village.state == State.Prologue)
            village.start();

        Games.index(village.villageId, null, null);
    }

}
