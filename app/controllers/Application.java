package controllers;

import com.google.common.base.Objects;
import consts.CookieName;
import models.User;
import models.Village;
import play.mvc.Controller;

import java.util.List;

public class Application extends Controller {

    static User getUser() {
        return User.findByIdString(session.get(CookieName.USER_ID));
    }

    /**
     * 非ログイン時はマイナス1が入りnullにならない
     *
     * @return ユーザID
     */
    static Long getUserId() {
        return Long.valueOf(Objects.firstNonNull(session.get(CookieName.USER_ID), "-1"));
    }

    public static void index() {
        List<Village> villages = Village.all().fetch();
        render(villages);
    }
}