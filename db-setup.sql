CREATE TABLE "token" (
     "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
     "name" varchar(255),
     "ticker" varchar(15) UNIQUE NOT NULL
);

CREATE TABLE "training-price" (
     "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
     "token_id" uuid NOT NULL,
     "price" decimal NOT NULL,
     "created_at" timestamp,
     FOREIGN KEY ("token_id") REFERENCES "token" ("id")
);

CREATE TABLE "live-price" (
     "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
     "token_id" uuid NOT NULL,
     "price" decimal NOT NULL,
     "created_at" timestamp,
     FOREIGN KEY ("token_id") REFERENCES "token" ("id")
);

CREATE TABLE "training-transaction" (
     "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
     "token_id" uuid NOT NULL,
     "quantity" decimal NOT NULL,
     "price_id" uuid NOT NULL,
     "type" varchar(10) NOT NULL CHECK(type IN ('BUY', 'SELL')),
     "created_at" timestamp,
     FOREIGN KEY ("token_id") REFERENCES "token" ("id"),
     FOREIGN KEY ("price_id") REFERENCES "training-price" ("id")
);

CREATE TABLE "live-transaction" (
     "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
     "token_id" uuid NOT NULL,
     "quantity" decimal NOT NULL,
     "price_id" uuid NOT NULL,
     "type" varchar(10) NOT NULL CHECK(type IN ('BUY', 'SELL')),
     "created_at" timestamp,
     FOREIGN KEY ("token_id") REFERENCES "token" ("id"),
     FOREIGN KEY ("price_id") REFERENCES "live-price" ("id")
);

CREATE TABLE "training-asset" (
    "token_id" uuid PRIMARY KEY,
    "quantity" decimal NOT NULL DEFAULT 0,
    "updated_at" timestamp,
    FOREIGN KEY ("token_id") REFERENCES "token" ("id")
);

CREATE TABLE "live-asset" (
    "token_id" uuid PRIMARY KEY,
    "quantity" decimal NOT NULL DEFAULT 0,
    "updated_at" timestamp,
    FOREIGN KEY ("token_id") REFERENCES "token" ("id")
);

ALTER TABLE "token" ADD COLUMN "created_at" timestamp;
ALTER TABLE "token" ADD COLUMN "updated_at" timestamp;
ALTER TABLE "token" ALTER COLUMN "name" SET NOT NULL;