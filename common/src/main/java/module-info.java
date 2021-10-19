module de.xab.porter.common {
    requires java.logging;
    requires transitive de.xab.porter.api;
    requires com.fasterxml.jackson.databind;
    requires okhttp3;
    requires kotlin.stdlib;
    requires okhttp3.logging;

    exports de.xab.porter.common.constant;
    exports de.xab.porter.common.util;
    exports de.xab.porter.common.enums;
    exports de.xab.porter.common.spi;
}