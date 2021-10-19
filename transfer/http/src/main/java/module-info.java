module de.xab.porter.transfer.http {
    requires de.xab.porter.transfer;
    requires de.xab.porter.common;

    requires okhttp3;
    requires kotlin.stdlib;
    requires com.fasterxml.jackson.annotation;

    opens de.xab.porter.transfer.http.connector to de.xab.porter.common;
    opens de.xab.porter.transfer.http.writer to de.xab.porter.common;

    exports de.xab.porter.transfer.http.connector;
    exports de.xab.porter.transfer.http.writer;
}