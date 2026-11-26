CREATE TABLE movimento_estoque (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tipo TEXT NOT NULL CHECK (tipo IN ('ENTRADA', 'SAIDA', 'VENDA')),
    codigo INTEGER NOT NULL CHECK (codigo > 0),
    quantidade INTEGER NOT NULL CHECK (quantidade > 0),
    estoque_resultante INTEGER NOT NULL CHECK (estoque_resultante >= 0),
    instante TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Estrutura JDBC v2 do registro de publicações do Spring Modulith.
CREATE TABLE event_publication (
    id UUID PRIMARY KEY,
    listener_id TEXT NOT NULL,
    event_type TEXT NOT NULL,
    serialized_event TEXT NOT NULL,
    publication_date TIMESTAMP WITH TIME ZONE NOT NULL,
    completion_date TIMESTAMP WITH TIME ZONE,
    status TEXT,
    completion_attempts INTEGER,
    last_resubmission_date TIMESTAMP WITH TIME ZONE
);

CREATE INDEX event_publication_serialized_event_hash_idx
    ON event_publication USING hash (serialized_event);
CREATE INDEX event_publication_by_completion_date_idx
    ON event_publication (completion_date);
