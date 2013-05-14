package controllers;

import com.google.common.base.Objects;
import consts.CookieName;
import models.User;
import models.Village;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Util;

import java.util.List;

public class Application extends Controller {
    @Util
    static User getUser() {
        return User.findByIdString(session.get(CookieName.USER_ID));
    }

    /**
     * 非ログイン時はマイナス1が入りnullにならない
     *
     * @return ユーザID
     */
    @Util
    static Long getUserId() {
        return Long.valueOf(Objects.firstNonNull(session.get(CookieName.USER_ID), "-1"));
    }

    /**
     * トップページ
     */
    @Transactional(readOnly = true)
    public static void index() {
        List<Village> villages = Village.all().fetch();
        render(villages);
    }
}
