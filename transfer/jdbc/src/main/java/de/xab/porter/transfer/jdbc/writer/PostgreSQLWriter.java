package de.xab.porter.transfer.jdbc.writer;

import static de.xab.porter.common.util.Strings.notNullOrEmpty;

import de.xab.porter.api.Column;
import de.xab.porter.api.Relation;
import de.xab.porter.api.Result;
import de.xab.porter.api.dataconnection.SinkConnection;
import de.xab.porter.api.exception.PorterException;
import de.xab.porter.common.util.Loggers;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

/**
 * PostgreSQL JDBC writer
 */
public class PostgreSQLWriter extends JDBCWriter {
  private Logger logger = Loggers.getLogger(this.getClass());

  @Override
  protected void writeInDefaultMode(SinkConnection sinkConnection,
                                    Connection jdbcConnection, Result<?> data) {
    writeInPostgreSQLFileMode(sinkConnection, jdbcConnection, data);
  }

  /**
   * using copy for postgreSQL
   */
  protected void writeInPostgreSQLFileMode(SinkConnection sinkConnection,
                                           Connection jdbcConnection,
                                           Result<?> data) {
    SinkConnection.Properties properties = sinkConnection.getProperties();
    Relation relation = (Relation)data.getResult();
    StringReader stringReader = null;
    String columns = "";
    if (!properties.isAllColumns()) {
      columns =
          relation.getMeta()
              .stream()
              .map(col
                   -> getColumnIdentifier(col.getName(), properties.getQuote()))
              .collect(Collectors.joining(", "));
    }
    String tableIdentifier = properties.getTableIdentifier();
    String copySQL = String.format("COPY %s %s FROM STDIN WITH DELIMITER '|'",
                                   tableIdentifier, columns);
    try {
      BaseConnection pgConnection = jdbcConnection.unwrap(BaseConnection.class);
      CopyManager copyManager = new CopyManager(pgConnection);
      String csv = relation.getData()
                       .stream()
                       .map(row
                            -> row.stream()
                                   .map(Object::toString)
                                   .collect(Collectors.joining("|")))
                       .collect(Collectors.joining("\n"));
      stringReader = new StringReader(csv);
      long rowCount =
          copyManager.copyIn(copySQL, new BufferedReader(stringReader));
      logger.log(Level.INFO, String.format("wrote %d rows to table %s",
                                           rowCount, tableIdentifier));
    } catch (IOException | SQLException e) {
      throw new PorterException("copy data failed", e);
    } finally {
      if (stringReader != null) {
        stringReader.close();
      }
    }
  }

  @Override
  protected String getCreate(String tableIdentifier) {
    return String.format("CREATE TABLE %s (\n", tableIdentifier);
  }

  @Override
  protected String getColumns(List<Column> meta, String quote) {
    return meta.stream()
        .map(column
             -> "\t" + getColumnIdentifier(column.getName(), quote) + "\t" +
                    getColumnType(column) + "\t" +
                    ((notNullOrEmpty(column.getNullable()) &&
                      "NO".equals(column.getNullable()))
                         ? "NOT NULL"
                         : "NULL"))
        .collect(Collectors.joining(", \n"));
  }

  @Override
  protected String getAfterDDL(String tableIdentifier, String quote,
                               List<Column> meta) {
    return meta.stream()
               .map(column
                    -> notNullOrEmpty(column.getComment())
                           ? ";\nCOMMENT ON COLUMN " + tableIdentifier + "." +
                                 quote + column.getName() + quote + " IS '" +
                                 column.getComment() + "'"
                           : "")
               .collect(Collectors.joining()) +
        ";";
  }

  @Override
  protected String getTableIdentifier(SinkConnection sinkConnection) {
    String quote = sinkConnection.getProperties().getQuote();
    return quote + sinkConnection.getCatalog() + quote + "." + quote +
        sinkConnection.getSchema() + quote + "." + quote +
        sinkConnection.getTable() + quote;
  }

  @Override
  protected String getColumnType(Column column) {
    switch (column.getColumnType()) {
    case TIMESTAMP:
      return column.getColumnType().getName();
    default:
      return super.getColumnType(column);
    }
  }
}
