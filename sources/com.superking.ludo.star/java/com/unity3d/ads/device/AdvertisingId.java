package com.unity3d.ads.device;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.unity3d.ads.log.DeviceLog;
import io.branch.referral.R;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.cocos2dx.lib.Cocos2dxHandler;

@TargetApi(9)
public class AdvertisingId {
    private static final String ADVERTISING_ID_SERVICE_NAME = "com.google.android.gms.ads.identifier.internal.IAdvertisingIdService";
    private static AdvertisingId instance = null;
    private String advertisingIdentifier = null;
    private boolean limitedAdvertisingTracking = false;

    private interface GoogleAdvertisingInfo extends IInterface {

        public static abstract class GoogleAdvertisingInfoBinder extends Binder implements GoogleAdvertisingInfo {

            private static class GoogleAdvertisingInfoImplementation implements GoogleAdvertisingInfo {
                private final IBinder _binder;

                GoogleAdvertisingInfoImplementation(IBinder binder) {
                    this._binder = binder;
                }

                public IBinder asBinder() {
                    return this._binder;
                }

                public String getId() throws RemoteException {
                    Parcel localParcel1 = Parcel.obtain();
                    Parcel localParcel2 = Parcel.obtain();
                    try {
                        localParcel1.writeInterfaceToken(AdvertisingId.ADVERTISING_ID_SERVICE_NAME);
                        this._binder.transact(1, localParcel1, localParcel2, 0);
                        localParcel2.readException();
                        String str = localParcel2.readString();
                        return str;
                    } finally {
                        localParcel2.recycle();
                        localParcel1.recycle();
                    }
                }

                public boolean getEnabled(boolean paramBoolean) throws RemoteException {
                    boolean bool = true;
                    Parcel localParcel1 = Parcel.obtain();
                    Parcel localParcel2 = Parcel.obtain();
                    try {
                        int i;
                        localParcel1.writeInterfaceToken(AdvertisingId.ADVERTISING_ID_SERVICE_NAME);
                        if (paramBoolean) {
                            i = 1;
                        } else {
                            i = 0;
                        }
                        localParcel1.writeInt(i);
                        this._binder.transact(2, localParcel1, localParcel2, 0);
                        localParcel2.readException();
                        if (localParcel2.readInt() == 0) {
                            bool = false;
                        }
                        localParcel2.recycle();
                        localParcel1.recycle();
                        return bool;
                    } catch (Throwable th) {
                        localParcel2.recycle();
                        localParcel1.recycle();
                    }
                }
            }

            public static GoogleAdvertisingInfo create(IBinder binder) {
                if (binder == null) {
                    return null;
                }
                IInterface localIInterface = binder.queryLocalInterface(AdvertisingId.ADVERTISING_ID_SERVICE_NAME);
                if (localIInterface == null || !(localIInterface instanceof GoogleAdvertisingInfo)) {
                    return new GoogleAdvertisingInfoImplementation(binder);
                }
                return (GoogleAdvertisingInfo) localIInterface;
            }

            public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
                int i = 0;
                switch (code) {
                    case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                        data.enforceInterface(AdvertisingId.ADVERTISING_ID_SERVICE_NAME);
                        String str1 = getId();
                        reply.writeNoException();
                        reply.writeString(str1);
                        return true;
                    case R.styleable.View_paddingStart /*2*/:
                        boolean bool1;
                        data.enforceInterface(AdvertisingId.ADVERTISING_ID_SERVICE_NAME);
                        if (data.readInt() != 0) {
                            bool1 = true;
                        } else {
                            bool1 = false;
                        }
                        boolean bool2 = getEnabled(bool1);
                        reply.writeNoException();
                        if (bool2) {
                            i = 1;
                        }
                        reply.writeInt(i);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            }
        }

        boolean getEnabled(boolean z) throws RemoteException;

        String getId() throws RemoteException;
    }

    private class GoogleAdvertisingServiceConnection implements ServiceConnection {
        private final BlockingQueue<IBinder> _binderQueue;
        boolean _consumed;

        private GoogleAdvertisingServiceConnection() {
            this._consumed = false;
            this._binderQueue = new LinkedBlockingQueue();
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                this._binderQueue.put(service);
            } catch (InterruptedException e) {
                DeviceLog.debug("Couldn't put service to binder que");
            }
        }

        public void onServiceDisconnected(ComponentName name) {
        }

        public IBinder getBinder() throws InterruptedException {
            if (this._consumed) {
                throw new IllegalStateException();
            }
            this._consumed = true;
            return (IBinder) this._binderQueue.take();
        }
    }

    private static AdvertisingId getInstance() {
        if (instance == null) {
            instance = new AdvertisingId();
        }
        return instance;
    }

    public static void init(Context context) {
        getInstance().fetchAdvertisingId(context);
    }

    public static String getAdvertisingTrackingId() {
        return getInstance().advertisingIdentifier;
    }

    public static boolean getLimitedAdTracking() {
        return getInstance().limitedAdvertisingTracking;
    }

    private void fetchAdvertisingId(Context context) {
        GoogleAdvertisingServiceConnection connection = new GoogleAdvertisingServiceConnection();
        Intent localIntent = new Intent("com.google.android.gms.ads.identifier.service.START");
        localIntent.setPackage("com.google.android.gms");
        if (context.bindService(localIntent, connection, 1)) {
            try {
                GoogleAdvertisingInfo advertisingInfo = GoogleAdvertisingInfoBinder.create(connection.getBinder());
                this.advertisingIdentifier = advertisingInfo.getId();
                this.limitedAdvertisingTracking = advertisingInfo.getEnabled(true);
            } catch (Exception e) {
                DeviceLog.exception("Couldn't get advertising info", e);
            } finally {
                context.unbindService(connection);
            }
        }
    }
}
