package model;


import lombok.Data;
import model.types.*;
import model.details.*;
import model.statistics.*;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Data
@SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField", "serial" })
@JsonIgnoreProperties({ "additionalProperties"})
@XmlType
public class Event implements Serializable {

	private Integer id;

    private Action action;

	private String name;

    private Live live;

    private Boolean offeredInRunning;

	private EventDescriptor eventDescriptor;

    private Scheduling scheduling;

    private ClockStage clockStage;

    private Clock clock;

	private BettingStatus bettingStatus;

	private Boolean display;

	private Integer displayOrder;

	private ExtraEventInformation extraEventInformation;

	private String participantAScore;

	private String participantBScore;

	private String extraScoreInfo;

	private Boolean hasOfferedInRunningChanged;

	private Boolean prematchOnly;

	private Integer prematchId;

	private Boolean resulted;

    private Date noMoreBetsTime;

    private MatchDetails matchDetails;

    private Map<EventPropertyType, String> properties;

    private ScoreDetails scoreDetails;

    private List<TvStation> tvStations;

    private List<MatchStatistic> matchStatistics;

    private List<Market> markets;

    private ResultInfo resultInfo;

}
