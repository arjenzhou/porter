module de.xab.porter.core.test {
    requires de.xab.porter.core;
    requires de.xab.porter.transfer;
    requires de.xab.porter.common;
    requires org.junit.jupiter.api;

    opens de.xab.porter.core.test;
    opens de.xab.porter.core.test.fixture to de.xab.porter.common;
    exports de.xab.porter.core.test;
}
