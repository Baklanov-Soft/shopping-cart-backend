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
    uuid UUID PRIMARY KEY,
    user_uuid UUID NOT NULL,
    role VARCHAR NOT NULL
)

