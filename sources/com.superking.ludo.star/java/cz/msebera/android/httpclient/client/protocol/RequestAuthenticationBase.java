package cz.msebera.android.httpclient.client.protocol;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpRequest;
import cz.msebera.android.httpclient.HttpRequestInterceptor;
import cz.msebera.android.httpclient.auth.AuthOption;
import cz.msebera.android.httpclient.auth.AuthProtocolState;
import cz.msebera.android.httpclient.auth.AuthScheme;
import cz.msebera.android.httpclient.auth.AuthState;
import cz.msebera.android.httpclient.auth.AuthenticationException;
import cz.msebera.android.httpclient.auth.ContextAwareAuthScheme;
import cz.msebera.android.httpclient.auth.Credentials;
import cz.msebera.android.httpclient.extras.HttpClientAndroidLog;
import cz.msebera.android.httpclient.protocol.HttpContext;
import cz.msebera.android.httpclient.util.Asserts;
import io.branch.referral.R;
import java.util.Queue;
import org.cocos2dx.lib.Cocos2dxEditBox;
import org.cocos2dx.lib.Cocos2dxHandler;

@Deprecated
abstract class RequestAuthenticationBase implements HttpRequestInterceptor {
    final HttpClientAndroidLog log = new HttpClientAndroidLog(getClass());

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$cz$msebera$android$httpclient$auth$AuthProtocolState = new int[AuthProtocolState.values().length];

        static {
            try {
                $SwitchMap$cz$msebera$android$httpclient$auth$AuthProtocolState[AuthProtocolState.FAILURE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$cz$msebera$android$httpclient$auth$AuthProtocolState[AuthProtocolState.SUCCESS.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$cz$msebera$android$httpclient$auth$AuthProtocolState[AuthProtocolState.CHALLENGED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    void process(AuthState authState, HttpRequest request, HttpContext context) {
        AuthScheme authScheme = authState.getAuthScheme();
        Credentials creds = authState.getCredentials();
        switch (AnonymousClass1.$SwitchMap$cz$msebera$android$httpclient$auth$AuthProtocolState[authState.getState().ordinal()]) {
            case Cocos2dxHandler.HANDLER_SHOW_DIALOG /*1*/:
                return;
            case R.styleable.View_paddingStart /*2*/:
                ensureAuthScheme(authScheme);
                if (authScheme.isConnectionBased()) {
                    return;
                }
                break;
            case Cocos2dxEditBox.kEndActionReturn /*3*/:
                Queue<AuthOption> authOptions = authState.getAuthOptions();
                if (authOptions == null) {
                    ensureAuthScheme(authScheme);
                    break;
                }
                while (!authOptions.isEmpty()) {
                    AuthOption authOption = (AuthOption) authOptions.remove();
                    authScheme = authOption.getAuthScheme();
                    creds = authOption.getCredentials();
                    authState.update(authScheme, creds);
                    if (this.log.isDebugEnabled()) {
                        this.log.debug("Generating response to an authentication challenge using " + authScheme.getSchemeName() + " scheme");
                    }
                    try {
                        request.addHeader(authenticate(authScheme, creds, request, context));
                        return;
                    } catch (AuthenticationException ex) {
                        if (this.log.isWarnEnabled()) {
                            this.log.warn(authScheme + " authentication error: " + ex.getMessage());
                        }
                    }
                }
                return;
        }
        if (authScheme != null) {
            try {
                request.addHeader(authenticate(authScheme, creds, request, context));
            } catch (AuthenticationException ex2) {
                if (this.log.isErrorEnabled()) {
                    this.log.error(authScheme + " authentication error: " + ex2.getMessage());
                }
            }
        }
    }

    private void ensureAuthScheme(AuthScheme authScheme) {
        Asserts.notNull(authScheme, "Auth scheme");
    }

    private Header authenticate(AuthScheme authScheme, Credentials creds, HttpRequest request, HttpContext context) throws AuthenticationException {
        Asserts.notNull(authScheme, "Auth scheme");
        if (authScheme instanceof ContextAwareAuthScheme) {
            return ((ContextAwareAuthScheme) authScheme).authenticate(creds, request, context);
        }
        return authScheme.authenticate(creds, request);
    }
}
