package utils;

import models.Member;
import models.Res;
import models.Village;

import java.util.Collections;
import java.util.List;

/**
 * @author dolpen
 */
public class ResUtil {

    public static List<Res> getRes(Village village, Member me, int day, boolean alive, boolean finished, int limit) {
        List<Res> logs = getResList(village, me, day, alive, finished, limit + 1);
        Collections.reverse(logs);
        return logs;
    }

    public static List<Res> getResList(Village village, Member me, int day, boolean alive, boolean finished, int limit) {
        if (finished) return Res.getAllResList(village, day, limit);
        if (me == null) return Res.getPublicResList(village, day, limit);
        if (alive) return Res.getPersonalResList(village, me, day, limit);
        return Res.getDeadPersonalResList(village, me, day, limit);
    }
}