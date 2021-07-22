package de.xab.porter.common.enums;

/**
 * possible state of {@link de.xab.porter.api.Result#sequenceNum}, except positive number that stands sequence number.
 */
public enum SequenceEnum {
    FIRST(0),
    FIRST_AND_LAST(-1),
    LAST_NOT_EMPTY(-3),
    LAST_IS_EMPTY(-2);

    private int sequenceNum;

    SequenceEnum(int sequenceNum) {
        this.sequenceNum = sequenceNum;
    }

    public static boolean isLast(long sequenceNum) {
        return sequenceNum < FIRST.sequenceNum;
    }

    public static boolean isFirst(long sequenceNum) {
        return sequenceNum == FIRST.sequenceNum || sequenceNum == FIRST_AND_LAST.sequenceNum;
    }

    public int getSequenceNum() {
        return sequenceNum;
    }

    public void setSequenceNum(int sequenceNum) {
        this.sequenceNum = sequenceNum;
    }
}
