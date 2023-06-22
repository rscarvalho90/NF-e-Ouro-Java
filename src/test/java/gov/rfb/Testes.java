package gov.rfb;

import gov.rfb.nfeouro.enums.AmbienteEnum;
import gov.rfb.nfeouro.model.NotaOuroCliente;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public class Testes {

    @Test
    public void importaCerificadoTeste() throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException, ParserConfigurationException, SAXException, XPathExpressionException, TransformerException, InvalidAlgorithmParameterException, MarshalException, XMLSignatureException, KeyException {
        NotaOuroCliente notaOuroCliente = new NotaOuroCliente(AmbienteEnum.HOMOLOGACAO, "/home/rafael/Projetos/NF-e Ouro Java/src/main/resources/certificados_homologacao/Cert_03763656000154.p12",
                                                            "senha1");

        notaOuroCliente.enviaDao("/home/rafael/Projetos/NF-e Ouro Java/src/test/resources/exemplos/primeiraAquisicao_03.xml");
    }

    @Test
    public void consultaPorNsuTeste() throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException, ParserConfigurationException, SAXException, XPathExpressionException, TransformerException, InvalidAlgorithmParameterException, MarshalException, XMLSignatureException, KeyException {
        NotaOuroCliente notaOuroCliente = new NotaOuroCliente(AmbienteEnum.HOMOLOGACAO, "/home/rafael/Projetos/NF-e Ouro Java/src/main/resources/certificados_homologacao/Cert_03763656000154.p12",
                "senha1");

        notaOuroCliente.consultaPorNsu(10);
    }

    @Test
    public void consultaPorChaveTeste() throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException, ParserConfigurationException, SAXException, XPathExpressionException, TransformerException, InvalidAlgorithmParameterException, MarshalException, XMLSignatureException, KeyException {
        NotaOuroCliente notaOuroCliente = new NotaOuroCliente(AmbienteEnum.HOMOLOGACAO, "/home/rafael/Projetos/NF-e Ouro Java/src/main/resources/certificados_homologacao/Cert_03763656000154.p12",
                "senha1");

        notaOuroCliente.consultaPorChave("3106200037636560001540010001770000000106");
    }
}
