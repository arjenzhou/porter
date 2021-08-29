module de.xab.porter.transfer {
    requires transitive de.xab.porter.api;
    requires de.xab.porter.common;
    requires java.sql;

    exports de.xab.porter.transfer.connector;
    exports de.xab.porter.transfer.channel;
    exports de.xab.porter.transfer.reporter;
    exports de.xab.porter.transfer.reader;
    exports de.xab.porter.transfer.writer;
    exports de.xab.porter.transfer.exception;

    opens de.xab.porter.transfer.channel to de.xab.porter.common;
    opens de.xab.porter.transfer.reporter to de.xab.porter.common;
}