package de.xab.porter.api.task;

/**
 * global properties of one transmission action
 */
public final class Properties {
  private String channel = "default";

  public String getChannel() { return channel; }

  public void setChannel(String channel) { this.channel = channel; }
}
