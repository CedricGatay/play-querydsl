package models.included;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author cgatay
 */
@Entity
public class Book {
    @Id
    @GeneratedValue
    public Long id;
    public String title;
}
