package models;

import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.List;

@Entity(name = "chara")
public class Chara extends Model {

    public Long characterId;

    @ManyToOne(cascade = CascadeType.DETACH)
    public Party party;

    public String name;

    public boolean isDummy() {
        return characterId <= 0L;
    }

    public static Chara makeNewCharacter(Party party, Long characterId, String name) {
        Chara chara = new Chara();
        chara.characterId = characterId;
        chara.name = name;
        chara.party = party;
        return chara.save();
    }

    public static List<Chara> findAllByParty(Party party) {
        return find("party =?1 and characterId != ?2", party, 0L).fetch();
    }

    public static Chara findById(Party party, Long characterId) {
        return find("party =?1 and characterId = ?2", party, characterId).first();
    }

    public static Chara dummy(Party party) {
        return findById(party, 0L);
    }
}
