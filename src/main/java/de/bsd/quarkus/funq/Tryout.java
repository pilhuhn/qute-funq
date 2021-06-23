package de.bsd.quarkus.funq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Map;

/**
 *
 */
@Path("/tryout")
public class Tryout {

    @Inject
    TemplateProcessor tp;

    private String sampleParams = "{\"key1\":\"Value1\",\"key3\":\"value3\"}";

    @GET
    @Path("/")
    @Produces("text/html")
    public String showUI(@QueryParam("bundle") String bundle,
                         @QueryParam("app") String app,
                         @QueryParam("et") String et,
                         @QueryParam("params") String params) {

        String form;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("form.html")) {
            BufferedInputStream bis = new BufferedInputStream(is);
            byte[] formBytes = bis.readAllBytes();
            form = new String(formBytes);
        }
        catch (Exception e) {
            return "<h1>Reading form failed: " + e.getMessage() + "</h1>";
        }


        if (bundle!=null && app != null && et != null) {

            String template = tp.getTemplate(bundle,app,et,"instant_mail"); // TODO don't hardcode type
            String escaped = escape(template);

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

            return rendered +
                    "<p/><hr/><p/>" +
                    escaped +
                    "<p/><hr/><p/>" +
                    form;
        }

    return form;


    }

    private String escape(String in) {
        return in.replaceAll("<", "&lt;");
    }


}
