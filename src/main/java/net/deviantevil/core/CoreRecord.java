package net.deviantevil.core;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;

//CREATE TABLE logs (
//    id INT NOT NULL AUTO_INCREMENT,
//    timestamp TIMESTAMP DEFAULT NOW(),
//    level VARCHAR(20) DEFAULT NULL,
//    name VARCHAR(20) DEFAULT NULL,
//    message TEXT NOT NULL,
//    PRIMARY KEY(id)
//);

@Entity
@Table(name = "decore_log")
public class CoreRecord {
    @Id
    private int id;

    @NotNull
    private Date timestamp;

    @NotEmpty
    private String level;

    @NotEmpty
    private String name;

    @NotEmpty
    private String message;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
