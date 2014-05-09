package model.types;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
@Data
@JsonIgnoreProperties({"additionalProperties"})
@XmlType
public class EachWayTerms implements Serializable {

	private Integer eachWayNumerator;

	private Integer eachWayDenominator;

	private Integer places;

    private Boolean eachWayBettingAvailable;
}

