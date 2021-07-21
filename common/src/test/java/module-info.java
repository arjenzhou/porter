module de.xab.porter.common.test {
    requires org.junit.jupiter.api;
    requires de.xab.porter.common;

    exports de.xab.porter.common;
    opens de.xab.porter.common.service to de.xab.porter.common;
}