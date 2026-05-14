package com.foss.fota.sysoper;

import android.os.Parcel;
import android.os.Parcelable;

public class RecoveryParams implements Parcelable {
    public static final Parcelable.Creator<RecoveryParams> CREATOR = new Parcelable.Creator<RecoveryParams>() { // from class: com.foss.fota.sysoper.RecoveryParams.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public RecoveryParams createFromParcel(Parcel parcel) {
            return new RecoveryParams(parcel);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public RecoveryParams[] newArray(int i) {
            return new RecoveryParams[i];
        }
    };
    private String[] headerKeyValuePairs;
    private long offset;
    private long size;
    private String url;

    public RecoveryParams(String str, long j, long j2, String[] strArr) {
        this.url = str;
        this.offset = j;
        this.size = j2;
        this.headerKeyValuePairs = strArr;
    }

    public RecoveryParams() {
    }

    protected RecoveryParams(Parcel parcel) {
        this.url = parcel.readString();
        this.offset = parcel.readLong();
        this.size = parcel.readLong();
        this.headerKeyValuePairs = parcel.createStringArray();
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String str) {
        this.url = str;
    }

    public long getOffset() {
        return this.offset;
    }

    public void setOffset(long j) {
        this.offset = j;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(long j) {
        this.size = j;
    }

    public String[] getHeaderKeyValuePairs() {
        return this.headerKeyValuePairs;
    }

    public void setHeaderKeyValuePairs(String[] strArr) {
        this.headerKeyValuePairs = strArr;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.url);
        parcel.writeLong(this.offset);
        parcel.writeLong(this.size);
        parcel.writeStringArray(this.headerKeyValuePairs);
    }
}
