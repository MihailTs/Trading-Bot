CREATE TABLE "token" (
     "id" uuid PRIMARY KEY,
     "name" varchar(255),
     "ticker" varchar(15) UNIQUE NOT NULL
);

CREATE TABLE "training-token-prices" (
     "id" uuid PRIMARY KEY,
     "token_id" uuid NOT NULL,
     "price" decimal NOT NULL,
     "created_at" timestamp,
     FOREIGN KEY ("token_id") REFERENCES "token" ("id")
);

CREATE TABLE "live-token-prices" (
     "id" uuid PRIMARY KEY,
     "token_id" uuid NOT NULL,
     "price" decimal NOT NULL,
     "created_at" timestamp,
     FOREIGN KEY ("token_id") REFERENCES "token" ("id")
);

CREATE TABLE "training-transactions" (
     "id" uuid PRIMARY KEY,
     "token_id" uuid NOT NULL,
     "quantity" decimal NOT NULL,
     "price" decimal NOT NULL,
     "type" varchar(10) NOT NULL CHECK(type IN ('BUY', 'SELL')),
     "created_at" timestamp,
     FOREIGN KEY ("token_id") REFERENCES "token" ("id")
);

CREATE TABLE "live-transactions" (
     "id" uuid PRIMARY KEY,
     "token_id" uuid NOT NULL,
     "quantity" decimal NOT NULL,
     "price" decimal NOT NULL,
     "type" varchar(10) NOT NULL CHECK(type IN ('BUY', 'SELL')),
     "created_at" timestamp,
     FOREIGN KEY ("token_id") REFERENCES "token" ("id")
);

CREATE TABLE "training-wallet" (
    "token_id" uuid PRIMARY KEY,
    "balance" decimal NOT NULL DEFAULT 0,
    "updated_at" timestamp,
    FOREIGN KEY ("token_id") REFERENCES "token" ("id")
);

CREATE TABLE "live-wallet" (
    "token_id" uuid PRIMARY KEY,
    "balance" decimal NOT NULL DEFAULT 0,
    "updated_at" timestamp,
    FOREIGN KEY ("token_id") REFERENCES "token" ("id")
);