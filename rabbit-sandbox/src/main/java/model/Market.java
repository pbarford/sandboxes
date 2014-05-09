package model;

import model.types.*;
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

    private Integer id;

    private Action action;

    private Boolean offeredInRunning;

    private Integer typeId;

    private String name;

    private BettingStatus bettingStatus;

    private Boolean display;

    private MarketSort sort;

    private Integer displayOrder;

    private Boolean inRunning;

    private Integer minAccumulator;

    private Integer maxAccumulator;

    private Boolean resulted;

    private Integer indexValue;

    private EachWayTerms eachWayTerms;

    private List<Rule4> rule4s;

    private Double handicapValue;

    private Double resultValue;

    private ToteBetting toteBetting;

    private Boolean startingPriceBettingAvailable;

    private Boolean livePriceBettingAvailable;

    private List<Selection> selections;

    private List<MarketIndexResulted> indexResults;

}
