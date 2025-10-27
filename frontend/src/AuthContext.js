import { AuthProvider } from "react-oidc-context";

const oidcConfig = {
    authority: process.env.REACT_APP_KEYCLOAK_AUTHORITY,
    client_id: process.env.REACT_APP_KEYCLOAK_CLIENT_ID,
    redirect_uri: process.env.REACT_APP_KEYCLOAK_REDIRECT_URI,
    onSigninCallback: () => {
        window.history.replaceState({}, document.title, window.location.pathname);
    }
};

export const AuthProviderContext = ({ children }) => {
    return (
        <AuthProvider {...oidcConfig}>
            {children}
        </AuthProvider>
    );
};