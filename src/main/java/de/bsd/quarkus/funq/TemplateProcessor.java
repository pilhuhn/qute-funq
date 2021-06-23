package de.bsd.quarkus.funq;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;

@ApplicationScoped
public class TemplateProcessor {
    @Inject
    Engine engine;


    String render(String bundle, String app, String event_type, Map<String, Object> payload) {
        // Get the template code from the DB
        String tmpl = getTemplate(bundle, app, event_type, "instant_mail");

        String result = renderTemplate(tmpl, payload);
        return result;
    }

    String renderTemplate(String tmpl, Map<String, Object> payload) {
        // Parse it
        Template t = engine.parse(tmpl);

        // Instantiate it with the payload of the Notification's action
        TemplateInstance ti = t.data(payload);

        // Render the template with the values
        String result = ti.render();
        return result;
    }

    String getTemplate(String bundle, String app, String event_type, String type) {

        String bae = bundle + ":" + app + ":" + event_type;

        PanacheQuery<QTemplate> qt = QTemplate.find("bae = :bae AND type = :type AND subtype = :subtype",
                Parameters.with("bae", bae)
                        .and("type", type)
                        .and("subtype", "body") // TODO don't hardcode
        );

        QTemplate qTemplate = qt.firstResult();

        if (qTemplate == null )
            return "- NO TEMPLATE FOUND -";

        return qTemplate.body;

    }
}
