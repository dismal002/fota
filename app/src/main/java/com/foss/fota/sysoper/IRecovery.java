package com.foss.fota.sysoper;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.foss.fota.sysoper.IRecoveryCallback;

public interface IRecovery extends IInterface {
    int reboot() throws RemoteException;

    int recovery(String str) throws RemoteException;

    int recovery_ab_binder(IRecoveryCallback iRecoveryCallback) throws RemoteException;

    int recovery_ab_install(RecoveryParams recoveryParams) throws RemoteException;

    public static abstract class Stub extends Binder implements IRecovery {
        private static final String DESCRIPTOR = "com.foss.fota.sysoper.IRecovery";
        static final int TRANSACTION_reboot = 4;
        static final int TRANSACTION_recovery = 1;
        static final int TRANSACTION_recovery_ab_binder = 3;
        static final int TRANSACTION_recovery_ab_install = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRecovery asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface iInterfaceQueryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (iInterfaceQueryLocalInterface != null && (iInterfaceQueryLocalInterface instanceof IRecovery)) {
                return (IRecovery) iInterfaceQueryLocalInterface;
            }
            return new Proxy(iBinder);
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            RecoveryParams recoveryParamsCreateFromParcel;
            switch (i) {
                case 1:
                    parcel.enforceInterface(DESCRIPTOR);
                    int iRecovery = recovery(parcel.readString());
                    parcel2.writeNoException();
                    parcel2.writeInt(iRecovery);
                    return true;
                case 2:
                    parcel.enforceInterface(DESCRIPTOR);
                    if (parcel.readInt() != 0) {
                        recoveryParamsCreateFromParcel = RecoveryParams.CREATOR.createFromParcel(parcel);
                    } else {
                        recoveryParamsCreateFromParcel = null;
                    }
                    int iRecovery_ab_install = recovery_ab_install(recoveryParamsCreateFromParcel);
                    parcel2.writeNoException();
                    parcel2.writeInt(iRecovery_ab_install);
                    return true;
                case 3:
                    parcel.enforceInterface(DESCRIPTOR);
                    int iRecovery_ab_binder = recovery_ab_binder(IRecoveryCallback.Stub.asInterface(parcel.readStrongBinder()));
                    parcel2.writeNoException();
                    parcel2.writeInt(iRecovery_ab_binder);
                    return true;
                case 4:
                    parcel.enforceInterface(DESCRIPTOR);
                    int iReboot = reboot();
                    parcel2.writeNoException();
                    parcel2.writeInt(iReboot);
                    return true;
                case 1598968902:
                    parcel2.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(i, parcel, parcel2, i2);
            }
        }

        private static class Proxy implements IRecovery {
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.foss.fota.sysoper.IRecovery
            public int recovery(String str) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    parcelObtain.writeString(str);
                    this.mRemote.transact(1, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                    return parcelObtain2.readInt();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.foss.fota.sysoper.IRecovery
            public int recovery_ab_install(RecoveryParams recoveryParams) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (recoveryParams != null) {
                        parcelObtain.writeInt(1);
                        recoveryParams.writeToParcel(parcelObtain, 0);
                    } else {
                        parcelObtain.writeInt(0);
                    }
                    this.mRemote.transact(2, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                    return parcelObtain2.readInt();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.foss.fota.sysoper.IRecovery
            public int recovery_ab_binder(IRecoveryCallback iRecoveryCallback) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    parcelObtain.writeStrongBinder(iRecoveryCallback != null ? iRecoveryCallback.asBinder() : null);
                    this.mRemote.transact(3, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                    return parcelObtain2.readInt();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.foss.fota.sysoper.IRecovery
            public int reboot() throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                    return parcelObtain2.readInt();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }
        }
    }
}
