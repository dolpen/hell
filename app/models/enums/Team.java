package models.enums;
/**
 * 陣営の情報です。
 * 勝利条件のもととなるほか、
 * プレイヤーキャラ情報がゲーム中にこっそり書き換わることも…
 */
public enum Team {

    Others(0),
    Village(1),
    Wolf(2),
    Hamster(3),
    Lovers(4);

    private int value;

    private Team(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Team by(int value) {
        for (Team t : values())
            if (t.value == value)
                return t;
        return null;
    }
}
