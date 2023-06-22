package gov.rfb.nfeouro.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class CertificadosUtil {

    /**
     * Importa os certificados de um arquivo com extensão .p12
     *
     * @param path Local, na estação de execução do serviço, em que encontra-se o certificado para assinatura do XML.
     * @param senha Senha do arquivo contendo os certificados.
     * @return KeyStore contendo os certificados
     * @throws KeyStoreException
     * @throws IOException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     */
    public KeyStore importaCertificados(String path, String senha) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore p12KeyStore = KeyStore.getInstance("pkcs12");
        p12KeyStore.load(new FileInputStream(path), senha.toCharArray());

        return p12KeyStore;
    }

}
