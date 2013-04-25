package controllers;

import models.Character;
import models.*;
import play.mvc.Controller;

public class Etc extends Controller {


    public static void reset() {
        Village.deleteAll();
        Member.deleteAll();
        Res.deleteAll();

        models.Character.deleteAll();
        Character.makeNewCharacter("アヤメ");
        Character.makeNewCharacter("カレン");
        Character.makeNewCharacter("フラン");
        Character.makeNewCharacter("リディア");
        Character.makeNewCharacter("エリカ");

        User.deleteAll();
    }


}
