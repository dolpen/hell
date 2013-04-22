package models.enums;

import java.util.EnumSet;

/**
 * 会話グループです
 * 会話グループは、それ自体にIDと、1つ以上の役職の集合を持ちます。
 * 例：狼会話グループ = {狼,C狂}
 * 例：共有会話グループ = {共有}
 * 例：霊能判定グループ = {霊能}
 * ゲーム中は、
 * ・公開された発言およびシステムメッセージ
 * ・所属している会話グループに紐づいた（集合内の全ての役職用の）秘匿会話、およびシステムメッセージ(狼会話、霊能判定など)
 * ・自分に紐づいた非公開メッセージ(独り言、投票、アクティビティなど)
 * のみ表示されます。
 */
public enum Group {
    Dummy(0, EnumSet.of(Skill.Dummy)), // 大体の能力者はこれ
    Wolf(1, EnumSet.of(Skill.Werewolf, Skill.Crazy, Skill.Fanatic)), // このグループに紐づく役職は(狼会話まで見られる)狼とC狂がこれにあたる
    Fanatic(1, EnumSet.of(Skill.Fanatic)), // このグループに紐づく役職は(狼リスト=狂信者用メッセージ)が見られる。狂信者専用
    Mystic(2, EnumSet.of(Skill.Mystic)); // 判定は霊能全員が見えなければならない

    private int value;

    private EnumSet<Skill> skills;

    private Group(int value, EnumSet<Skill> skills) {
        this.value = value;
        this.skills = skills;
    }

    public int getValue() {
        return value;
    }

    public EnumSet<Skill> getSkills() {
        return skills;
    }

    public static Group by(int value) {
        for (Group g : values())
            if (g.value == value)
                return g;
        return null;
    }
}
