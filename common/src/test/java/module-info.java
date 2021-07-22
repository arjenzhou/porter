module de.xab.porter.common.test {
    requires org.junit.jupiter.api;
    requires com.fasterxml.jackson.core;
    requires de.xab.porter.common;

    exports de.xab.porter.common.test.json;
    exports de.xab.porter.common.test.spi;
    exports de.xab.porter.common.test.string;

    opens de.xab.porter.common.test.spi.impl to de.xab.porter.common;
}