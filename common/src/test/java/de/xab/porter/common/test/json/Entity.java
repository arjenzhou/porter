package de.xab.porter.common.test.json;

import java.util.List;

/**
 * class for JSON test
 */
public class Entity {
    private List<String> field1;
    private String field2;
    private int field3;

    public List<String> getField1() {
        return field1;
    }

    public void setField1(List<String> field1) {
        this.field1 = field1;
    }

    public String getField2() {
        return field2;
    }

    public void setField2(String field2) {
        this.field2 = field2;
    }

    public int getField3() {
        return field3;
    }

    public void setField3(int field3) {
        this.field3 = field3;
    }
}
