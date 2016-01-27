package model.statistics;

import lombok.Data;
import model.validation.custom.annotation.MappingKey;

import java.io.Serializable;

@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField", "serial"})
@Data
public class MatchStatistic implements Serializable{

	private static final long serialVersionUID = 5460157773038094582L;

    @MappingKey
	private MatchStatisticType type;
	private Integer participantA;
	private Integer participantB;

}
