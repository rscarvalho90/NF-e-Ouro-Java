package gov.rfb.nfeouro.util;

import gov.rfb.nfeouro.enums.MetodoHttpEnum;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

public class HttpsUtil {

    private String pathCertificado, senhaCertificado;

    /**
     * Utilizar este construtor caso deseje realizar uma conexão em que o cliente também se autentica via certificado digital (Two-Way SSL).
     *
     * @param pathCertificado Local, na estação de execução do serviço, em que encontra-se o certificado para assinatura do XML.
     * @param senhaCertificado Senha do arquivo do certificado.
     */
    public HttpsUtil(String pathCertificado, String senhaCertificado) {
        this.pathCertificado = pathCertificado;
        this.senhaCertificado = senhaCertificado;
    }

    public HttpsUtil() {
    }

    /**
     * Realiza uma chamada HTTPS.
     *
     * @param urlStr URL a ser chamada.
     * @param metodo Método HTTP a ser utilizado.
     * @param headers Map contendo cabeçalhos da requisição, em que a chave é o nome do campo e o valor é o valor do campo.
     *                Enviar 'null' caso não haja cabeçalhos a serem enviados.
     * @param corpoRequisicao Corpo da requisição HTTP a ser enviado. Enviar 'null' caso não haja corpo a ser enviado.
     * @param insereCertificado Deve ser realizada uma conexão de confiança mútua (Two-Way SSL)? caso positivo,
     *                          os atributos {@link HttpsUtil#pathCertificado} e {@link HttpsUtil#senhaCertificado}
     *                          devem ser informados no objeto.
     *
     * @return Conteúdo (corpo) da resposta.
     * @throws CertificateException
     * @throws UnrecoverableKeyException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws IOException
     */
    public String doHttpsRequest(String urlStr, MetodoHttpEnum metodo, Map<String, String> headers, String corpoRequisicao, boolean insereCertificado) throws CertificateException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException, IOException {
        if (insereCertificado) {
            if (senhaCertificado == null || pathCertificado == null) {
                throw new CertificateException("Ao definir o parâmetro 'insereCertificado' como 'true', devem ser informados o path (local) e a senha do certificado a ser utilizado.");
            }

            KeyStore certificadosClientStore = new CertificadosUtil().importaCertificados(this.pathCertificado, this.senhaCertificado);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(certificadosClientStore, this.senhaCertificado.toCharArray());
            KeyManager[] kms = kmf.getKeyManagers();

            TrustManager[] confiaEmTodosCertificados = new TrustManager[]{new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }

            }};

            SSLContext sslContext = null;
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kms, confiaEmTodosCertificados, new SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostNameVerifierCustomizado());
        }

        URL url = new URL(urlStr);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod(metodo.metodo);

        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }
        }

        if (corpoRequisicao != null) {
            con.setDoOutput(true);
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = corpoRequisicao.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    public void setPathCertificado(String pathCertificado) {
        this.pathCertificado = pathCertificado;
    }

    public void setSenhaCertificado(String senhaCertificado) {
        this.senhaCertificado = senhaCertificado;
    }
}

class HostNameVerifierCustomizado implements HostnameVerifier {

    @Override
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }
}
