package io.robe.admin.resources;

import com.google.inject.Inject;
import io.dropwizard.hibernate.UnitOfWork;
import io.robe.admin.hibernate.dao.LanguageDao;
import io.robe.admin.hibernate.entity.Language;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("language")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LanguageResource {

    @Inject
    LanguageDao languageDao;

    @GET
    @Path("/all")
    @UnitOfWork(readOnly = true, cacheMode = CacheMode.GET,flushMode = FlushMode.MANUAL)
    public List<Language> getAll() {
        return languageDao.findAll(Language.class);
    }


}
