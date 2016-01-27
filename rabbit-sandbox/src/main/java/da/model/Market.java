package da.model;

import da.model.annotation.MappingKey;
import da.model.types.*;
import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

@Data
@SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
@JsonIgnoreProperties({ "additionalProperties"})
@XmlRootElement
public class Market implements Serializable {

    @MappingKey
    private Integer id;

    private Boolean offeredInRunning;

    private Integer typeId;

    private String name;

    private BettingStatus bettingStatus;

    private Boolean display;

    private Integer displayOrder;

    private Boolean inRunning;

    private Integer minAccumulator;

    private Integer maxAccumulator;

    private Boolean resulted;

    private Integer indexValue;

    private Double handicapValue;

    private Double resultValue;

    private Boolean startingPriceBettingAvailable;

    private Boolean livePriceBettingAvailable;

    private List<Selection> selections;

    private List<MarketIndexResulted> indexResults;

}
