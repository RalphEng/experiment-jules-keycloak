import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import { AuthProviderContext } from './AuthContext';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <AuthProviderContext>
      <App />
    </AuthProviderContext>
  </React.StrictMode>
);