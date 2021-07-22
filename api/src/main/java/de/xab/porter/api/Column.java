package de.xab.porter.api;

import java.sql.JDBCType;

/**
 * A entity class describe the definition of table.
 */
public final class Column {
  // not null
  private String name;
  private String className;
  private int displaySize;
  private boolean signed;
  private int precision;
  private int scale;
  private JDBCType columnType;
  private String columnTypeName;
  // from columns, nullable
  private String comment;
  private String nullable;
  // from primary keys, nullable
  private boolean isPrimaryKey;
  private short primaryKeySeq;
  // from index info, nullable
  private String indexName;

  public Column(String name) { this.name = name; }

  public Column() {}

  public String getName() { return name; }

  public void setName(String name) { this.name = name; }

  public String getClassName() { return className; }

  public void setClassName(String className) { this.className = className; }

  public int getDisplaySize() { return displaySize; }

  public void setDisplaySize(int displaySize) {
    this.displaySize = displaySize;
  }

  public boolean isSigned() { return signed; }

  public void setSigned(boolean signed) { this.signed = signed; }

  public int getPrecision() { return precision; }

  public void setPrecision(int precision) { this.precision = precision; }

  public int getScale() { return scale; }

  public void setScale(int scale) { this.scale = scale; }

  public String getComment() { return comment; }

  public void setComment(String comment) { this.comment = comment; }

  public String getNullable() { return nullable; }

  public void setNullable(String nullable) { this.nullable = nullable; }

  public boolean isPrimaryKey() { return isPrimaryKey; }

  public void setPrimaryKey(boolean primaryKey) { isPrimaryKey = primaryKey; }

  public short getPrimaryKeySeq() { return primaryKeySeq; }

  public void setPrimaryKeySeq(short primaryKeySeq) {
    this.primaryKeySeq = primaryKeySeq;
  }

  public String getIndexName() { return indexName; }

  public void setIndexName(String indexName) { this.indexName = indexName; }

  public JDBCType getColumnType() { return columnType; }

  public void setColumnType(JDBCType columnType) {
    this.columnType = columnType;
  }

  public String getColumnTypeName() { return columnTypeName; }

  public void setColumnTypeName(String columnTypeName) {
    this.columnTypeName = columnTypeName;
  }
}
