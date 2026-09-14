package com.corhuila.sgie.Security;

import io.github.bucket4j.ConsumptionProbe;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRateLimiterTest {

    @Test
    void permiteConsumirHastaLaCapacidadConfigurada() {
        LoginRateLimiter limiter = new LoginRateLimiter(3, 1);

        assertThat(limiter.tryConsume("1.2.3.4").isConsumed()).isTrue();
        assertThat(limiter.tryConsume("1.2.3.4").isConsumed()).isTrue();
        assertThat(limiter.tryConsume("1.2.3.4").isConsumed()).isTrue();

        ConsumptionProbe cuartoIntento = limiter.tryConsume("1.2.3.4");
        assertThat(cuartoIntento.isConsumed()).isFalse();
        assertThat(cuartoIntento.getNanosToWaitForRefill()).isGreaterThan(0);
    }

    @Test
    void cadaClaveTieneSuPropioLimite() {
        LoginRateLimiter limiter = new LoginRateLimiter(1, 1);

        assertThat(limiter.tryConsume("ip-A").isConsumed()).isTrue();
        assertThat(limiter.tryConsume("ip-A").isConsumed()).isFalse();

        assertThat(limiter.tryConsume("ip-B").isConsumed()).isTrue();
    }
}
