module de.xab.porter.transfer.jdbc.test {
    requires de.xab.porter.transfer.jdbc;
    requires de.xab.porter.transfer;
    requires org.junit.jupiter.api;
    requires org.mockito;
    requires java.sql;

    opens de.xab.porter.transfer.jdbc.test;
    exports de.xab.porter.transfer.jdbc.test;
}
