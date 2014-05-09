package model.types;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.List;

@Data
@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
@JsonIgnoreProperties({"additionalProperties"})
@XmlType
public class ToteBetting implements Serializable {

    private Boolean forecastAvailable;

    private Boolean tricastAvailable;

    private List<Dividend> dividends;
}
