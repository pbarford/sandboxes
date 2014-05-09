package model.types;

import lombok.Data;

import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@Data
@XmlType
@SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
public class PhotoFinish implements Serializable {

    private Boolean unresolved;

    private Integer contendedPosition;
}
