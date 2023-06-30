package gov.rfb.nfeouro.enums;

public enum AmbienteEnum {
    HOMOLOGACAO("https://hom-nfoe.estaleiro.serpro.gov.br/API"),
    PRODUCAO("https://nfeouro.rfb.gov.br/API");

    AmbienteEnum(String valor) {
        url = valor;
    }

    public final String url;
}