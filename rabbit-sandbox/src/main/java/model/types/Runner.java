package model.types;

import lombok.Data;

import java.io.Serializable;

@Data
@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
public class Runner implements Serializable {

    private Integer runnerNumber;

    private String runnerName;

    private Integer startingPriceNumerator;

    private Integer startingPriceDenominator;

    private String favouriteInformation;
    private String finishPosition;
    private String resultAmendment;
    private String winningDistance;

    private Boolean photoFinish;
}

