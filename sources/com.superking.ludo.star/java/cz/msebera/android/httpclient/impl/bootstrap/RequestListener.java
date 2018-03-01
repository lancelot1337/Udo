package cz.msebera.android.httpclient.impl.bootstrap;

import cz.msebera.android.httpclient.ExceptionLogger;
import cz.msebera.android.httpclient.HttpConnectionFactory;
import cz.msebera.android.httpclient.HttpServerConnection;
import cz.msebera.android.httpclient.config.SocketConfig;
import cz.msebera.android.httpclient.protocol.HttpService;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

class RequestListener implements Runnable {
    private final HttpConnectionFactory<? extends HttpServerConnection> connectionFactory;
    private final ExceptionLogger exceptionLogger;
    private final ExecutorService executorService;
    private final HttpService httpService;
    private final ServerSocket serversocket;
    private final SocketConfig socketConfig;
    private final AtomicBoolean terminated = new AtomicBoolean(false);

    public RequestListener(SocketConfig socketConfig, ServerSocket serversocket, HttpService httpService, HttpConnectionFactory<? extends HttpServerConnection> connectionFactory, ExceptionLogger exceptionLogger, ExecutorService executorService) {
        this.socketConfig = socketConfig;
        this.serversocket = serversocket;
        this.connectionFactory = connectionFactory;
        this.httpService = httpService;
        this.exceptionLogger = exceptionLogger;
        this.executorService = executorService;
    }

    public void run() {
        while (!isTerminated() && !Thread.interrupted()) {
            try {
                Socket socket = this.serversocket.accept();
                socket.setSoTimeout(this.socketConfig.getSoTimeout());
                socket.setKeepAlive(this.socketConfig.isSoKeepAlive());
                socket.setTcpNoDelay(this.socketConfig.isTcpNoDelay());
                if (this.socketConfig.getRcvBufSize() > 0) {
                    socket.setReceiveBufferSize(this.socketConfig.getRcvBufSize());
                }
                if (this.socketConfig.getSndBufSize() > 0) {
                    socket.setSendBufferSize(this.socketConfig.getSndBufSize());
                }
                if (this.socketConfig.getSoLinger() >= 0) {
                    socket.setSoLinger(true, this.socketConfig.getSoLinger());
                }
                this.executorService.execute(new Worker(this.httpService, (HttpServerConnection) this.connectionFactory.createConnection(socket), this.exceptionLogger));
            } catch (Exception ex) {
                this.exceptionLogger.log(ex);
                return;
            }
        }
    }

    public boolean isTerminated() {
        return this.terminated.get();
    }

    public void terminate() throws IOException {
        if (this.terminated.compareAndSet(false, true)) {
            this.serversocket.close();
        }
    }
}
