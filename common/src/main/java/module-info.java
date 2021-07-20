module de.xab.porter.common {
    requires java.logging;
    requires transitive de.xab.porter.api;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;

    exports de.xab.porter.common.constant;
    exports de.xab.porter.common.util;
    exports de.xab.porter.common.enums;
    exports de.xab.porter.common.spi;
}