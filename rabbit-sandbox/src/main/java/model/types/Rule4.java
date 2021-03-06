package model.types;

import lombok.Data;
import model.validation.custom.annotation.MappingKey;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
@Data
@JsonIgnoreProperties({"additionalProperties"})
@XmlType
public class Rule4 implements Serializable {

    @MappingKey
    private Long id;

    private Integer deductionPercentage;

    private PriceType priceType;

    private String comment;

    private Long startTime;

    private Long endTime;


}

