package com.skrybe.sdk.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.skrybe.sdk.internal.Errors;
import com.skrybe.sdk.internal.Fields;
import com.skrybe.sdk.internal.Parse;
import com.skrybe.sdk.internal.Transport;
import com.skrybe.sdk.model.Template;
import com.skrybe.sdk.params.TemplateParams;

import java.util.ArrayList;
import java.util.List;

/** Email templates. */
public final class Templates {
    private final Transport transport;

    /**
     * Internal: get one from {@code Skrybe.templates()}.
     *
     * @param transport the client's transport
     */
    public Templates(Transport transport) {
        this.transport = transport;
    }

    /** @return the brand's templates by name, without their HTML */
    public List<Template> list() {
        String text = transport.text("api/templates/get-templates.php", new Fields(), true);
        JsonNode rows = Parse.jsonObject(text, "templates").path("templates");
        List<Template> out = new ArrayList<>();
        for (JsonNode row : rows) {
            out.add(Parse.bind(row, "templates", text, Template.class));
        }
        return out;
    }

    /** @return the template, without its content */
    public Template get(int templateId) {
        return get(templateId, false);
    }

    /** @return the template, with its HTML and plain text when includeContent is true */
    public Template get(int templateId, boolean includeContent) {
        String text = transport.text("api/templates/get-template.php", new Fields()
                .with("template_id", templateId)
                .with("include_content", includeContent ? "yes" : null), true);
        return Parse.jsonObject(text, "template", Template.class);
    }

    /** Creates a template; name and htmlText are required. Not retried: a repeat would create a second template. */
    public Template create(TemplateParams fields) {
        Errors.requireParams("fields", fields);
        Errors.requireParams("name", fields.getName(), "htmlText", fields.getHtmlText());
        String text = transport.text("api/templates/create-template.php", fieldsBody(new Fields(), fields), false);
        return Parse.jsonObject(text, "template", Template.class);
    }

    /** Only the fields set change. */
    public Template update(int templateId, TemplateParams fields) {
        Errors.requireParams("fields", fields);
        String text = transport.text("api/templates/update-template.php",
                fieldsBody(new Fields().with("template_id", templateId), fields), false);
        return Parse.jsonObject(text, "template", Template.class);
    }

    /** Deletes the template. Campaigns already made from it are unaffected. */
    public void delete(int templateId) {
        String text = transport.text("api/templates/delete-template.php", new Fields().with("template_id", templateId), false);
        Parse.jsonObject(text, "delete result");
    }

    private static Fields fieldsBody(Fields body, TemplateParams fields) {
        return body
                .with("name", fields.getName())
                .with("html_text", fields.getHtmlText())
                .with("plain_text", fields.getPlainText())
                .with("from_name", fields.getFromName())
                .with("from_email", fields.getFromEmail())
                .with("reply_to", fields.getReplyTo())
                .with("editor", fields.getEditor());
    }
}
