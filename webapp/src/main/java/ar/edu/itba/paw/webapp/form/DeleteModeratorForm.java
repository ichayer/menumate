package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.NotNull;

public class DeleteModeratorForm {
    @NotNull
    private Long userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}