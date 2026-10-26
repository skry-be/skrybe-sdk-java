package com.skrybe.sdk.model;

import java.util.Collections;
import java.util.List;

/**
 * One autoresponder with its emails, from {@code autoresponders().get()}.
 */
public class AutoresponderWithEmails extends Autoresponder {
    private List<AutoresponderEmail> emailList;

    AutoresponderWithEmails() {
    }

    /** @return its emails */
    public List<AutoresponderEmail> getEmailList() {
        return emailList == null ? Collections.emptyList() : Collections.unmodifiableList(emailList);
    }
}
