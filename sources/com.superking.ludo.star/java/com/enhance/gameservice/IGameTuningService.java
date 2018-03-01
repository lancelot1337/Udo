package com.enhance.gameservice;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IGameTuningService extends IInterface {

    public static abstract class Stub extends Binder implements IGameTuningService {
        private static final String DESCRIPTOR = "com.enhance.gameservice.IGameTuningService";
        static final int TRANSACTION_boostUp = 3;
        static final int TRANSACTION_getAbstractTemperature = 4;
        static final int TRANSACTION_setFramePerSecond = 2;
        static final int TRANSACTION_setGamePowerSaving = 5;
        static final int TRANSACTION_setPreferredResolution = 1;

        private static class Proxy implements IGameTuningService {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public int setPreferredResolution(int resolution) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(resolution);
                    this.mRemote.transact(Stub.TRANSACTION_setPreferredResolution, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setFramePerSecond(int fps) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fps);
                    this.mRemote.transact(Stub.TRANSACTION_setFramePerSecond, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int boostUp(int seconds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(seconds);
                    this.mRemote.transact(Stub.TRANSACTION_boostUp, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAbstractTemperature() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getAbstractTemperature, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setGamePowerSaving(boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enable) {
                        i = Stub.TRANSACTION_setPreferredResolution;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setGamePowerSaving, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGameTuningService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGameTuningService)) {
                return new Proxy(obj);
            }
            return (IGameTuningService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            switch (code) {
                case TRANSACTION_setPreferredResolution /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setPreferredResolution(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_setFramePerSecond /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setFramePerSecond(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_boostUp /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = boostUp(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_getAbstractTemperature /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getAbstractTemperature();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_setGamePowerSaving /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setGamePowerSaving(data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int boostUp(int i) throws RemoteException;

    int getAbstractTemperature() throws RemoteException;

    int setFramePerSecond(int i) throws RemoteException;

    int setGamePowerSaving(boolean z) throws RemoteException;

    int setPreferredResolution(int i) throws RemoteException;
}
