package de.bsd.quarkus.funq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.funqy.Context;
import io.quarkus.funqy.Funq;
import io.quarkus.funqy.knative.events.CloudEvent;
import io.quarkus.funqy.knative.events.CloudEventMapping;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

public class Rewrite {

    @Inject
    Engine engine;

    @Funq
    @CloudEventMapping(trigger = "notification", responseSource = "qute-func", responseType = "email")
//    @CloudEventMapping(trigger = "com.redhat.cloud.notification", responseSource = "qute-func", responseType = "email")
    // TODO Figure out how to use the avro Action object from notification-backend
    //      This probably needs some special Jackson configuration
    //      See also rewrite-funq project
    public String process(Map<String,Object> input, @Context CloudEvent eventInfo) throws JsonProcessingException {

        System.out.println("got >>" + input + "<<");
        System.out.println("With context >> " + eventInfo);

        String bundle = (String) input.get("bundle");
        String app = (String) input.get("application");
        String event_type = (String) input.get("event_type");
        String time = (String) input.get("timestamp");

        // eventInfo.extensions().get("rhaccount")  <- get at ce-rhaccount extension header

        List events = (List) input.get("events");
        Map<String, Object> tmpMap = (Map<String, Object>) events.get(0);
        String jsonPayload = (String) tmpMap.get("payload");
        Map<String, Object> payload = new ObjectMapper().readValue(jsonPayload, Map.class);

        // Get the template code from the DB
        String tmpl = getTemplate(bundle, app, event_type, "instant_mail");

        // Parse it
        Template t = engine.parse(tmpl);

        // Instantiate it with the payload of the Notification's action
        TemplateInstance ti = t.data(payload);

        // Render the template with the values
        String result = ti.render();


        return result;
    }

    private String getTemplate(String bundle, String app, String event_type, String type) {

        String bae = bundle + ":" + app + ":" + event_type;

        PanacheQuery<QTemplate> qt = QTemplate.find("bae = :bae AND type = :type AND subtype = :subtype",
                Parameters.with("bae", bae)
                        .and("type", type)
                        .and("subtype","body") // TODO don't hardcode
        );

        QTemplate qTemplate = qt.firstResult();

        return qTemplate.body;

    }
}
