package com.foss.fota.utils;

import android.content.pm.Signature;
import android.util.Base64;
import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class GetPkgSignatureHash {
    private String a(byte[] bArr, String str) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bArr) {
            String hexString = Integer.toHexString(b & 255);
            if (hexString.length() == 1) {
                sb.append('0');
            }
            sb.append(hexString).append(str);
        }
        return sb.toString();
    }

    private String a(byte[] bArr) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(bArr);
            return a(messageDigest.digest(), "");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public String[] a(Signature[] signatureArr) {
        if (signatureArr.length != 0 && signatureArr[0] != null) {
            byte[] byteArray = signatureArr[0].toByteArray();
            if (byteArray.length <= 0) {
                return null;
            }
            String[] strArr = new String[2];
            try {
                X509Certificate x509Certificate = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(new ByteArrayInputStream(byteArray));
                strArr[0] = a(Base64.encodeToString(x509Certificate.getEncoded(), 2).getBytes());
                strArr[1] = x509Certificate.getIssuerDN().toString();
                return strArr;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }
}
