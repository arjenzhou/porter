module de.xab.porter.api {
    requires transitive java.sql;

    exports de.xab.porter.api;
    exports de.xab.porter.api.task;
    exports de.xab.porter.api.dataconnection;
    exports de.xab.porter.api.exception;
    exports de.xab.porter.api.annoation;
}