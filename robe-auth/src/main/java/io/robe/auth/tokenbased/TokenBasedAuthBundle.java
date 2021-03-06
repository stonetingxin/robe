package io.robe.auth.tokenbased;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.robe.auth.tokenbased.configuration.HasTokenBasedAuthConfiguration;
import io.robe.auth.tokenbased.configuration.TokenBasedAuthConfiguration;
import io.robe.auth.tokenbased.filter.TokenBasedAuthResponseFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenBasedAuthBundle<T extends Configuration & HasTokenBasedAuthConfiguration> implements ConfiguredBundle<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenBasedAuthBundle.class);
    private TokenBasedAuthConfiguration configuration;

    /**
     * Initializes the environment.
     *
     * @param configuration the configuration object
     * @param environment   the service's {@link io.dropwizard.setup.Environment}
     * @throws Exception if something goes wrong
     */
    @Override
    public void run(T configuration, Environment environment) throws Exception {
        LOGGER.info("\n------------------------\n-------Auth Bundle------\n------------------------");
        this.configuration = configuration.getTokenBasedAuthConfiguration();

        environment.jersey().getResourceConfig().getContainerResponseFilters().add(new TokenBasedAuthResponseFilter(configuration.getTokenBasedAuthConfiguration()));
    }

    /**
     * Initializes the service bootstrap.
     *
     * @param bootstrap the service bootstrap
     */
    @Override
    public void initialize(Bootstrap<?> bootstrap) {
    }


    public TokenBasedAuthConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(TokenBasedAuthConfiguration configuration) {
        this.configuration = configuration;
    }
}
