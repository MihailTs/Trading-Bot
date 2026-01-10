CREATE TABLE "token" (
     "id" bigint PRIMARY KEY NOT NULL ,
     "name" varchar(255) NOT NULL,
     "ticker" varchar(15) NOT NULL
);

CREATE TABLE "training-price" (
     "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
     "token_id" bigint NOT NULL,
     "price" decimal NOT NULL,
     "created_at" timestamp,
     FOREIGN KEY ("token_id") REFERENCES "token" ("id")
);

CREATE TABLE "live-price" (
     "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
     "token_id" bigint NOT NULL,
     "price" decimal NOT NULL,
     "created_at" timestamp,
     FOREIGN KEY ("token_id") REFERENCES "token" ("id")
);

CREATE TABLE "training-transaction" (
     "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
     "token_id" bigint NOT NULL,
     "quantity" decimal NOT NULL,
     "price_id" uuid NOT NULL,
     "type" varchar(10) NOT NULL CHECK(type IN ('BUY', 'SELL')),
     "created_at" timestamp,
     FOREIGN KEY ("token_id") REFERENCES "token" ("id"),
     FOREIGN KEY ("price_id") REFERENCES "training-price" ("id")
);

CREATE TABLE "live-transaction" (
     "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
     "token_id" bigint NOT NULL,
     "quantity" decimal NOT NULL,
     "price_id" uuid NOT NULL,
     "type" varchar(10) NOT NULL CHECK(type IN ('BUY', 'SELL')),
     "created_at" timestamp,
     FOREIGN KEY ("token_id") REFERENCES "token" ("id"),
     FOREIGN KEY ("price_id") REFERENCES "live-price" ("id")
);

CREATE TABLE "training-asset" (
    "token_id" bigint PRIMARY KEY,
    "quantity" decimal NOT NULL DEFAULT 0,
    "updated_at" timestamp,
    FOREIGN KEY ("token_id") REFERENCES "token" ("id")
);

CREATE TABLE "live-asset" (
    "token_id" bigint PRIMARY KEY,
    "quantity" decimal NOT NULL DEFAULT 0,
    "updated_at" timestamp,
    FOREIGN KEY ("token_id") REFERENCES "token" ("id")
);

ALTER TABLE "token" ADD COLUMN "created_at" timestamp;
ALTER TABLE "token" ADD COLUMN "updated_at" timestamp;
ALTER TABLE "token" ALTER COLUMN "name" SET NOT NULL;
ALTER TABLE "live-asset" ADD COLUMN "created_at" timestamp;
ALTER TABLE "training-asset" ADD COLUMN "created_at" timestamp;

ALTER TABLE "training-transaction" DROP COLUMN "token_id";
ALTER TABLE "live-transaction" DROP COLUMN "token_id";
ALTER TABLE "token" ADD COLUMN "circulating_supply" decimal;

-- values are as of 10.01.2026 around 15:30
INSERT INTO "token" (id, name, ticker, circulating_supply, created_at, updated_at)
VALUES (1, 'Bitcoin', 'BTC', 19974018, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
        (1027, 'Ethereum', 'ETH', 120694733.7402891, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
        (825, 'Tether USDt', 'USDT', 186966394639.43137, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
        (52, 'XRP', 'XRP', 60699967552, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
        (1839, 'BNB', 'BNB', 137734106.82999998, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
        (5426, 'Solana', 'SOL', 564261938.2380047, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
        (3408, 'USDC', 'USDC', 74754621689.3042, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
        (1958, 'Tron', 'TRX', 94701724892.11887, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
        (74, 'Dogecoin', 'DOGE', 168257143126.5791, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

ALTER TABLE "token" ALTER COLUMN "circulating_supply" SET NOT NULL;
ALTER TABLE "token" ADD CONSTRAINT minimum_constraint CHECK ("circulating_supply" >= 0);