package gov.rfb.nfeouro.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gov.rfb.nfeouro.enums.AmbienteEnum;
import gov.rfb.nfeouro.enums.MetodoHttpEnum;
import gov.rfb.nfeouro.util.CertificadosUtil;
import gov.rfb.nfeouro.util.HttpsUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.zip.GZIPOutputStream;

public class NotaOuroCliente {

    private final String pathCertificado;
    private final String senhaCertificado;
    private final AmbienteEnum ambiente;

    /**
     *
     * @param ambiente Ambiente em que o serviço será executado.
     * @param pathCertificado Local, na estação de execução do serviço, em que encontra-se o certificado para assinatura do XML.
     * @param senhaCertificado Senha do arquivo do certificado.
     */
    public NotaOuroCliente(AmbienteEnum ambiente, String pathCertificado, String senhaCertificado) {
        this.ambiente = ambiente;
        this.pathCertificado = pathCertificado;
        this.senhaCertificado = senhaCertificado;
    }

    /**
     * Envia um XML contendo uma DAO (Declaração de Aquisição de Ouro ativo financeiro).
     *
     * @param xmlPath Path (local, caminho) do arquivo XML a ser enviado.
     * @return Resposta do servidor à requisição.
     * @throws CertificateException
     * @throws KeyStoreException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws XPathExpressionException
     * @throws TransformerException
     * @throws InvalidAlgorithmParameterException
     * @throws MarshalException
     * @throws XMLSignatureException
     * @throws KeyException
     */
    public String enviaDao(String xmlPath) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException, ParserConfigurationException, SAXException, XPathExpressionException, TransformerException, InvalidAlgorithmParameterException, MarshalException, XMLSignatureException, KeyException {
        // Assina o XML
        final String xml = assinaXml(xmlPath);

        // Compacta o XML assinado em formato GZIP
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(obj);
        gzip.write(xml.getBytes(StandardCharsets.UTF_8));
        gzip.close();

        // Converte o GZIP contendo o XML para base64
        String xmlGzipBase64 = Base64.getEncoder().encodeToString(obj.toByteArray());

        // Envia o xml assinado para o serviço
        String corpoRequisicao = "{\"XmlGzipDao\": \"" + xmlGzipBase64 + "\"}";
        final String resposta = new HttpsUtil(this.pathCertificado, this.senhaCertificado).doHttpsRequest(this.ambiente.url + "/nfeouro", MetodoHttpEnum.POST, cabecalhosPadrao(), corpoRequisicao, true);

        return resposta;
    }

    /**
     * Consulta um XML contendo uma NF-e Ouro e seu DAO (Declaração de Aquisição de Ouro ativo financeiro).
     *
     * @param nsuRecepcao NSU da NF-e Ouro.
     * @return Resposta do servidor à requisição.
     * @throws UnrecoverableKeyException
     * @throws CertificateException
     * @throws IOException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public String consultaPorNsu(int nsuRecepcao) throws UnrecoverableKeyException, CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        final String resposta = new HttpsUtil(this.pathCertificado, this.senhaCertificado).doHttpsRequest(this.ambiente.url + "/nsu/" + nsuRecepcao + "/nfeouro", MetodoHttpEnum.GET, cabecalhosPadrao(), null, true);

        return resposta;
    }

    /**
     * Consulta um XML contendo uma NF-e Ouro e seu DAO (Declaração de Aquisição de Ouro ativo financeiro).
     *
     * @param chaveAcesso Chave de acesso da NF-e Ouro.
     * @return Resposta do servidor à requisição.
     * @throws UnrecoverableKeyException
     * @throws CertificateException
     * @throws IOException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public String consultaPorChave(String chaveAcesso) throws UnrecoverableKeyException, CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        final String resposta = new HttpsUtil(this.pathCertificado, this.senhaCertificado).doHttpsRequest(this.ambiente.url + "/nfeouro/" + chaveAcesso, MetodoHttpEnum.GET, cabecalhosPadrao(), null, true);

        return resposta;
    }

    /**
     * Assina um XML com certificado do tipo A1.
     *
     * @param xmlPath Path (local, caminho) do arquivo XML a ser enviado.
     * @return XML, em formato String, assinado.
     * @throws CertificateException
     * @throws KeyStoreException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws UnrecoverableKeyException
     * @throws XPathExpressionException
     * @throws TransformerException
     * @throws InvalidAlgorithmParameterException
     * @throws KeyException
     * @throws MarshalException
     * @throws XMLSignatureException
     */
    private String assinaXml(String xmlPath) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, ParserConfigurationException, SAXException, UnrecoverableKeyException, XPathExpressionException, TransformerException, InvalidAlgorithmParameterException, KeyException, MarshalException, XMLSignatureException {
        // Importa um certificado tipo A1
        final KeyStore keyStore = new CertificadosUtil().importaCertificados(this.pathCertificado, this.senhaCertificado);

        // Abre XML a ser assinado
        final FileInputStream xmlInputStream = new FileInputStream(xmlPath);
        String xml = new String(xmlInputStream.readAllBytes());
        xmlInputStream.close();
        xml = configuraXml(xml);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document documento = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));

        // Localiza a URI de referência
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile("//*[local-name(.)='infDAO']/@Id");
        final String referencia = "#" + expr.evaluate(documento);

        // Assina o XML
        XMLSignatureFactory sf = XMLSignatureFactory.getInstance("DOM");
        NodeList elementos = documento.getElementsByTagName("infDAO");
        Element elemento = (Element) elementos.item(0);
        elemento.setIdAttribute("Id", true);
        List<Transform> transformsList = new ArrayList<>();
        transformsList.add(sf.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null));
        transformsList.add(sf.newTransform("http://www.w3.org/TR/2001/REC-xml-c14n-20010315", (TransformParameterSpec) null));
        Reference ref = sf.newReference(referencia, sf.newDigestMethod(DigestMethod.SHA1, null), transformsList, null,
                null);
        final String alias = keyStore.aliases().nextElement();
        final Key chavePrivada = keyStore.getKey(alias, this.senhaCertificado.toCharArray());
        final X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
        KeyInfoFactory kif = sf.getKeyInfoFactory();
        List<X509Certificate> x509Content = new ArrayList<>();
        x509Content.add(cert);
        X509Data x509Data = kif.newX509Data(x509Content);
        KeyInfo ki = kif.newKeyInfo(Collections.singletonList(x509Data));
        SignedInfo si = sf.newSignedInfo(
                sf.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
                sf.newSignatureMethod(SignatureMethod.RSA_SHA1, null), Collections.singletonList(ref));
        XMLSignature assinador = sf.newXMLSignature(si, ki);

        DOMSignContext dsc = new DOMSignContext(chavePrivada, documento.getDocumentElement());

        dsc.setBaseURI(referencia);

        assinador.sign(dsc);

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        trans.transform(new DOMSource(documento), new StreamResult(os));
        xml = finalizaXml(os.toString()).replaceAll(" standalone=\"no\"", "").replaceAll("&#13;", "");

        return xml;
    }

    /**
     * Finaliza o XML já assinado para envio pelo serviço.
     * @param xmlTxt XML, em formato String, a ser formatado.
     * @return XML, em formato String, pronto para envio pelo serviço.
     */
    private String finalizaXml(String xmlTxt) {
        xmlTxt = xmlTxt.replaceAll(" standalone=\"no\"", "");
        xmlTxt = xmlTxt.replaceAll("&#13;", "");
        xmlTxt = xmlTxt.replaceAll("\r", "");
        xmlTxt = xmlTxt.replaceAll("\n", "");
        xmlTxt = xmlTxt.replaceAll(System.lineSeparator(), "");
        xmlTxt = xmlTxt.replaceAll("\t", "");

        return xmlTxt;
    }

    /**
     * Configura o XML antes da assinatura.
     * @param xmlTxt XML, em formato String, a ser configurado.
     * @return XML, em formato String, para ser assinado.
     */
    private String configuraXml(String xmlTxt) {
        xmlTxt = xmlTxt.replaceAll("\r", "");
        xmlTxt = xmlTxt.replaceAll("\n", "");
        xmlTxt = xmlTxt.replaceAll(System.lineSeparator(), "");
        xmlTxt = xmlTxt.replaceAll("\t", "");

        return xmlTxt;
    }

    /**
     * @return Retorna o IP (público) atual do cliente.
     * @throws IOException
     * @throws UnrecoverableKeyException
     * @throws CertificateException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    private String getIp() throws IOException, UnrecoverableKeyException, CertificateException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        final String resposta = new HttpsUtil().doHttpsRequest("https://api.myip.com", MetodoHttpEnum.GET, null, null, false);

        return new ObjectMapper().readValue(resposta.toString(), ObjectNode.class).get("ip").asText();
    }

    /**
     * @return Retorna os cabeçalhos-padrão para as requisições ao serviço.
     * @throws IOException
     * @throws CertificateException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     * @throws KeyManagementException
     */
    private Map<String, String> cabecalhosPadrao() throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
        String ip = getIp();

        // Acessa o certificado do usuário para enviar como cabeçalho da requisição
        final KeyStore certificados = new CertificadosUtil().importaCertificados(this.pathCertificado, this.senhaCertificado);
        final Certificate certificado = certificados.getCertificate(certificados.aliases().nextElement());
        String certificadoBase64 = "-----BEGIN CERTIFICATE-----\n" + Base64.getEncoder().encodeToString(certificado.getEncoded()) + "\n-----END CERTIFICATE-----";
        certificadoBase64 = Base64.getEncoder().encodeToString(certificadoBase64.getBytes(StandardCharsets.UTF_8));

        final HashMap<String, String> cabecalhosPadrao = new HashMap<>();

        cabecalhosPadrao.put("X-SSL-Client-Cert", certificadoBase64);
        cabecalhosPadrao.put("X-ARR-ClientCert", certificadoBase64);
        cabecalhosPadrao.put("X-Forwarded-For", ip);
        cabecalhosPadrao.put("Content-Type", "application/json");

        return cabecalhosPadrao;
    }
}