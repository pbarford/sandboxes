package model.types;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlType;

@Data
@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
@JsonIgnoreProperties({"additionalProperties"})
@XmlType
public class MarketStatus extends Status {

    private Integer birIndex;

    private Boolean confirmResults;

    private Integer eachwayFactorNum;
    private Integer eachwayFactorDen;
    private Integer eachwayPlaces;
    private Double handicapMakeup;
    private Double hcapValue;

    private String score;

}
