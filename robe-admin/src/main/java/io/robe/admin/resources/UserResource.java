package io.robe.admin.resources;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;
import io.robe.admin.dto.UserDTO;
import io.robe.admin.hibernate.dao.MailTemplateDao;
import io.robe.admin.hibernate.dao.RoleDao;
import io.robe.admin.hibernate.dao.TicketDao;
import io.robe.admin.hibernate.dao.UserDao;
import io.robe.admin.hibernate.entity.MailTemplate;
import io.robe.admin.hibernate.entity.Role;
import io.robe.admin.hibernate.entity.Ticket;
import io.robe.admin.hibernate.entity.User;
import io.robe.admin.util.ExceptionMessages;
import io.robe.auth.Credentials;
import io.robe.common.exception.RobeRuntimeException;
import io.robe.mail.MailItem;
import io.robe.mail.MailManager;
import org.hibernate.FlushMode;
import org.joda.time.DateTime;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

import static org.hibernate.CacheMode.GET;

@Path("user")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {


    public static final String E_MAIL = "E-MAIL";
    public static final String TICKET = "TICKET";
    public static final String TEMPLATES_PATH = "/templates/";

    @Inject
    UserDao userDao;
    @Inject
    RoleDao roleDao;

    @Inject
    TicketDao ticketDao;

    @Inject
    MailTemplateDao mailTemplateDao;

    @Path("all")
    @GET
    @UnitOfWork(readOnly = true, cacheMode = GET, flushMode = FlushMode.MANUAL)
    public List<UserDTO> getUsers(@Auth Credentials credentials) {
        List<User> entities = userDao.findAll(User.class);
        List<UserDTO> users = new LinkedList<UserDTO>();
        for (User entity : entities) {
            UserDTO user = new UserDTO(entity);
            users.add(user);
        }
        return users;
    }

    @GET
    @UnitOfWork(readOnly = true, cacheMode = GET, flushMode = FlushMode.MANUAL)
    @Path("{userId}")
    public UserDTO get(@Auth Credentials credentials, @PathParam("userId") String id) {
        return new UserDTO(userDao.findById(id));
    }

    @GET
    @UnitOfWork(readOnly = true, cacheMode = GET, flushMode = FlushMode.MANUAL)
    @Path("profile")
    public UserDTO getByEmail(@Auth Credentials credentials) {
        User user = userDao.findByUsername(credentials.getUsername()).get();
        return new UserDTO(user);
    }

    @PUT
    @UnitOfWork
    public UserDTO create(@Auth Credentials credentials, @Valid UserDTO user, @Context UriInfo uriInfo) {
        Optional<User> checkUser = userDao.findByUsername(user.getEmail());
        if (checkUser.isPresent()) {
            throw new RobeRuntimeException(E_MAIL, user.getEmail() + " already used by another user. Please use different e-mail.");
        }
        User entity = new User();
        entity.setEmail(user.getEmail());
        entity.setName(user.getName());
        entity.setSurname(user.getSurname());
        entity.setActive(user.isActive());
        Role role = roleDao.findById(user.getRoleOid());
        if (role == null) {
            throw new RobeRuntimeException("Role", user.getEmail() + ExceptionMessages.CANT_BE_NULL.toString());
        }
        entity.setRole(role);
        entity.setPassword(UUID.randomUUID().toString());

        entity = userDao.create(entity);


        Ticket ticket = new Ticket();
        ticket.setType(Ticket.Type.CHANGE_PASSWORD);
        ticket.setUser(entity);
        DateTime expire = DateTime.now().plusDays(5);
        ticket.setExpirationDate(expire.toDate());
        ticket = ticketDao.create(ticket);
        String url = uriInfo.getBaseUri().toString();
        String ticketUrl = url + "ticket/" + ticket.getOid();

        MailItem mailItem = new MailItem();

        Optional<MailTemplate> mailTemplateOptional = mailTemplateDao.findByCode(Ticket.Type.CHANGE_PASSWORD.name());
        Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        Template template = null;
        Map<String, Object> parameter = new HashMap<String, Object>();
        Writer out = new StringWriter();

        if (mailTemplateOptional.isPresent()) {
            String body = mailTemplateOptional.get().getTemplate();
            try {
                template = new Template("robeTemplate", body, cfg);
                parameter.put("name", entity.getName());
                parameter.put("surname", entity.getSurname());
            } catch (IOException e) {
                throw new RobeRuntimeException("ERROR", e);
            }

        } else {
            cfg.setClassForTemplateLoading(this.getClass(), TEMPLATES_PATH);
            try {
                template = cfg.getTemplate("ChangePasswordMail.ftl");
            } catch (IOException e) {
                throw new RobeRuntimeException(E_MAIL, e);
            }

        }

        try {
            parameter.put("ticketUrl", ticketUrl);
            if (template != null) {
                template.process(parameter, out);
            } else {
                throw new RobeRuntimeException(E_MAIL, "ChangePasswordMail template not found");
            }
        } catch (TemplateException e) {
            throw new RobeRuntimeException(E_MAIL, e);
        } catch (IOException e) {
            throw new RobeRuntimeException("ERROR", e);
        }

        mailItem.setBody(out.toString());
        mailItem.setReceivers(entity.getUsername());
        mailItem.setTitle("Robe.io Password Change Request");
        MailManager.sendMail(mailItem);

        return new UserDTO(entity);

    }

    @POST
    @UnitOfWork
    public UserDTO update(@Auth Credentials credentials, @Valid UserDTO user) {
        // Get and check user
        User entity = userDao.findById(user.getOid());
        if (entity == null) {
            throw new RobeRuntimeException("User", user.getOid() + ExceptionMessages.NOT_EXISTS.toString());
        }
        //Get role and firm and check for null
        Role role = roleDao.findById(user.getRoleOid());
        if (role == null) {
            throw new RobeRuntimeException("Role", user.getEmail() + ExceptionMessages.CANT_BE_NULL.toString());
        }
        entity.setOid(user.getOid());
        entity.setEmail(user.getEmail());
        entity.setName(user.getName());
        entity.setSurname(user.getSurname());
        entity.setActive(user.isActive());
        entity.setRole(role);

        entity = userDao.update(entity);
        userDao.flush();

        return new UserDTO(entity);

    }


    @POST
    @Path("updatePassword")
    @Consumes
    @UnitOfWork
    public UserDTO updatePassword(@Auth Credentials credentials,
                                  @FormParam("newPassword") String newPassword,
                                  @FormParam("oldPassword") String oldPassword) {

        User user = userDao.findByUsername(credentials.getUsername()).get();

        String oPassword = user.getPassword();
        if (!(oPassword.equals(oldPassword))) {
            throw new RobeRuntimeException("Old Password Error", "Your old password is not correct");
        }

        user.setPassword(newPassword);
        userDao.update(user);

        return new UserDTO(user);
    }

    @DELETE
    @UnitOfWork
    public UserDTO delete(@Auth Credentials credentials, UserDTO user) {
        User entity = userDao.findById(user.getOid());
        userDao.delete(entity);
        return user;
    }

    @POST
    @Path("emailRequest")
    @Consumes
    @UnitOfWork
    public User createUserByEmailRequest(@Auth Credentials credentials, @FormParam("email") String email, @FormParam("roleOid") String roleOid, @Context UriInfo uriInfo) {

        Optional<User> checkUser = userDao.findByUsername(email);
        if (checkUser.isPresent()) {
            throw new RobeRuntimeException(E_MAIL, checkUser.get().getEmail() + " already used by another user. Please use different e-mail.");
        }

        User entity = new User();
        entity.setEmail(email);
        entity.setName(email);
        entity.setSurname(email);
        entity.setActive(false);
        entity.setPassword(UUID.randomUUID().toString());
        Role role = roleDao.findById(roleOid);
        if (role == null) {
            throw new RobeRuntimeException("Role", "Role " + ExceptionMessages.CANT_BE_NULL.toString());
        }
        entity.setRole(role);
        entity = userDao.create(entity);

        Ticket ticket = new Ticket();
        ticket.setType(Ticket.Type.REGISTER);
        ticket.setUser(entity);
        DateTime expire = DateTime.now().plusDays(5);
        ticket.setExpirationDate(expire.toDate());
        ticket = ticketDao.create(ticket);
        String url = uriInfo.getBaseUri().toString();
        String ticketUrl = url + "ticket/" + ticket.getOid();

        MailItem mailItem = new MailItem();

        Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        cfg.setClassForTemplateLoading(this.getClass(), TEMPLATES_PATH);
        Map<String, Object> parameter = new HashMap<String, Object>();
        Writer out = new StringWriter();

        Optional<MailTemplate> mailTemplateOptional = mailTemplateDao.findByCode(Ticket.Type.REGISTER.name());
        Template template = null;


        if (mailTemplateOptional.isPresent()) {
            String body = mailTemplateOptional.get().getTemplate();
            try {
                template = new Template("robeTemplate", body, cfg);

            } catch (IOException e) {
                throw new RobeRuntimeException("ERROR", e.getLocalizedMessage());
            }

        } else {
            cfg.setClassForTemplateLoading(this.getClass(), TEMPLATES_PATH);
            try {
                template = cfg.getTemplate("RegisterMail.ftl");
            } catch (IOException e) {
                throw new RobeRuntimeException(E_MAIL, e);
            }
        }

        try {
            parameter.put("ticketUrl", ticketUrl);
            if (template != null) {
                template.process(parameter, out);
            } else {
                throw new RobeRuntimeException(E_MAIL, "ChangePasswordMail template not found");
            }
        } catch (TemplateException | IOException e) {
            throw new RobeRuntimeException("ERROR", e);
        }

        mailItem.setBody(out.toString());
        mailItem.setReceivers(email);
        mailItem.setTitle("Robe.io Registration Request");
        MailManager.sendMail(mailItem);

        return entity;
    }

    @POST
    @UnitOfWork
    @Path("registerByMail")
    public UserDTO registerUserByMail(@Valid UserDTO user) {

        Ticket ticket = ticketDao.findById(user.getTicket());
        if (ticket == null) {
            throw new RobeRuntimeException(TICKET, "Ticket not found");
        }

        if (ticket.getExpirationDate().getTime() < Calendar.getInstance().getTime().getTime()) {
            throw new RobeRuntimeException(TICKET, "Ticket date is expired");
        }
        User entity = ticket.getUser();
        if (!entity.getUsername().equals(user.getUsername())) {
            throw new RobeRuntimeException(TICKET, "Tickets not belong to your email");
        }
        entity.setName(user.getName());
        entity.setSurname(user.getSurname());
        entity.setPassword(user.getNewPassword());
        entity.setActive(true);

        ticket.setExpirationDate(DateTime.now().toDate());
        return user;

    }

    @POST
    @UnitOfWork
    @Path("registerPassword")
    public UserDTO registerUserPassword(@Valid UserDTO user) {

        Ticket ticket = ticketDao.findById(user.getTicket());
        if (ticket == null) {
            throw new RobeRuntimeException(TICKET, "Ticket not found");
        }
        if (ticket.getExpirationDate().getTime() < Calendar.getInstance().getTime().getTime()) {
            throw new RobeRuntimeException(TICKET, "Ticket date is expired");
        }
        User entity = ticket.getUser();
        if (!entity.getUsername().equals(user.getUsername())) {
            throw new RobeRuntimeException(TICKET, "Tickets not belong to your email");
        }
        entity.setPassword(user.getNewPassword());
        entity.setActive(true);

        userDao.update(entity);

        ticket.setExpirationDate(DateTime.now().toDate());
        ticketDao.update(ticket);

        return user;

    }


}
