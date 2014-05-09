package model.details;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.List;

@Data
@SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
@JsonIgnoreProperties({ "additionalProperties" })
@XmlType
public class MatchDetails implements Serializable {

	private List<MatchDetail> participantAMatchDetails;

	private List<MatchDetail> participantBMatchDetails;

}
