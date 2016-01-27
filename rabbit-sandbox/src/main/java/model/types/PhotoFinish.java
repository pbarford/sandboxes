package model.types;

import lombok.Data;
import model.validation.custom.annotation.MappingKey;

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
