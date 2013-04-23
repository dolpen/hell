package consts;

import com.google.common.collect.ImmutableMap;
import models.enums.Skill;

import java.util.Map;

public class Constants {
    public static final String SYSTEM_NAME = "天の声";
    public static final String SETTLE_VILLAGE = "人狼、それは人のふりをすることができる伝説の狼。\nその人狼がこの村に紛れ込んでいるという噂がどこからともなく広がりました。\n村人達は半信半疑ながらも、集会所に集まって話し合いをすることにしました。";
    public static final String ENTER_VILLAGE = "%s が集会場にやってきました。";
    public static final String LEAVE_VILLAGE = "%s が立ち去りました。";
    public static final String SET_SKILL = "%s の役職が %s に決定しました。";
    public static final String VOTE_ACTION = "%s は %s に投票します。";
    public static final String VOTE_COUNTS = "%s に、 %d 人投票しました。";
    public static final String VOTE_EXECUTION = "投票の結果、 %s が処刑されました。";
    public static final String VOTE_RESULT = " %s は %s だったようです。";
    public static final String FORTUNE_RESULT = " %s は %s のようです。";
    public static final String GUARD_RESULT = " %s を護衛しています。";
    public static final String BITE_EXECUTION = " %s を襲撃します。";
    public static final String BITE_FAILED = "今日は犠牲者がいないようだ。人狼は襲撃を失敗したのだろうか。";
    public static final String BITE_RESULT = " %s が無残な姿で発見されました。";
    public static final String TWILIGHT = "夜になりました。\n村人達は家に鍵をかけ、夜が明けるのを待っています。";
    public static final String WIN_VILLAGER = "すべての人狼を退治しました。\n多くの犠牲の上に、ついに村に平和が訪れました。";
    public static final String WIN_WOLF = "もう人狼に抵抗できるほど村人は残っていません。\n人狼は残った村人をすべて喰らい尽くし、新たな獲物を求めてこの村を去っていきました。";


    public static final Map<Skill,String> ACTION_MESSAGE = ImmutableMap.<Skill,String>builder()
            .put(Skill.Augur,"%s は %s を占います。")
            .put(Skill.Werewolf,"%s は %s を襲撃します。")
            .put(Skill.Hunter,"%s は %s を護衛します。")
            .build();
    public static final String FORM_CATTLE = "村狼占狂\n"
            + "村村狼占狂\n"
            + "村村村狼占狂\n"
            + "村村村村狼占狂\n"
            + "村村村村狼狼占狩\n"
            + "村村村村村狼狼占狩\n"
            + "村村村村狼狼占霊狂狩\n"
            + "村村村村村狼狼占霊狂狩\n"
            + "村村村村村村狼狼占霊狂狩\n"
            + "村村村村村村村狼狼占霊狂狩\n"
            + "村村村村村村村村狼狼占霊狂狩\n"
            + "村村村村村村村村村狼狼占霊狂狩\n";
           // + "村村村村村村村狼狼狼占霊狂狩共共\n"
           // + "村村村村村村村村狼狼狼占霊狂狩共共\n"
           // + "村村村村村村村村村狼狼狼占霊狂狩共共\n"
           // + "村村村村村村村村村村狼狼狼占霊狂狩共共\n"
           // + "村村村村村村村村村村村狼狼狼占霊狂狩共共\n"
           // + "村村村村村村村村村村村村狼狼狼占霊狂狩共共\n"
           // + "村村村村村村村村村村村村村狼狼狼占霊狂狩共共";

}
