package io.robe.admin;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import io.dropwizard.Application;
import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import io.dropwizard.views.ViewRenderer;
import io.dropwizard.views.freemarker.FreemarkerViewRenderer;
import io.robe.admin.cli.InitializeCommand;
import io.robe.admin.guice.module.AuthenticatorModule;
import io.robe.admin.guice.module.HibernateModule;
import io.robe.admin.quartz.hibernate.HibernateJobProvider;
import io.robe.assets.AdvancedAssetBundle;
import io.robe.auth.tokenbased.TokenBasedAuthBundle;
import io.robe.common.exception.RobeExceptionMapper;
import io.robe.guice.GuiceBundle;
import io.robe.hibernate.HibernateBundle;
import io.robe.mail.MailBundle;
import io.robe.quartz.QuartzBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;


/**
 * Default io.robe.admin class of Robe.
 * If you extend this class on your applications io.robe.admin class and call super methods at
 * overridden methods you will still benefit of robe souse.
 */
public class RobeApplication<T extends RobeServiceConfiguration> extends Application<T> {


    private static final Logger LOGGER = LoggerFactory.getLogger(RobeApplication.class);
    private boolean withServerCommand = false;

    public static void main(String[] args) throws Exception {
        RobeApplication application = new RobeApplication();
        if (args.length > 1 && args[0].equals("server"))
            application.setWithServerCommand(true);
        application.run(args);
    }

    public void setWithServerCommand(boolean withServerCommand) {
        this.withServerCommand = withServerCommand;
    }


    /**
     * Adds
     * Hibernate bundle for PROVIDER connection
     * Asset bundle for io.robe.admin screens and
     * Class scanners for
     * <ul>
     * <li>Entities</li>
     * <li>HealthChecks</li>
     * <li>Providers</li>
     * <li>InjectableProviders</li>
     * <li>Resources</li>
     * <li>Tasks</li>
     * <li>Managed objects</li>
     * </ul>
     *
     * @param bootstrap
     */
    @Override
    public void initialize(Bootstrap<T> bootstrap) {

        HibernateBundle<T> hibernateBundle = new HibernateBundle<T>();

        TokenBasedAuthBundle<T> authBundle = new TokenBasedAuthBundle<T>();

        addGuiceBundle(bootstrap, authBundle, hibernateBundle);

        bootstrap.addBundle(hibernateBundle);
        bootstrap.addCommand(new InitializeCommand(this, hibernateBundle));
        if (withServerCommand) {
            //TODO: Find a better way to send it
            HibernateJobProvider.setHibernateBundle(hibernateBundle);

            bootstrap.addBundle(authBundle);
            bootstrap.addBundle(new QuartzBundle<T>());
            bootstrap.addBundle(new ViewBundle());
            bootstrap.addBundle(new ViewBundle(ImmutableList.<ViewRenderer>of(new FreemarkerViewRenderer())));
            bootstrap.addBundle(new MailBundle<T>());
            bootstrap.addBundle(new AdvancedAssetBundle<T>());
        }

    }

    private void addGuiceBundle(Bootstrap<T> bootstrap, TokenBasedAuthBundle<T> authBundle, HibernateBundle hibernateBundle) {
        List<Module> modules = new LinkedList<Module>();
        modules.add(new HibernateModule(hibernateBundle));
        if (withServerCommand) {
            modules.add(new AuthenticatorModule(authBundle, bootstrap.getMetricRegistry()));
        }
        bootstrap.addBundle(new GuiceBundle<T>(modules, bootstrap.getApplication().getConfigurationClass()));
    }


    /**
     * {@inheritDoc}
     * In addition adds exception mapper.
     *
     * @param configuration
     * @param environment
     * @throws Exception
     */
    @UnitOfWork
    @Override
    public void run(T configuration, Environment environment) throws Exception {
        addExceptionMappers(environment);
    }

    private void addExceptionMappers(Environment environment) {
        environment.jersey().register(new RobeExceptionMapper());

    }

}
