package model.types;



import lombok.Data;
import model.types.clock.ClockStatus;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Date;

@Data
@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
@JsonIgnoreProperties({"additionalProperties"})
@XmlType
public class Clock implements Serializable {

    private Period period;

    private Integer periodTimeElapsed;

    private Date updatedAtTimestamp;

    private ClockStatus status;

}
