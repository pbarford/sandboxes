package da.model;


import da.model.types.*;
import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Date;
import java.util.List;


@Data
@SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField", "serial" })
@JsonIgnoreProperties({ "additionalProperties"})
@XmlType
public class Event implements Serializable {

	private Integer id;

	private String name;

    private Boolean offeredInRunning;

	private EventDescriptor eventDescriptor;

	private BettingStatus bettingStatus;

	private Boolean display;

	private Integer displayOrder;

	private Boolean resulted;

    private Date noMoreBetsTime;

    private List<Market> markets;

    private ResultInfo resultInfo;

}
