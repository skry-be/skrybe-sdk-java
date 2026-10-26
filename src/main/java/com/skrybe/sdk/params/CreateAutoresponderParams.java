package com.skrybe.sdk.params;

/**
 * Parameters for {@code autoresponders().create()}. Required: name, type, listId.
 *
 * <p>Build with {@link #builder()}. Only the fields set are sent.
 */
public final class CreateAutoresponderParams {
    private final String name;
    private final AutoresponderType type;
    private final String listId;
    private final String customField;

    private CreateAutoresponderParams(Builder b) {
        this.name = b.name;
        this.type = b.type;
        this.listId = b.listId;
        this.customField = b.customField;
    }

    /** @return a new builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return the autoresponder's name, or null when not set */
    public String getName() {
        return name;
    }

    /** @return drip, annually or on_date, or null when not set */
    public AutoresponderType getType() {
        return type;
    }

    /** @return the encrypted list id, or null when not set */
    public String getListId() {
        return listId;
    }

    /** @return required for annually and on_date: a Date custom field of the list, one autoresponder per field, or null when not set */
    public String getCustomField() {
        return customField;
    }

    /** Builds a {@link CreateAutoresponderParams}. */
    public static final class Builder {
        private String name;
        private AutoresponderType type;
        private String listId;
        private String customField;

        private Builder() {
        }

        /**
         * The autoresponder's name. Required.
         *
         * @return this builder
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Drip, annually or on_date. Required.
         *
         * @return this builder
         */
        public Builder type(AutoresponderType type) {
            this.type = type;
            return this;
        }

        /**
         * The encrypted list id. A list can have one drip autoresponder. Required.
         *
         * @return this builder
         */
        public Builder listId(String listId) {
            this.listId = listId;
            return this;
        }

        /**
         * Required for annually and on_date: a Date custom field of the list, one autoresponder per field.
         *
         * @return this builder
         */
        public Builder customField(String customField) {
            this.customField = customField;
            return this;
        }

        /** @return the parameters */
        public CreateAutoresponderParams build() {
            return new CreateAutoresponderParams(this);
        }
    }
}
