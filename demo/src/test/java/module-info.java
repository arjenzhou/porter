module de.xab.porter.demo.test {
    requires org.junit.jupiter.api;
    requires de.xab.porter.api;
    requires de.xab.porter.core;
    requires de.xab.porter.transfer;
    requires java.logging;
    requires java.sql;

    opens de.xab.porter.demo.test;
    exports de.xab.porter.demo.test;
}