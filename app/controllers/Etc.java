package controllers;

import models.Chara;
import models.*;
import play.mvc.Controller;

public class Etc extends Controller {


    public static void reset() {
        Village.deleteAll();
        Member.deleteAll();
        Res.deleteAll();

        Chara.deleteAll();
        Chara.makeNewCharacter("アヤメ");
        Chara.makeNewCharacter("カレン");
        Chara.makeNewCharacter("フラン");
        Chara.makeNewCharacter("リディア");
        Chara.makeNewCharacter("エリカ");

        User.deleteAll();
    }


}
