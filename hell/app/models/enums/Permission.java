package models.enums;

/**
 * 会話やシステムメッセージなど、村ログのそれぞれの構成要素に対して、ゲーム中の公開範囲を設定します。
 * ・公開
 * ・グループ情報
 * ・非公開
 * の3種類です。
 */
public enum Permission {

    Public(0),
    Group(1), // is not Team cf. Werewolf & Fanatic (Except Insane)
    Personal(2),
    Spirit(3);
    private int value;

    private Permission(int value) {
        this.value = value;
    }

    public static Permission by(int value) {
        for (Permission t : values())
            if (t.value == value)
                return t;
        return null;
    }

    public int getValue() {
        return value;
    }
}
