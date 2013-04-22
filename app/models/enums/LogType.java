package models.enums;

public enum LogType {
    Other(0),
    Say(1),
    System(2);

    private int value;

    private LogType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static LogType by(int value) {
        for (LogType t : values())
            if (t.value == value)
                return t;
        return null;
    }
}
