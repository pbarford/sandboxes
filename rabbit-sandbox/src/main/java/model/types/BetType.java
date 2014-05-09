package model.types;

import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum BetType {
    FORECAST,
    TRICAST,
    TOTE_WIN,
    TOTE_PLACE,
    TOTE_EXACTA,
    TOTE_TRIFECTA;
}
