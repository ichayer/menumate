package ar.edu.itba.paw.model;

public enum OrderStatus {
    ORDERED("ordered"),
    REJECTED("rejected"),
    CANCELLED("cancelled"),
    CONFIRMED("confirmed"),
    READY("ready"),
    DELIVERED("delivered");

    private final String messageCode;

    OrderStatus(final String messageCode) {
        this.messageCode = messageCode;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public boolean happensBefore(OrderStatus status) {
        return this.ordinal() < status.ordinal();
    }

    public boolean happensBeforeOrIs(OrderStatus status) {
        return this.ordinal() <= status.ordinal();
    }
}
