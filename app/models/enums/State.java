package models.enums;

/**
 * 村の状態を規定する列挙子です
 */
public enum State {

    Prologue(0, "開始待ち"),
    Night(1, "進行中"),
    Day(2, "進行中"),
    Epilogue(3, "決着"),
    Closed(4, "終了"),
    Deserted(5, "廃村");

    private int value;

    private String label;

    private State(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static State by(int value) {
        for (State s : values())
            if (s.value == value)
                return s;
        return null;
    }
}
