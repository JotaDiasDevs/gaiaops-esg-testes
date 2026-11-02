package br.com.fiap.gaiaops.suporte;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Relogio controlado pelos testes.
 *
 * A API decide "licenca vencida", "dias para vencer" e a data das medicoes pelo
 * relogio. Com a hora real, um teste aprovado hoje poderia falhar amanha. Aqui a
 * data e fixa (01/10/2026 12:00 UTC) e um cenario pode ajusta-la; o valor volta
 * ao padrao antes de cada cenario/teste.
 */
public class RelogioDeTeste extends Clock {

    public static final Instant PADRAO = Instant.parse("2026-10-01T12:00:00Z");

    private volatile Instant agora = PADRAO;

    public void ajustar(Instant instante) {
        this.agora = instante;
    }

    public void restaurar() {
        this.agora = PADRAO;
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zona) {
        return Clock.fixed(agora, zona);
    }

    @Override
    public Instant instant() {
        return agora;
    }
}
