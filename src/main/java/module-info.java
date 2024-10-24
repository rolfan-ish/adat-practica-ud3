module es.rolfan.app {
    requires org.mariadb.jdbc;
    requires com.opencsv;
    requires jakarta.persistence;
    requires org.slf4j;
    requires org.apache.logging.log4j;
    requires org.hibernate.orm.core;

    opens es.rolfan.dao.sql to org.hibernate.orm.core;
    opens es.rolfan.dao.csv;

    exports es.rolfan.dao.csv to com.opencsv;
    exports es.rolfan.app;
}