package com.castelodostorres.sistema.modelo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RespostaLogin {

    private String session;
    private String error;
    private Integer code;

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public boolean hasSession() {
        return session != null && !session.isBlank();
    }

    public boolean hasError() {
        return error != null && !error.isBlank();
    }
}