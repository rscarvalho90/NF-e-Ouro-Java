package gov.rfb.nfeouro.enums;

public enum MetodoHttpEnum {
    GET("GET"), POST("POST"), PUT("PUT"), DELETE("DELETE");

    MetodoHttpEnum(String metodo) {
        this.metodo = metodo;
    }

    public String metodo;
}
