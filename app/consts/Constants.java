package consts;

import com.google.common.collect.ImmutableMap;
import models.enums.Skill;

import java.util.Map;

public class Constants {

    public static final String DATETIME_PICKER = "yyyy/MM/dd HH:mm:ss";
    public static final String SYSTEM_NAME = "天の声";
    public static final String VILLAGE_SETTLE = "人狼、それは人のふりをすることができる伝説の狼。\nその人狼がこの村に紛れ込んでいるという噂がどこからともなく広がりました。\n村人達は半信半疑ながらも、集会所に集まって話し合いをすることにしました。";
    public static final String VILLAGE_ENTER = "%s が集会場にやってきました。";
    public static final String VILLAGE_LEAVE = "%s が立ち去りました。";
    public static final String VILLAGE_COUNT = "この村には%sいるようです。";
    public static final String SKILL_SET = "%s の役職が %s に決定しました。";
    public static final String SKILL_WOLF = "この村に潜む人狼は、%sです。";
    public static final String SKILL_FREEMASON = "%sは心を共有しています。";
    public static final String SKILL_FREEMASON_SINGLE = "%s は凄い共有者です。凄すぎて相方はいません。";
    public static final String VOTE_SET = "%s は %s に投票します。";
    public static final String VOTE_ACTION = "%s は %s に投票しました。";
    //public static final String VOTE_COUNTS = "%s に、 %d 人投票しました。";
    public static final String EXECUTION_ACTION = "投票の結果、 %s が処刑されました。";
    public static final String EXECUTION_MYSTIC = " %s は %s だったようです。";
    public static final String FORTUNE_ACTION = " %s は %s のようです。";
    public static final String GUARD_ACTION = " %s を護衛しています。";
    public static final String ATTACK_SET = " %s を襲撃します。";
    public static final String ATTACK_FAILED = "今日は犠牲者がいないようだ。人狼は襲撃を失敗したのだろうか。";
    public static final String HORRIBLE = " %s が無残な姿で発見されました。";
    public static final String SUICIDE = " %s は %s の後を追って崖から身を投げました。";
    public static final String RANDOM = "（ランダム）";
    public static final String TWILIGHT = "夜になりました。\n村人達は家に鍵をかけ、夜が明けるのを待っています。";
    public static final String NO_WINNER = "そして誰もいなくなった。";
    public static final String WIN_VILLAGER = "すべての人狼を退治しました。\n多くの犠牲の上に、ついに村に平和が訪れました。";
    public static final String WIN_WOLF = "もう人狼に抵抗できるほど村人は残っていません。\n人狼は残った村人をすべて喰らい尽くし、新たな獲物を求めてこの村を去っていきました。";
    public static final String WIN_HAMSTER_V = "すべての人狼を退治しました。\n多くの犠牲を重ね、ついに村に平和が訪れたかのように見えました。\nしかし、村にはまだ妖魔がひっそりと生き残っていました。";
    public static final String WIN_HAMSTER_W = "もう人狼に抵抗できるほど村人は残っていません。\n生き残った村人もすべて人狼に襲われてしまいました。\nしかし、その人狼もまた村に潜んでいた妖魔によって滅ぼされました。";
    public static final String WIN_LOVERS = "愛の前ではすべてのものが無力でした。";

    public static final Map<Skill, String> ACTION_MESSAGE = ImmutableMap.<Skill, String>builder()
            .put(Skill.Augur, "%s は %s を占います。")
            .put(Skill.Werewolf, "%s は %s を襲撃します。")
            .put(Skill.Hunter, "%s は %s を護衛します。")
            .put(Skill.Wooer, "%s は %s に求愛します。")
            .put(Skill.Cupid, "%s は %s と %s に愛の矢を撃ちます。")
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
            + "村村村村村村村村村狼狼占霊狂狩\n"
            + "村村村村村村村狼狼狼占霊狂狩共共\n"
            + "村村村村村村村村狼狼狼占霊狂狩共共\n"
            + "村村村村村村村村村狼狼狼占霊狂狩共共\n"
            + "村村村村村村村村村村狼狼狼占霊狂狩共共\n"
            + "村村村村村村村村村村村狼狼狼占霊狂狩共共\n"
            + "村村村村村村村村村村村村狼狼狼占霊狂狩共共\n"
            + "村村村村村村村村村村村村村狼狼狼占霊狂狩共共";
}
