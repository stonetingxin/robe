package io.robe.hibernate.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * An abstract Entity implementation. All entities have to extend this class.
 * Standard fields (oid,lastupdated) will be added to your entity.
 */
@MappedSuperclass
public abstract class BaseEntity implements Serializable {


	private static final long serialVersionUID = 1914842698571907341L;

	@Id
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	@GeneratedValue(generator = "system-uuid")
	@Column( length = 32)
	private String oid;

	@Version
	private long lastUpdated;


	public BaseEntity() {
	}


	public BaseEntity(String oid, long lastUpdated) {
		this.oid = oid;
		this.lastUpdated = lastUpdated;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	public long getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(long lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

}
