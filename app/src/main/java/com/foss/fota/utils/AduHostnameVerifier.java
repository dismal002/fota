package com.foss.fota.utils;

import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

public final class AduHostnameVerifier implements HostnameVerifier {
    public static final AduHostnameVerifier INSTANCE = new AduHostnameVerifier();
    private static final Pattern IP_ADDRESS_PATTERN = Pattern.compile("([0-9a-fA-F]*:[0-9a-fA-F:.]*)|([\\d.]+)");
    private final String fossCn = "foss.cn";
    private final String fossCom = "foss.com";

    private AduHostnameVerifier() {
    }

    static boolean isIpAddress(String hostname) {
        return IP_ADDRESS_PATTERN.matcher(hostname).matches();
    }

    private static List<String> getSubjectAlternativeNames(X509Certificate certificate, int type) {
        ArrayList<String> result = new ArrayList<>();
        try {
            Collection<List<?>> subjectAlternativeNames = certificate.getSubjectAlternativeNames();
            if (subjectAlternativeNames == null) {
                return Collections.emptyList();
            }
            for (List<?> list : subjectAlternativeNames) {
                if (list != null && list.size() >= 2) {
                    Integer altType = (Integer) list.get(0);
                    if (altType != null && altType.intValue() == type) {
                        String altName = (String) list.get(1);
                        if (altName != null) {
                            result.add(altName);
                        }
                    }
                }
            }
            return result;
        } catch (CertificateParsingException e) {
            return Collections.emptyList();
        }
    }

    @Override // javax.net.ssl.HostnameVerifier
    public boolean verify(String hostname, SSLSession session) {
        try {
            return verify(hostname, (X509Certificate) session.getPeerCertificates()[0]);
        } catch (SSLException e) {
            return false;
        }
    }

    public boolean verify(String hostname, X509Certificate certificate) {
        if (isIpAddress(hostname)) {
            return verifyIpAddress(hostname, certificate);
        }
        return verifyHostname(hostname, certificate);
    }

    private boolean verifyIpAddress(String ipAddress, X509Certificate certificate) {
        List<String> altNames = getSubjectAlternativeNames(certificate, 7);
        for (String altName : altNames) {
            if (ipAddress.equalsIgnoreCase(altName)) {
                return true;
            }
        }
        return false;
    }

    private boolean verifyHostname(String hostname, X509Certificate certificate) {
        String lowerCaseHostname = hostname.toLowerCase(Locale.US);
        List<String> altNames = getSubjectAlternativeNames(certificate, 2);
        boolean hasAltNames = false;
        for (String altName : altNames) {
            if (verifyMatch(lowerCaseHostname, altName)) {
                return true;
            }
            hasAltNames = true;
        }
        if (!hasAltNames) {
            String commonName = new DistinguishedNameParser(certificate.getSubjectX500Principal()).findMostSpecific("cn");
            if (commonName != null) {
                return verifyMatch(lowerCaseHostname, commonName);
            }
        }
        return false;
    }

    private boolean verifyMatch(String hostname, String pattern) {
        if ((!hostname.endsWith(this.fossCn) && !hostname.endsWith(this.fossCom)) || 
            hostname == null || hostname.length() == 0 || hostname.startsWith(".") || hostname.endsWith("..") || 
            pattern == null || pattern.length() == 0 || pattern.startsWith(".") || pattern.endsWith("..")) {
            return false;
        }
        if (!hostname.endsWith(".")) {
            hostname = hostname + '.';
        }
        if (!pattern.endsWith(".")) {
            pattern = pattern + '.';
        }
        String lowerCasePattern = pattern.toLowerCase(Locale.US);
        if (!lowerCasePattern.contains("*")) {
            return hostname.equals(lowerCasePattern);
        }
        if (!lowerCasePattern.startsWith("*.") || lowerCasePattern.indexOf('*', 1) != -1 || hostname.length() < lowerCasePattern.length() || "*.".equals(lowerCasePattern)) {
            return false;
        }
        String suffix = lowerCasePattern.substring(1);
        if (!hostname.endsWith(suffix)) {
            return false;
        }
        int suffixStart = hostname.length() - suffix.length();
        return suffixStart <= 0 || hostname.lastIndexOf('.', suffixStart - 1) == -1;
    }
}
