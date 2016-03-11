package com.paddypower.destmgmt.model

import java.util.Date

import Constants.Action._
import Constants.Live._
import Constants.BettingStatus._
import Constants.ExtraEventInformation._
import Constants.EventPropertyType._
import Constants.EventSort._
import Constants.MarketSort._
import Constants.SelectionResultType._
import Constants.SelectionCharacteristic._
import Constants.SelectionPropertyType._
import Constants.Period._
import Constants.ClockStatus._
import Constants.BetType._
import Constants.PriceType._
import Constants.MatchDetailType._
import Constants.MatchStatisticType._


case class Event(id: Integer,
                 action: String,
                 eventDescriptor: EventDescriptor,
                 name:Option[String] = None,
                 markets:Option[List[Market]] = None)

case class TEvent(id: Integer,
                  action: Action,
                  name: String,
                  live: Live,
                  offeredInRunning: Boolean,
                  eventDescriptor: EventDescriptor,
                  scheduling: Scheduling,
                  clockStage: ClockStage,
                  clock: Clock,
                  bettingStatus: BettingStatus,
                  display: Boolean,
                  displayOrder: Integer,
                  extraEventInformation: ExtraEventInformation,
                  participantAScore: String,
                  participantBScore: String,
                  extraScoreInfo: String,
                  hasOfferedInRunningChanged: Boolean,
                  prematchOnly: Boolean,
                  prematchId: Integer,
                  resulted: Boolean,
                  noMoreBetsTime: Date,
                  matchDetails: MatchDetails,
                  properties: Map[EventPropertyType, String],
                  scoreDetails: ScoreDetails,
                  tvStations: List[TvStation],
                  matchStatistics: List[MatchStatistic],
                  markets: List[Market],
                  resultInfo: ResultInfo,
                  riskManagement: RiskManagement,
                  traderId: Integer,
                  sort: EventSort)

case class Market(id: Integer,
               action: Action,
               offeredInRunning: Boolean,
               typeId: Integer,
               name: String,
               bettingStatus: BettingStatus,
               display: Boolean,
               sort: MarketSort,
               displayOrder: Integer,
               inRunning: Boolean,
               minAccumulator: Integer,
               maxAccumulator: Integer,
               resulted: Boolean,
               indexValue: Integer,
               eachWayTerms: EachWayTerms,
               rule4s: List[Rule4],
               typeLinkId: Integer,
               handicapValue: Double,
               isBIRTransition: Boolean,
               resultValue: Double,
               toteBetting: ToteBetting,
               startingPriceBettingAvailable: Boolean,
               livePriceBettingAvailable: Boolean,
               selections: List[Selection],
               indexResults: List[MarketIndexResulted])

case class Selection(alternativeName: String,
                     typeId: Integer,
                     priceNumerator: Integer,
                     priceDenominator: Integer,
                     priceDecimal: String,
                     previousPriceNumerator: Integer,
                     previousPriceDenominator: Integer,
                     previousPriceDecimal: String,
                     resultType: SelectionResultType,
                     place: Integer,
                     characteristic: SelectionCharacteristic,
                     winDeadHeatReductionNumerator: Integer,
                     winDeadHeatReductionDenominator: Integer,
                     startingPriceNumerator: Integer,
                     startingPriceDenominator: Integer,
                     showPrice: Boolean,
                     appearanceId: String,
                     properties: Map[SelectionPropertyType, String],
                     indexResults: List[SelectionIndexResult],
                     handicapScore: Integer,
                     participantKey: String)

case class Clock(period: Period, periodTimeElapsed: Integer, updatedAtTimestamp: Date, status: ClockStatus)

case class ClockStage(currentPeriodStartTime: Date, periodId: Integer)

case class Dividend(betType: BetType, selectionIds: String, amount: Double)

case class EachWayTerms(eachWayNumerator: Integer, eachWayDenominator: Integer, places: Integer, eachWayBettingAvailable: Boolean, eachWayTermsWithBet: Boolean)

case class EventDescriptor(typeId: Integer,
                           subclassId: Integer,
                           typeName: Option[String] = None,
                           subclassName: Option[String] = None,
                           displayTypeId: Option[Integer] = None,
                           displayTypeName: Option[String] = None)

case class MarketDescriptor(typeId:Integer, isBirType:Boolean)

case class MarketIndexResulted(indexValue: Integer, resulted: Boolean)

case class MarketStatus(birIndex: Integer,
                        confirmResults: Boolean,
                        eachwayFactorNum: Integer,
                        eachwayFactorDen: Integer,
                        eachwayPlaces: Integer,
                        handicapMakeup: Double,
                        hcapValue: Double,
                        score: String)

case class PhotoFinish(unresolved:Boolean, contendedPosition:Integer)

case class ResultInfo(runners: List[Runner],photoFinishes: List[PhotoFinish])

case class RiskManagement(layToLose: Double, liabilityLimit: Double, leastMaxBet: Double, mostMaxBet: Double)

case class Rule4(id: Long, deductionPercentage: Integer, priceType: PriceType, comment: String, startTime: Long, endTime: Long)

case class Runner(runnerNumber: Integer,
                  runnerName: String,
                  startingPriceNumerator: Integer,
                  startingPriceDenominator: Integer,
                  favouriteInformation: String,
                  finishPosition: String,
                  resultAmendment: String,
                  winningDistance: String,
                  photoFinish: Boolean)

case class Scheduling(scheduledStartTime: Date, actualStartTime: Date)
case class Score(timeOfScore: Integer, participantName: String, period: Period)
case class ScoreDetails(participantAScores: List[Score], participantBScores: List[Score])
case class SelectionIndexResult(indexValue: Integer, resultType: SelectionResultType)
case class Status(suspended: Boolean, displayed: Boolean)
case class TvStation(name:String)

case class MatchDetail(matchDetailType: MatchDetailType, time: Integer, participantName: String, period: Period)
case class MatchDetails(participantAMatchDetails: List[MatchDetail], participantBMatchDetails: List[MatchDetail])

case class MatchStatistic(`type`: MatchStatisticType, participantA: Integer, participantB: Integer)
case class ToteBetting(forecastAvailable: Boolean, tricastAvailable: Boolean, dividends: List[Dividend])