CREATE TABLE produto (
    codigo  INTEGER        PRIMARY KEY,
    preco   NUMERIC(12, 2) NOT NULL CHECK (preco > 0),
    estoque INTEGER        NOT NULL CHECK (estoque >= 0)
);
