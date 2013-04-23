package models.enums;

/**
 * 陣営の情報です。
 * 勝利条件のもととなるほか、
 * プレイヤーキャラ情報がゲーム中にこっそり書き換わることも…
 */
public enum Team {

    Others(0, "その他"),
    Village(1, "村人"),
    Wolf(2, "人狼"),
    Hamster(3, "妖魔"),
    Lovers(4, "恋人");

    private int value;
    private String label;

    private Team(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label + "陣営";
    }

    public static Team by(int value) {
        for (Team t : values())
            if (t.value == value)
                return t;
        return null;
    }
}
