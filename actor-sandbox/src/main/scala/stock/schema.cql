CREATE KEYSPACE stocks WITH replication = {
  'class': 'NetworkTopologyStrategy',
  'datacenter1': '1'
};

USE stocks;

CREATE TABLE quotes (
  symbol text,
  trading_date text,
  quote_timestamp bigint,
  time text,
  bid text,
  ask text,
  exchange text,
  PRIMARY KEY ((symbol, trading_date), quote_timestamp)
) WITH CLUSTERING ORDER BY (quote_timestamp DESC) AND
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.000000 AND
  gc_grace_seconds=864000 AND
  read_repair_chance=0.100000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'SnappyCompressor'};