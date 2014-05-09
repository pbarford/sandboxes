package model.types;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Date;

@Data
@SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
@JsonIgnoreProperties({"additionalProperties"})
@XmlType
public class Scheduling implements Serializable {

    private Date scheduledStartTime;

    private Date actualStartTime;

}
