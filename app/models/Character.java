package models;

import play.db.jpa.GenericModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity(name = "character")
public class Character extends GenericModel {

    @Id
    @GeneratedValue
    public Long characterId;

    public String name;

    public static Character makeNewCharacter(String name) {
        Character character = new Character();
        character.name = name;
        return character.save();
    }

    public static Character findById(Long characterId) {
        return find("characterId = ?1", characterId).first();
    }

    public static Character dummy() {
        return findById(1L);
    }
}
