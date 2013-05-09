package models.enums;

/**
 * 決着パターンです
 */
public enum EpilogueType {


    Village(0, Team.Village, Team.Village),
    Wolf(1, Team.Wolf, Team.Wolf),
    HamsterV(2, Team.Village, Team.Hamster),
    HamsterW(3, Team.Village, Team.Hamster),
    Lovers(4, Team.Others, Team.Lovers),
    Draw(4, Team.Others, Team.Others);

    private int value;
    private Team trigger;
    private Team winner;

    private EpilogueType(int value, Team trigger, Team winner) {
        this.value = value;
        this.trigger = trigger;
        this.winner = winner;
    }

    public int getValue() {
        return value;
    }

    public Team getTrigger() {
        return trigger;
    }

    public Team getWinner() {
        return winner;
    }


    public static EpilogueType by(int value) {
        for (EpilogueType t : values())
            if (t.value == value)
                return t;
        return null;
    }
}
