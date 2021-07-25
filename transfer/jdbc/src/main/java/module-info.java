module de.xab.porter.transfer.jdbc {
    requires de.xab.porter.transfer;
    requires de.xab.porter.common;
    requires com.zaxxer.hikari;
    requires org.postgresql.jdbc;

    opens de.xab.porter.transfer.jdbc.reader to de.xab.porter.common;
    opens de.xab.porter.transfer.jdbc.writer to de.xab.porter.common;
    exports de.xab.porter.transfer.jdbc.reader;
    exports de.xab.porter.transfer.jdbc.writer;
    exports de.xab.porter.transfer.jdbc.connection;
}