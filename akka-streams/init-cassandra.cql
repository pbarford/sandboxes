USE docker_dev;

DROP TABLE selection_bet;
DROP TABLE bet;
DROP TABLE multi_bet;

CREATE TABLE selection_bet (
  selection_id text,
  bet_id text,
  sub_key text,
  bet_timestamp bigint,
  action text,
  stake double,
  price_type text,
  price double,
  bet_type text,
  cur text,
  PRIMARY KEY ((selection_id), bet_id, sub_key, bet_timestamp)
);

CREATE TABLE bet (
  bet_id text,
  sub_key text,
  selection_id text,
  bet_timestamp bigint,
  action text,
  stake double,
  price_type text,
  price double,
  bet_type text,
  cur text,
  PRIMARY KEY ((bet_id), sub_key, selection_id, bet_timestamp)
);


CREATE TABLE multi_bet (
  multi_key text,
  bet_id text,
  bet_timestamp bigint,
  action text,
  profit_loss double,
  payout_total double,
  bet_type text,
  sub_bet_type text,
  cur text,
  PRIMARY KEY ((multi_key), bet_id, bet_timestamp)
);