package net.deviantevil.moderation;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.avaje.ebean.validation.NotEmpty;

//CREATE TABLE moderation_log (
//id INT NOT NULL AUTO_INCREMENT,
//stamp TIMESTAMP DEFAULT NOW(),
//end TIMESTAMP DEFAULT NULL,
//action VARCHAR(20) NOT EMPTY,
//issuer VARCHAR(20) NOT EMPTY,
//target VARCHAR(20) NOT EMPTY,
//reason TEXT,
//PRIMARY KEY(id)
//);

@Entity
@Table(name="decore_moderation")
public class ModerationRecord {
    @Id
    private int id;
    
    private Date timestamp;
    
    private Date end;
    
    @NotEmpty
    private String action;
    
    @NotEmpty
    private String issuer;
    
    @NotEmpty
    private String target;
    
    private String reason;
    
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

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}