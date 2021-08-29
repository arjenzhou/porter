package de.xab.porter.transfer.reporter;

public interface Reporter {
    <T> void report(T t);
}