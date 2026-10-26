package br.com.authentication.domain.model;

/**
 * Type of the value of a {@link Claim}. Defines how the stored text is validated and written into the JWT.
 */
public enum ClaimValueType {
    STRING {
        @Override
        public Object parse(String value) {
            return value;
        }
    },
    INTEGER {
        @Override
        public Object parse(String value) {
            try {
                return Long.valueOf(value.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Value '" + value + "' is not a valid INTEGER");
            }
        }
    },
    BOOLEAN {
        @Override
        public Object parse(String value) {
            String normalized = value.trim().toLowerCase();
            if (normalized.equals("true")) {
                return Boolean.TRUE;
            }
            if (normalized.equals("false")) {
                return Boolean.FALSE;
            }
            throw new IllegalArgumentException("Value '" + value + "' is not a valid BOOLEAN (use true or false)");
        }
    };

    /** Converts the stored text into the typed value; throws {@link IllegalArgumentException} when invalid. */
    public abstract Object parse(String value);
}
