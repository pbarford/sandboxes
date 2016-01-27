package da.model;

import da.model.annotation.MappingKey;
import da.model.types.BettingStatus;
import da.model.types.SelectionIndexResult;
import da.model.types.SelectionResultType;
import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.List;

@Data
@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
@JsonIgnoreProperties({"additionalProperties"})
@XmlType
public class Selection implements Serializable {

    @MappingKey
    private Integer id;

    private String name;

    private String alternativeName;

    private Integer displayOrder;

    private BettingStatus bettingStatus;

    private Boolean display;

    private Integer typeId;

    private Integer priceNumerator;

    private Integer priceDenominator;

    private String priceDecimal;

    private Integer previousPriceNumerator;

    private Integer previousPriceDenominator;

    private String previousPriceDecimal;

    private SelectionResultType resultType;

    private Integer place;

    private Integer winDeadHeatReductionNumerator;

    private Integer winDeadHeatReductionDenominator;

    private Integer startingPriceNumerator;

    private Integer startingPriceDenominator;

    private Boolean showPrice;

    private List<SelectionIndexResult> indexResults;

}
