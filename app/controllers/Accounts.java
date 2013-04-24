package controllers;

import consts.CookieName;
import models.User;
import play.mvc.Controller;

public class Accounts extends Controller {

    public static void index() {
        render();
    }

    public static void login(String name, String pass) {
        User user = User.get(name, pass);
        if (user != null) {
            session.put(CookieName.USER_ID, String.valueOf(user.userId));
        } else {
            session.remove(CookieName.USER_ID);
        }
        Application.index();
    }

    public static void logout() {
        session.remove(CookieName.USER_ID);
        Application.index();
    }

    public static void regist(String name, String pass) {
        if (User.get(name, pass) != null) {
            render();
        }
        User.createNewUser(name, pass);
        login(name, pass);
    }

}
