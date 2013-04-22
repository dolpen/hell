package utils;

import com.google.common.collect.Lists;
import models.Character;
import models.Member;
import models.Village;
import models.enums.State;

import java.util.List;

/**
 * @author dolpen
 */
public class CharacterUtil {
    public static List<Character> getCharacters(Village village, Member me) {
        if (village.state == State.Prologue && me == null)
            return Character.all().fetch();
        return Lists.newArrayList();
    }
}