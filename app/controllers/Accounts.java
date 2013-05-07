package controllers;

import consts.CookieName;
import models.User;
import play.db.jpa.NoTransaction;
import play.db.jpa.Transactional;
import play.mvc.Controller;

public class Accounts extends Controller {

    /**
     * ユーザー登録ページ
     */
    @NoTransaction
    public static void index() {
        render();
    }

    /**
     * ユーザー登録
     *
     * @param name 名前
     * @param pass パスワード
     */
    @Transactional
    public static void regist(String name, String pass) {
        if (User.get(name, pass) != null) {
            render();
        }
        User.createNewUser(name, pass);
        login(name, pass);
    }

    /**
     * ログイン
     *
     * @param name 名前
     * @param pass パスワード
     */
    @Transactional(readOnly = true)
    public static void login(String name, String pass) {
        User user = User.get(name, pass);
        if (user != null) {
            session.put(CookieName.USER_ID, String.valueOf(user.userId));
        } else {
            session.remove(CookieName.USER_ID);
        }
        Application.index();
    }

    /**
     * ログアウト
     */
    @NoTransaction
    public static void logout() {
        session.remove(CookieName.USER_ID);
        Application.index();
    }


}
