package models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import play.db.jpa.GenericModel;

@Entity(name = "character")
public class Character extends GenericModel {

    @Id
    @GeneratedValue
    public Long characterId;

    public String name;

    @OneToMany(cascade = CascadeType.DETACH, mappedBy = "character")
    public List<Member> members;

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
