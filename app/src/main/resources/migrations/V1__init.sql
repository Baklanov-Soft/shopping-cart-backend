CREATE TABLE brands (
    uuid UUID PRIMARY KEY,
    name VARCHAR UNIQUE NOT NULL
);

CREATE TABLE categories (
    uuid UUID PRIMARY KEY,
    name VARCHAR UNIQUE NOT NULL
);

CREATE TABLE items (
    uuid UUID PRIMARY KEY,
    name VARCHAR UNIQUE NOT NULL,
    description VARCHAR NOT NULL,
    price NUMERIC NOT NULL,
    currency VARCHAR(3) NOT NULL,
    brand_id UUID NOT NULL,
    category_id UUID NOT NULL,
    CONSTRAINT brand_id_fkey FOREIGN KEY (brand_id)
      REFERENCES brands (uuid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
    CONSTRAINT cat_id_fkey FOREIGN KEY (category_id)
      REFERENCES categories (uuid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE users (
    uuid UUID PRIMARY KEY,
    username VARCHAR UNIQUE NOT NULL,
    passwordHashed VARCHAR NOT NULL
);

CREATE TABLE salt (
    user_uuid UUID PRIMARY KEY,
    salt VARCHAR NOT NULL,
    iterations INTEGER NOT NULL,
    CONSTRAINT user_uuid_fk FOREIGN KEY (user_uuid) REFERENCES users (uuid)
);

CREATE TABLE roles (
    user_uuid UUID NOT NULL,
    role VARCHAR NOT NULL,
    UNIQUE (user_uuid, role),
    CONSTRAINT user_uuid_fk FOREIGN KEY (user_uuid) REFERENCES users (uuid)
);

CREATE TABLE orders(
    uuid UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    payment_id UUID UNIQUE NOT NULL,
    items JSONB NOT NULL, -- just a map itemId -> amount
    total NUMERIC NOT NULL,
    currency VARCHAR(3) NOT NULL,
    CONSTRAINT user_id_fkey FOREIGN KEY (user_id) REFERENCES users(uuid) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION
)

