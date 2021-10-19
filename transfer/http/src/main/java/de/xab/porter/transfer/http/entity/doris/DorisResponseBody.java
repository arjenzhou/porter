
package de.xab.porter.transfer.http.entity.doris;

import com.fasterxml.jackson.annotation.JsonAlias;

public class DorisResponseBody {
    @JsonAlias("BeginTxnTimeMs")
    private Long beginTxnTimeMs;
    @JsonAlias("CommitAndPublishTimeMs")
    private Long commitAndPublishTimeMs;
    @JsonAlias("ErrorURL")
    private String errorURL;
    @JsonAlias("ExistingJobStatus")
    private String existingJobStatus;
    @JsonAlias("Label")
    private String label;
    @JsonAlias("LoadBytes")
    private Long loadBytes;
    @JsonAlias("LoadTimeMs")
    private Long loadTimeMs;
    @JsonAlias("Message")
    private String message;
    @JsonAlias("NumberFilteredRows")
    private Long numberFilteredRows;
    @JsonAlias("NumberLoadedRows")
    private Long numberLoadedRows;
    @JsonAlias("NumberTotalRows")
    private Long numberTotalRows;
    @JsonAlias("NumberUnselectedRows")
    private Long numberUnselectedRows;
    @JsonAlias("ReadDataTimeMs")
    private Long readDataTimeMs;
    @JsonAlias("Status")
    private String status;
    @JsonAlias("StreamLoadPutTimeMs")
    private Long streamLoadPutTimeMs;
    @JsonAlias("TxnId")
    private Long txnId;
    @JsonAlias("WriteDataTimeMs")
    private Long writeDataTimeMs;

    public Long getBeginTxnTimeMs() {
        return beginTxnTimeMs;
    }

    public void setBeginTxnTimeMs(Long beginTxnTimeMs) {
        this.beginTxnTimeMs = beginTxnTimeMs;
    }

    public Long getCommitAndPublishTimeMs() {
        return commitAndPublishTimeMs;
    }

    public void setCommitAndPublishTimeMs(Long commitAndPublishTimeMs) {
        this.commitAndPublishTimeMs = commitAndPublishTimeMs;
    }

    public String getErrorURL() {
        return errorURL;
    }

    public void setErrorURL(String errorURL) {
        this.errorURL = errorURL;
    }

    public String getExistingJobStatus() {
        return existingJobStatus;
    }

    public void setExistingJobStatus(String existingJobStatus) {
        this.existingJobStatus = existingJobStatus;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getLoadBytes() {
        return loadBytes;
    }

    public void setLoadBytes(Long loadBytes) {
        this.loadBytes = loadBytes;
    }

    public Long getLoadTimeMs() {
        return loadTimeMs;
    }

    public void setLoadTimeMs(Long loadTimeMs) {
        this.loadTimeMs = loadTimeMs;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getNumberFilteredRows() {
        return numberFilteredRows;
    }

    public void setNumberFilteredRows(Long numberFilteredRows) {
        this.numberFilteredRows = numberFilteredRows;
    }

    public Long getNumberLoadedRows() {
        return numberLoadedRows;
    }

    public void setNumberLoadedRows(Long numberLoadedRows) {
        this.numberLoadedRows = numberLoadedRows;
    }

    public Long getNumberTotalRows() {
        return numberTotalRows;
    }

    public void setNumberTotalRows(Long numberTotalRows) {
        this.numberTotalRows = numberTotalRows;
    }

    public Long getNumberUnselectedRows() {
        return numberUnselectedRows;
    }

    public void setNumberUnselectedRows(Long numberUnselectedRows) {
        this.numberUnselectedRows = numberUnselectedRows;
    }

    public Long getReadDataTimeMs() {
        return readDataTimeMs;
    }

    public void setReadDataTimeMs(Long readDataTimeMs) {
        this.readDataTimeMs = readDataTimeMs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getStreamLoadPutTimeMs() {
        return streamLoadPutTimeMs;
    }

    public void setStreamLoadPutTimeMs(Long streamLoadPutTimeMs) {
        this.streamLoadPutTimeMs = streamLoadPutTimeMs;
    }

    public Long getTxnId() {
        return txnId;
    }

    public void setTxnId(Long txnId) {
        this.txnId = txnId;
    }

    public Long getWriteDataTimeMs() {
        return writeDataTimeMs;
    }

    public void setWriteDataTimeMs(Long writeDataTimeMs) {
        this.writeDataTimeMs = writeDataTimeMs;
    }
}
