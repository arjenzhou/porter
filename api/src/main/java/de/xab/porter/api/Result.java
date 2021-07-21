package de.xab.porter.api;

/**
 * the outer data structure includes real data and its sequence number of batch
 *
 * @param <T> the type of data transferred
 */
public class Result<T> {
    private long sequenceNum;
    private T result;

    public Result(long sequenceNum, T result) {
        this.sequenceNum = sequenceNum;
        this.result = result;
    }

    public long getSequenceNum() {
        return sequenceNum;
    }

    public void setSequenceNum(int sequenceNum) {
        this.sequenceNum = sequenceNum;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
