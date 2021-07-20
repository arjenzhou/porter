package de.xab.porter.api;

import java.util.LinkedList;
import java.util.List;

/**
 * a table's structure and its partial data, equivalent to query ResultSet.
 */
public class Relation {
    List<Column> meta;
    List<List<?>> data;

    public Relation(List<Column> meta) {
        this.meta = meta;
        this.data = new LinkedList<>();
    }

    public List<Column> getMeta() {
        return meta;
    }

    public void setMeta(List<Column> meta) {
        this.meta = meta;
    }

    public List<List<?>> getData() {
        return data;
    }

    public void setData(List<List<?>> data) {
        this.data = data;
    }
}
