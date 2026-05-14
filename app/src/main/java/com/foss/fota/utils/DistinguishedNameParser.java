package com.foss.fota.utils;

import javax.security.auth.x500.X500Principal;

/* JADX INFO: compiled from: DistinguishedNameParser.java */
/* JADX INFO: loaded from: classes.dex */
public final class DistinguishedNameParser {
    private final String dn;
    private final int length;
    private int pos;
    private int beg;
    private int end;
    private int cur;
    private char[] chars;

    public DistinguishedNameParser(X500Principal principal) {
        this.dn = principal.getName("RFC2253");
        this.length = this.dn.length();
    }

    private String nextAT() {
        while (this.pos < this.length && this.chars[this.pos] == ' ') {
            this.pos++;
        }
        if (this.pos == this.length) {
            return null;
        }
        this.beg = this.pos;
        this.pos++;
        while (this.pos < this.length && this.chars[this.pos] != '=' && this.chars[this.pos] != ' ') {
            this.pos++;
        }
        if (this.pos >= this.length) {
            throw new IllegalStateException("Unexpected end of DN: " + this.dn);
        }
        this.end = this.pos;
        if (this.chars[this.pos] == ' ') {
            while (this.pos < this.length && this.chars[this.pos] != '=' && this.chars[this.pos] == ' ') {
                this.pos++;
            }
            if (this.chars[this.pos] != '=' || this.pos == this.length) {
                throw new IllegalStateException("Unexpected end of DN: " + this.dn);
            }
        }
        this.pos++;
        while (this.pos < this.length && this.chars[this.pos] == ' ') {
            this.pos++;
        }
        if (this.end - this.beg > 4 && this.chars[this.beg + 3] == '.' && ((this.chars[this.beg] == 'O' || this.chars[this.beg] == 'o') && ((this.chars[this.beg + 1] == 'I' || this.chars[this.beg + 1] == 'i') && (this.chars[this.beg + 2] == 'D' || this.chars[this.beg + 2] == 'd')))) {
            this.beg += 4;
        }
        return new String(this.chars, this.beg, this.end - this.beg);
    }

    private String quotedAV() {
        this.pos++;
        this.beg = this.pos;
        this.end = this.beg;
        while (this.pos != this.length) {
            if (this.chars[this.pos] == '\"') {
                this.pos++;
                while (this.pos < this.length && this.chars[this.pos] == ' ') {
                    this.pos++;
                }
                return new String(this.chars, this.beg, this.end - this.beg);
            }
            if (this.chars[this.pos] == '\\') {
                this.chars[this.end] = getEscaped();
            } else {
                this.chars[this.end] = this.chars[this.pos];
            }
            this.pos++;
            this.end++;
        }
        throw new IllegalStateException("Unexpected end of DN: " + this.dn);
    }

    private String hexAV() {
        if (this.pos + 4 >= this.length) {
            throw new IllegalStateException("Unexpected end of DN: " + this.dn);
        }
        this.beg = this.pos;
        this.pos++;
        while (this.pos != this.length && this.chars[this.pos] != '+' && this.chars[this.pos] != ',' && this.chars[this.pos] != ';') {
            if (this.chars[this.pos] == ' ') {
                this.end = this.pos;
                this.pos++;
                while (this.pos < this.length && this.chars[this.pos] == ' ') {
                    this.pos++;
                }
                int i = this.end - this.beg;
                if (i < 5 || (i & 1) == 0) {
                    throw new IllegalStateException("Unexpected end of DN: " + this.dn);
                }
                byte[] bArr = new byte[i / 2];
                int i2 = this.beg + 1;
                for (int i3 = 0; i3 < bArr.length; i3++) {
                    bArr[i3] = (byte) getByte(i2);
                    i2 += 2;
                }
                return new String(this.chars, this.beg, i);
            }
            if (this.chars[this.pos] >= 'A' && this.chars[this.pos] <= 'F') {
                this.chars[this.pos] = (char) (this.chars[this.pos] + ' ');
            }
            this.pos++;
        }
        this.end = this.pos;
        int i = this.end - this.beg;
        if (i < 5 || (i & 1) == 0) {
            throw new IllegalStateException("Unexpected end of DN: " + this.dn);
        }
        return new String(this.chars, this.beg, i);
    }

    private String escapedAV() {
        this.beg = this.pos;
        this.end = this.pos;
        while (this.pos < this.length) {
            switch (this.chars[this.pos]) {
                case ' ':
                    this.cur = this.end;
                    this.pos++;
                    this.chars[this.end++] = ' ';
                    while (this.pos < this.length && this.chars[this.pos] == ' ') {
                        this.chars[this.end++] = ' ';
                        this.pos++;
                    }
                    if (this.pos == this.length || this.chars[this.pos] == ',' || this.chars[this.pos] == '+' || this.chars[this.pos] == ';') {
                        return new String(this.chars, this.beg, this.cur - this.beg);
                    }
                    break;
                case '+':
                case ',':
                case ';':
                    return new String(this.chars, this.beg, this.end - this.beg);
                case '\\':
                    this.chars[this.end++] = getEscaped();
                    this.pos++;
                    break;
                default:
                    this.chars[this.end++] = this.chars[this.pos];
                    this.pos++;
                    break;
            }
        }
        return new String(this.chars, this.beg, this.end - this.beg);
    }

    private char getEscaped() {
        this.pos++;
        if (this.pos == this.length) {
            throw new IllegalStateException("Unexpected end of DN: " + this.dn);
        }
        switch (this.chars[this.pos]) {
            case ' ':
            case '\"':
            case '#':
            case '%':
            case '*':
            case '+':
            case ',':
            case ';':
            case '<':
            case '=':
            case '>':
            case '\\':
            case '_':
                return this.chars[this.pos];
            default:
                return getUTF8();
        }
    }

    private char getUTF8() {
        int i;
        int res;
        int firstByte = getByte(this.pos);
        this.pos++;
        if (firstByte < 128) {
            return (char) firstByte;
        }
        if (firstByte < 192 || firstByte > 247) {
            return '?';
        }
        if (firstByte <= 223) {
            i = 1;
            res = firstByte & 31;
        } else if (firstByte <= 239) {
            i = 2;
            res = firstByte & 15;
        } else {
            i = 3;
            res = firstByte & 7;
        }
        for (int j = 0; j < i; j++) {
            this.pos++;
            if (this.pos == this.length || this.chars[this.pos] != '\\') {
                return '?';
            }
            this.pos++;
            int nextByte = getByte(this.pos);
            this.pos++;
            if ((nextByte & 192) != 128) {
                return '?';
            }
            res = (res << 6) + (nextByte & 63);
        }
        return (char) res;
    }

    private int getByte(int position) {
        int i1;
        int i2;
        if (position + 1 >= this.length) {
            throw new IllegalStateException("Malformed DN: " + this.dn);
        }
        char c1 = this.chars[position];
        if (c1 >= '0' && c1 <= '9') {
            i1 = c1 - '0';
        } else if (c1 >= 'a' && c1 <= 'f') {
            i1 = c1 - 'W';
        } else if (c1 >= 'A' && c1 <= 'F') {
            i1 = c1 - '7';
        } else {
            throw new IllegalStateException("Malformed DN: " + this.dn);
        }
        char c2 = this.chars[position + 1];
        if (c2 >= '0' && c2 <= '9') {
            i2 = c2 - '0';
        } else if (c2 >= 'a' && c2 <= 'f') {
            i2 = c2 - 'W';
        } else if (c2 >= 'A' && c2 <= 'F') {
            i2 = c2 - '7';
        } else {
            throw new IllegalStateException("Malformed DN: " + this.dn);
        }
        return (i1 << 4) + i2;
    }

    public String findMostSpecific(String attributeType) {
        this.pos = 0;
        this.beg = 0;
        this.end = 0;
        this.cur = 0;
        this.chars = this.dn.toCharArray();
        String type = nextAT();
        if (type == null) {
            return null;
        }
        while (true) {
            String value = "";
            if (this.pos == this.length) {
                return null;
            }
            switch (this.chars[this.pos]) {
                case '\"':
                    value = quotedAV();
                    break;
                case '#':
                    value = hexAV();
                    break;
                case '+':
                case ',':
                case ';':
                    break;
                default:
                    value = escapedAV();
                    break;
            }
            if (attributeType.equalsIgnoreCase(type)) {
                return value;
            }
            if (this.pos >= this.length) {
                return null;
            }
            if (this.chars[this.pos] != ',' && this.chars[this.pos] != ';' && this.chars[this.pos] != '+') {
                throw new IllegalStateException("Malformed DN: " + this.dn);
            }
            this.pos++;
            type = nextAT();
            if (type == null) {
                throw new IllegalStateException("Malformed DN: " + this.dn);
            }
        }
    }
}
