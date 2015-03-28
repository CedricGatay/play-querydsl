package models.ignored;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author cgatay
 */
@Entity
public class NotQueryDSL {
    @Id
    @GeneratedValue
    public Long id;
    public String ignored;
}
