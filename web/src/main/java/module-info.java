module de.xab.porter.web {
    requires de.xab.porter.api;
    requires de.xab.porter.core;

    requires spring.web;
    requires spring.boot;
    requires spring.boot.autoconfigure;

    opens de.xab.porter.web;
    opens de.xab.porter.web.controller;
}