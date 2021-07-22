module de.xab.porter.demo {
    requires de.xab.porter.transfer;
    requires de.xab.porter.transfer.jdbc;

    //for reflection
    opens de.xab.porter.demo.reader to de.xab.porter.common;
    opens de.xab.porter.demo.writer to de.xab.porter.common;
}