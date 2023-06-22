package gov.rfb.nfeouro.enums;

public enum AmbienteEnum {
    HOMOLOGACAO("https://hom-nfoe.estaleiro.serpro.gov.br/API");

    AmbienteEnum(String valor) {
        url = valor;
    }

    public String url;
}