package model.types;

import lombok.Data;
import model.validation.custom.annotation.MappingKey;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;


@Data
@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
@JsonIgnoreProperties({"additionalProperties"})
@XmlType
public class SelectionIndexResult implements Serializable {

    @MappingKey
    private Integer indexValue;
    private SelectionResultType resultType;

}
