package persistence

import akka.persistence.{SnapshotSelectionCriteria, SaveSnapshotSuccess, PersistentActor}

trait SnapshotHousekeeping {
  self : PersistentActor =>

  def performHouseKeeping: Receive = {
    case SaveSnapshotSuccess(meta) =>
      deleteMessages(meta.sequenceNr-1)
      deleteSnapshots(SnapshotSelectionCriteria(minSequenceNr = 0, maxSequenceNr = meta.sequenceNr - 1))
  }
}
