package model.details;


import lombok.Data;
import model.types.Period;
import model.validation.custom.annotation.MappingKey;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@Data
@SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
@JsonIgnoreProperties({ "additionalProperties" })
@XmlType
public class MatchDetail implements Serializable{

    @MappingKey
    private MatchDetailType matchDetailType;
	private Integer time;
    private String participantName;
    private Period period;


}
