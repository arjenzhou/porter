# Porter

![workflow](https://github.com/arjenzhou/porter/actions/workflows/build.yml/badge.svg)
[![codecov](https://codecov.io/gh/arjenzhou/porter/branch/master/graph/badge.svg?token=WMRO0TVZMG)](https://codecov.io/gh/arjenzhou/porter)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/arjenzhou/porter)

                            _____
    ________ ______ __________  /______ ________
    ___  __ \_  __ \__  ___/_  __/_  _ \__  ___/
    __  /_/ // /_/ /_  /    / /_  /  __/_  /
    _  .___/ \____/ /_/     \__/  \___/ /_/
    /_/

Porter is an open-source, universal data-transmission framework.

## Features

- Native JDBC and HTTP clients supported
- High scalability with Porter SPI
- Pluggable data source extensions

## Getting Started

Porter runs on Java 11 or above, you may install appropriate JDK before starting.

There is a demo Reader extension in [demo module](./demo/src). You can check it in advance.

## Properties / Configurations

A transfer task contains one source and one or more sinks. The task has its own properties, source and each sink has its
own properties. Source and sinks are generally a subtype of `DataConnection` with its own properties.

### Task

Source and sinks have the ability to use different type of connector, in other words, they can use different type of
transfer module (e.g. JDBC and HTTP). You can design your channel to handle it.

🔤`channel`  
Optional, channel is the mailbox that reader and writer exchange their data. Default channel does nothing but transfer
data, you can do data transform, flow control in your channel.

🔤`reporter`  
Optional, reporter is used to report task progress. Default reporter just log the batch the task has processed.

### DataConnection

Data connection have the common properties of source and sink.

🔤`connectorType`  
The connector for reader or writer to connect to data source, for JDBC data source `hikari` is the default connector.

🔤`type`  
The data source type of data connection. e.g. mysql.

🔤`url`  
The url of the data source, generally `host:port`.

🔤`username`

🔤`password`

🔤`catalog`  
Catalog of database, could be null if no exists.

🔤`schema`  
Schema of database.

[What's the difference between a catalog and a schema in a relational database?](https://stackoverflow.com/questions/7022755/whats-the-difference-between-a-catalog-and-a-schema-in-a-relational-database)

🔤`table`  
Could be null if source is not a table.

### SrcConnection

🔤`sql`  
The data to be transferred by executing this sql.

#### Properties

✅`readTableMeta`  
Optional, indicates whether read source table meta or not, may cost more performance. `Comment`, `Keys / Indexes`
, `Nullable` are gotten by this.

🔢`batchSize`  
Data rows in each batch.

✅`split`  
Optional, whether split SQL among readers.

🔤`splitColum`  
Optional, split data by this column.

🔢`readerNumber`  
Optional, split SQL to readers of this number.

### SinkConnection

#### Properties

🔤`writeMode`  
Optional, insert mode while using JDBC, available options:

- `default`
- `PREPARED_BATCH`
- `STATEMENT_BATCH`
- `STATEMENT_VALUES`

All data type may have its default or exclusive option.

✅`create`  
Optional, whether create sink table by porter.

## Porter Web

You can also use `porter web` as a daemon server to submitting transfer jobs for debug.

### Template input

```shell
curl --location --request POST 'localhost:8080/transfer' \
--header 'Content-Type: application/json' \
--data-raw \
`{
  "srcConnection": {
    "connectorType": "hikari",
    "type": "mysql",
    "url": "127.0.0.1:3306",
    "username": "root",
    "password": "password",
    "catalog": null,
    "schema": "porter",
    "table": "source_table",
    "sql": "SELECT * FROM `porter`.`source_table`",
    "properties": {
      "batchSize": 5000,
      "readTableMeta": true,
      "split": true,
      "splitColumn" : "source_key",
      "readerNumber" : 5
    }
  },
  "sinkConnections": [
    {
      "connectorType": "hikari",
      "type": "postgresql",
      "url": "127.0.0.1:5432",
      "username": "postgres",
      "password": "password",
      "catalog": "porter",
      "schema": "PUBLIC",
      "table": "sink_table",
      "properties": {
        "writeMode": "default",
        "create": true
      }
    }
  ],
  "properties": {
    "channel": "default",
    "reporter": "default"
  }
}`
```

## Contributing

See [CONTRIBUTING](CONTRIBUTING.md) for details on submitting patches and the contribution workflow.

## Reporting bugs

Follow [ISSUE_TEMPLATE](.github/ISSUE_TEMPLATE/BUG_REPORT.md) to report any issues.

## License

Porter project is under the [MIT license](LICENSE).
