package da.model.types;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@Data
@SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
@JsonIgnoreProperties({"additionalProperties"})
@XmlType
public class EventDescriptor implements Serializable {

    private Integer typeId;

    private String typeName;

    private Integer displayTypeId;

    private String displayTypeName;

    private Integer subclassId;

    private String subclassName;

}
