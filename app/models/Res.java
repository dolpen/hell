package models;

import consts.Constants;
import models.enums.Group;
import models.enums.LogType;
import models.enums.Permission;
import models.enums.Skill;
import play.db.jpa.Model;

import javax.persistence.Entity;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

@Entity(name = "res")
public class Res extends Model {

    public Long memberId;
    public Long villageId;
    public String name;
    public String body;
    public int dayCount;
    public Date postDate;
    public Permission permission;
    public Skill skill;
    public LogType logType;

    public static List<Res> getPublicResList(Village village, int dayCount) {
        return find(
                "villageId = ?1 and (permission = ?2 or (permission = ?3 and skill = ?4 and logType = ?5)) and dayCount = ?6 order by postDate asc",
                village.villageId, Permission.Public, Permission.Group, Skill.Werewolf, LogType.Say, dayCount).fetch();
    }

    public static List<Res> getPersonalResList(Village village, Member member, int dayCount) {
        return find(
                "villageId = ?1 and (memberId = ?2 or (permission = ?3 and skill in (?4)) or (permission = ?3 and skill = ?5 and logType = ?6) or permission = ?7) and dayCount = ?8 order by postDate asc",
                village.villageId, member.memberId, Permission.Group,
                member.skill.getGroup().getSkills(), Skill.Werewolf, LogType.Say, Permission.Public, dayCount).fetch();
    }

    public static List<Res> getDeadPersonalResList(Village village, Member member, int dayCount) {
        return find(
                "villageId = ?1 and (memberId = ?2 or (permission = ?3 and skill in (?4)) or (permission = ?3 and skill = ?5 and logType = ?6) or permission in (?7)) and dayCount = ?8 order by postDate asc",
                village.villageId, member.memberId, Permission.Group,
                member.skill.getGroup().getSkills(), Skill.Werewolf, LogType.Say, EnumSet.of(Permission.Public, Permission.Spirit), dayCount).fetch();
    }

    public static List<Res> getAllResList(Village village, int dayCount) {
        return find("villageId = ?1 and dayCount = ?2 order by postDate asc", village.villageId, dayCount).fetch();
    }




    public static boolean createNewRes(Village village, Member member, Permission permission, String body) {
        if (permission == Permission.Group && member.skill.getGroup() == Group.Dummy) permission = Permission.Personal;
        if (permission != Permission.Spirit && !member.isAlive() && !village.isFinished()) return false;
        Res r = new Res();
        r.memberId = member.memberId;
        r.villageId = village.villageId;
        r.name = member.name;
        r.dayCount = village.dayCount;
        r.postDate = new Date();
        r.permission = permission;
        r.skill = member.skill;
        r.body = body;
        r.logType = LogType.Say;
        return r.save() != null;
    }

    public static boolean createNewSystemMessage(Village village, Permission permission, Skill skill, String body) {
        Res r = new Res();
        r.memberId = 0L;
        r.villageId = village.villageId;
        r.name = Constants.SYSTEM_NAME;
        r.dayCount = village.dayCount;
        r.postDate = new Date();
        r.permission = permission;
        r.skill = skill == null ? Skill.Dummy : skill;
        r.body = body;
        r.logType = LogType.System;
        return r.save() != null;
    }

    public static boolean createNewPersonalMessage(Village village, Member member, Permission permission, String body) {
        Res r = new Res();
        r.memberId = member.memberId;
        r.villageId = village.villageId;
        r.name = Constants.SYSTEM_NAME;
        r.dayCount = village.dayCount;
        r.postDate = new Date();
        r.permission = permission;
        r.skill = Skill.Dummy;
        r.body = body;
        r.logType = LogType.System;
        return r.save() != null;
    }
}
