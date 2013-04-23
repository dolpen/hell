package models.enums;

/**
 * 役職情報です
 */
public enum Skill {

    Dummy(0, "ダミー", "ダ"),
    Villager(1, "村人", "村"), // 実装済み
    Augur(2, "占い師", "占"), // 実装済み
    Mystic(3, "霊能者", "霊"),
    Hunter(4, "狩人", "狩"),
    Freemason(5, "共有", "共"),
    Stigmata(6, "聖痕者", "聖"), // 番号管理めどいので当分未実装で行きたい
    Werewolf(7, "人狼", "狼"), // 実装済み
    Insane(8, "狂人", "狂"),
    Fanatic(9, "狂信者", "信"),
    Crazy(10, "Ｃ国狂人", "Ｃ"),
    Hamster(11, "妖魔", "妖"),
    Cupid(12, "キューピッド", "Ｑ"),
    Wooer(13, "求愛者", "求");

    private int value;

    private String label;

    private String sign;

    private Skill(int value, String label, String sign) {
        this.value = value;
        this.label = label;
        this.sign = sign;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }


    public String getSign() {
        return sign;
    }


    // getter

    /**
     * 夜能力を所持しているか(閲覧グループはGroupで管理)
     *
     * @return 夜能力の有無
     */
    public boolean hasAbility() {
        switch (this) {
            case Werewolf:
            case Augur:
            case Hunter:
                return true;
        }
        return false;
    }


    /**
     * 数値から役職を得る
     *
     * @return 役職
     */
    public static Skill by(int value) {
        for (Skill s : values())
            if (s.value == value)
                return s;
        return null;
    }


    /**
     * 文字から役職を得る
     *
     * @return 役職
     */
    public static Skill bySign(String sign) {
        if (sign == null) return null;
        for (Skill s : values())
            if (s.sign.equals(sign))
                return s;
        return null;
    }

    /**
     * 攻撃が成功するか
     *
     * @return 成功するなら<code>true</code>
     */
    public boolean isAttackable() {
        switch (this) {
            case Werewolf:
            case Hamster:
                return false;
        }
        return true;
    }

    /**
     * @return 占い師からの見え方
     */
    public String getAppearance() {
        return this == Werewolf ? "人狼" : "人間";
    }


    /**
     * @return 初期陣営
     */
    public Team getInitialTeam() {
        switch (this) {
            case Werewolf:
            case Insane:
            case Crazy:
            case Fanatic:
                return Team.Wolf;
            case Cupid:
            case Wooer:
                return Team.Lovers;
            case Hamster:
                return Team.Hamster;
            default:
                return Team.Village;
        }
        //return Team.Others;
    }

    /**
     * @return 会話閲覧グループ
     */
    public Group getGroup() {
        switch (this) {
            case Fanatic:
                return Group.Fanatic;
            case Werewolf:
            case Crazy:
                return Group.Wolf;
            case Mystic:
                return Group.Mystic;
        }
        return Group.Dummy;
    }

    /**
     * 秘匿会話ルームを所持しているか(閲覧グループはGroupで管理)
     *
     * @return 秘匿会話能力の有無
     */
    public boolean hasCloset() {
        switch (this) {
            case Werewolf:
            case Crazy:
                return true;
        }
        return false;
    }
}
