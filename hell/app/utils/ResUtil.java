package utils;

import models.Member;
import models.Res;
import models.Village;

import java.util.List;

/**
 * @author dolpen
 */
public class ResUtil {
    public static List<Res> getRes(Village village, Member me, int day, boolean alive, boolean finished){
        if(finished)return Res.getAllResList(village, day);
        if(me==null)return Res.getPublicResList(village, day);
        if(alive)return Res.getPersonalResList(village, me, day);
        return Res.getDeadPersonalResList(village, me, day);
    }
}