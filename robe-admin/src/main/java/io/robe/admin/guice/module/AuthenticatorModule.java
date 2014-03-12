package io.robe.admin.guice.module;

import com.google.common.cache.CacheBuilderSpec;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.yammer.dropwizard.auth.Authenticator;
import com.yammer.dropwizard.auth.CachingAuthenticator;
import io.robe.admin.hibernate.dao.ServiceDao;
import io.robe.admin.hibernate.dao.UserDao;
import io.robe.auth.Credentials;
import io.robe.auth.SampleToken;
import io.robe.auth.TokenWrapper;
import io.robe.auth.impl.tokenbased.TokenBasedAuthenticator;
import io.robe.hibernate.HibernateBundle;

/**
 * Default Guice bindings are done at this class.
 */
public class AuthenticatorModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(Authenticator.class).toProvider(new Provider<Authenticator<String, Credentials>>() {
            @Inject
            HibernateBundle hibernateBundle;

            @Override
            public Authenticator get() {
                TokenWrapper.initialize(SampleToken.class);
                TokenBasedAuthenticator tokenBasedAuthenticator =
                        new TokenBasedAuthenticator(
                                new UserDao(hibernateBundle.getSessionFactory()),
                                new ServiceDao(hibernateBundle.getSessionFactory()));
                return CachingAuthenticator.wrap(tokenBasedAuthenticator, CacheBuilderSpec.parse("maximumSize=10000, expireAfterAccess=1m"));
            }
        });

    }


}
