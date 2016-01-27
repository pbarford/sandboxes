package da.model.types;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@Data
@SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
@JsonIgnoreProperties({"additionalProperties"})
@XmlType
public class Status implements Serializable {

    private Boolean suspended;

    private Boolean displayed;
}
