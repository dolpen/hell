package models;

import play.db.jpa.GenericModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * キャラクターセット
 */

@Entity(name = "party")
public class Party extends GenericModel {

    @Id
    @GeneratedValue
    public Long partyId;

    public String name;
    public String dir;

    public static Party makeNewParty(String name, String dir) {
        Party party = new Party();
        party.name = name;
        party.dir = dir;
        return party.save();
    }

    public static Party findById(Long partyId) {
        return find("partyId = ?1", partyId).first();
    }

    public boolean addChara(Long characterId, String name) {
        return Chara.makeNewCharacter(this, characterId, name) != null;
    }

}