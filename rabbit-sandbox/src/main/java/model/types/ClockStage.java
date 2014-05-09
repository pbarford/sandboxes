package model.types;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: luca
 * Date: 5/31/13
 * Time: 2:43 PM
 * To change this template use File | Settings | File Templates.
 */


@Data
@SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
@JsonIgnoreProperties({"additionalProperties"})
@XmlType
public class ClockStage implements Serializable {

    private Date currentPeriodStartTime;
    private Integer periodId;

}
