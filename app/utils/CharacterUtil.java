package utils;

import com.google.common.collect.Lists;
import models.Chara;
import models.Member;
import models.Party;
import models.Village;
import models.enums.State;

import java.util.List;

/**
 * @author dolpen
 */
public class CharacterUtil {
    public static List<Chara> getCharacters(Village village, Member me) {
        if (village.state == State.Prologue && me == null)
            return Chara.findAllByParty(Party.findById(village.partyId));
        return Lists.newArrayList();
    }
}