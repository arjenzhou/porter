package de.xab.porter.api.task;

/**
 * global properties of one transmission action
 */
public final class Properties {
    private String channel = "default";
    private String reporter = "default";

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}