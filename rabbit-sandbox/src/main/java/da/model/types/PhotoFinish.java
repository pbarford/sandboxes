package da.model.types;

import da.model.annotation.MappingKey;
import lombok.Data;

import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@Data
@XmlType
@SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
public class PhotoFinish implements Serializable {

    private Boolean unresolved;

    @MappingKey
    private Integer contendedPosition;
}
