package de.bsd.quarkus.funq;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.Entity;

/**
 *
 */
@Entity
public class QTemplate extends PanacheEntity {

    String bae; // bundle-app-event_type
    String type; // type like 'email' or 'json'
    String subtype; // like 'header' or 'body'

    public String body; // The template text

    void store() {
        persistAndFlush();
    }
}
