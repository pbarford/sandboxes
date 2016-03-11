package com.paddypower.destmgmt.model

object Constants {

  object Action extends Enumeration {
    type Action = Value
    val CREATE, UPDATE, REFRESH = Value
  }

  object BettingStatus extends Enumeration {
    type BettingStatus = Value
    val ACTIVE, SUSPENDED = Value
  }

  object BetType extends Enumeration {
    type BetType = Value
    val FORECAST, TRICAST, TOTE_WIN, TOTE_PLACE, TOTE_EXACTA, TOTE_TRIFECTA = Value
  }

  object EventPropertyType extends Enumeration {
    type EventPropertyType = Value
    val GAME_LENGTH,
    COUNTRY,
    RACE_DISTANCE,
    RACE_NUMBER,
    GOING_INFORMATION,
    GROUP_ID,
    GROUP_STATUS,
    EVENT_STAGE,
    NUMBER_OF_RUNNERS,
    MAXIMUM_NUMBER_OF_RUNNERS,
    HANDICAP,
    GROUP_TYPE,
    EVENT_STATUS = Value
  }

  object EventSort extends Enumeration {
    type EventSort = Value
    val MATCH,
    TOURNAMENT,
    GROUP_1,
    GROUP_2,
    GROUP_3,
    GROUP_4,
    GROUP_5,
    GROUP_6,
    GROUP_7,
    GROUP_8,
    GROUP_9,
    GROUP_10,
    GROUP_11,
    GROUP_12,
    GROUP_13,
    GROUP_14,
    GROUP_15,
    GROUP_16 = Value
  }

  object ExtraEventInformation extends Enumeration {
    type ExtraEventInformation = Value
    val RAIN_DELAY, NOT_APPLICABLE = Value
  }

  object Live extends Enumeration {
    type Live = Value
    val YES, NO, NOT_APPLICABLE = Value
  }

  object MarketSort extends Enumeration {
    type MarketSort = Value
    val STANDARD,
    CORRECT_SCORE,
    FIRST_SCORER,
    HEAD_TO_HEAD,
    HIGHER_LOWER,
    NEW_HANDICAP_BETTING,
    WIN_DRAW_WIN,
    SCORECAST,
    WESTERN_HANDICAP,
    INDEX_MARKET,
    ASIAN_HANDICAP = Value
  }

  object Period extends Enumeration {
    type Period = Value
    val PRE_MATCH,
    PERIOD_1,
    END_PERIOD_1,
    PERIOD_2,
    END_PERIOD_2,
    PERIOD_3,
    END_PERIOD_3,
    PERIOD_4,
    END_PERIOD_4,
    OVERTIME,
    END_OVERTIME,
    EXTRA_TIME_1,
    END_EXTRA_TIME_1,
    EXTRA_TIME_2,
    END_EXTRA_TIME_2,
    PENALTIES,
    END,
    NOT_APPLICABLE = Value
  }

  object PriceType extends Enumeration {
    type PriceType = Value
    val STARTING_PRICE, LIVE_PRICE = Value
  }

  object SelectionCharacteristic extends Enumeration {
    type SelectionCharacteristic = Value
    val HOME,
    DRAW,
    AWAY,
    HIGHER,
    LOWER,
    BETTING_WITHOUT = Value
  }

  object SelectionPropertyType extends Enumeration {
    type SelectionPropertyType = Value
    val RUNNER_ID,
    RUNNER_NUMBER,
    JOCKEY_NAME,
    RUNNER_STATUS,
    DRAW_NUMBER,
    WEIGHT,
    TRAINER,
    AGE = Value
  }

  object SelectionResultType extends Enumeration {
    type SelectionResultType = Value
    val WIN, LOSE, HCAP_RESULT, HIGHER_LOWER_RESULT, PUSH, VOID, PLACE, INDEX_RESULTED = Value
  }

  object ClockStatus extends Enumeration {
    type ClockStatus = Value
    val RUNNING, STOPPED, NOT_APPLICABLE = Value
  }

  object MatchDetailType extends Enumeration {
    type MatchDetailType = Value
    val CORNERS, YELLOW_CARD, RED_CARD = Value
  }

  object MatchStatisticType extends Enumeration {
    type MatchStatisticType = Value
    val CORNERS, YELLOW_CARDS, RED_CARDS, YELLOW_RED_CARDS = Value
  }
}