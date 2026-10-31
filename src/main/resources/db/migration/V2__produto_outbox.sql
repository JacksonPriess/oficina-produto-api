CREATE TABLE produto_outbox (
    event_id UUID PRIMARY KEY,
    payload TEXT NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    publicado_em TIMESTAMPTZ,
    tentativas INTEGER NOT NULL DEFAULT 0,
    proxima_tentativa TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultimo_erro VARCHAR(1000)
);

CREATE INDEX idx_produto_outbox_pendente ON produto_outbox(proxima_tentativa, criado_em)
    WHERE publicado_em IS NULL;
