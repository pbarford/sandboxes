package da.model.types;

import da.model.annotation.MappingKey;
import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@Data
@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
@JsonIgnoreProperties({"additionalProperties"})
@XmlType
public class TvStation implements Serializable {

    @MappingKey
    private String name;
}
