package de.bsd.quarkus.funq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Map;

/**
 *
 */
@Path("/tryout")
public class Tryout {

    public static final String INSTANT_MAIL = "instant_mail";
    @Inject
    TemplateProcessor tp;

    private String sampleParams = "{\"key1\":\"Value1\",\"key3\":\"value3\"}";

    @GET
    @Path("/")
    @Produces("text/html")
    @Transactional
    public String showUI(@QueryParam("bundle") String bundle,
                         @QueryParam("app") String app,
                         @QueryParam("et") String et,
                         @QueryParam("params") String params,
                         @QueryParam("template") String incomingTemplate,
                         @QueryParam("updateTemplate") String updateTemplate,
                         @Context UriInfo uriInfo) {

        String form;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("form.html")) {
            BufferedInputStream bis = new BufferedInputStream(is);
            byte[] formBytes = bis.readAllBytes();
            form = new String(formBytes);
        }
        catch (Exception e) {
            return "<h1>Reading form failed: " + e.getMessage() + "</h1>";
        }

        bundle = fillDefaultIfNeeded(bundle, "my-bundle");
        app = fillDefaultIfNeeded(app, "my-app");
        et = fillDefaultIfNeeded(et, "a_type");
        params = fillDefaultIfNeeded(params, sampleParams);
        form = fillFormValues(form, bundle,app,et);


        String template = tp.getTemplate(bundle,app,et, INSTANT_MAIL); // TODO don't hardcode type
        String escaped = escape(template);

        String trimmedTemplate = incomingTemplate != null ? incomingTemplate.trim() : "";
        if (updateTemplate!=null && updateTemplate.equals("on") && !trimmedTemplate.isBlank()) {
            tp.updateTemplate(bundle,app,et, INSTANT_MAIL, trimmedTemplate);
            // TODO print update message
            escaped = escape(trimmedTemplate);
            template = trimmedTemplate;
        }

        Map<String, Object> payload = null;
        try {
            payload = new ObjectMapper().readValue(params, Map.class);
        } catch (JsonProcessingException e) {
            return "Could not process parameters : " + e.getMessage() +
                    "<p/><hr/><p/>" + form;
        }

        String rendered = tp.renderTemplate(template, payload);

        if (params==null || params.isBlank()) {
            form = form.replaceAll("##PARAMS##", sampleParams);
        } else {
            form = form.replaceAll("##PARAMS##", params.trim());
        }
        form = form.replaceAll("##TEMPLATE##", escaped);

        return rendered +
                "<p/><hr/><p/>" +
                form;


    }

    private String fillDefaultIfNeeded(String what, String defVal) {
        if (what==null || what.isBlank()) {
            return defVal;
        }
        return what;
    }

    private String fillFormValues(String form, String bundle, String app, String et) {
        form = replaceOne(form, bundle, "##BUNDLE##");
        form = replaceOne(form, app, "##APP##" );
        form = replaceOne(form, et, "##ET##" );

        return form;
    }

    private String replaceOne(String form, String what, String placeholder) {
            return form.replaceAll(placeholder, what);
    }

    private String escape(String in) {
        return in.replaceAll("<", "&lt;");
    }


}
