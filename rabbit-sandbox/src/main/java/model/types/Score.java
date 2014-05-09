package model.types;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@Data
@SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
@JsonIgnoreProperties({ "additionalProperties" })
@XmlType
public class Score implements Serializable{
	
	private Integer timeOfScore;
    private String participantName;
    private Period period;


}
