module es.rolfan.app {
    requires org.mariadb.jdbc;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.desktop;
    requires com.opencsv;
    requires org.slf4j;

    opens es.rolfan.model.csv;
    opens es.rolfan.model.sql to org.hibernate.orm.core;

    exports es.rolfan.app;
}