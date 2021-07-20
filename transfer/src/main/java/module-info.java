module de.xab.porter.transfer {
    requires transitive de.xab.porter.api;
    requires de.xab.porter.common;
    requires java.sql;

    exports de.xab.porter.transfer.datasource;
    exports de.xab.porter.transfer.channel;
    exports de.xab.porter.transfer.reader;
    exports de.xab.porter.transfer.writer;
}