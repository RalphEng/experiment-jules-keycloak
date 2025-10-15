import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import { useAuth } from 'react-oidc-context';

function App() {
    return (
        <Router>
            <Header />
            <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/secure" element={<SecurePage />} />
                <Route path="/admin" element={<AdminPage />} />
                <Route path="/callback" element={<Callback />} />
            </Routes>
        </Router>
    );
}

function Header() {
    const auth = useAuth();

    return (
        <header>
            <nav>
                <Link to="/">Home</Link> | <Link to="/secure">Secure</Link> | <Link to="/admin">Admin</Link>
            </nav>
            <div>
                {!auth.isAuthenticated ? (
                    <button onClick={() => auth.signinRedirect()}>Log in</button>
                ) : (
                    <button onClick={() => auth.signoutRedirect()}>Log out</button>
                )}
            </div>
        </header>
    );
}

function Home() {
    const auth = useAuth();
    return (
        <div>
            <h1>Public Page</h1>
            <p>Welcome, {auth.user?.profile.preferred_username || 'guest'}.</p>
        </div>
    );
}

function SecurePage() {
    const auth = useAuth();

    if (auth.isLoading) {
        return <div>Loading...</div>;
    }

    if (!auth.isAuthenticated) {
        auth.signinRedirect();
        return null;
    }

    return (
        <div>
            <h1>Secure Page</h1>
            <p>Welcome, {auth.user?.profile.preferred_username}. You have the following roles: {auth.user?.profile.realm_access.roles.join(', ')}</p>
        </div>
    );
}

function AdminPage() {
    const auth = useAuth();
    const [users, setUsers] = React.useState([]);
    const [error, setError] = React.useState(null);

    React.useEffect(() => {
        if (auth.user) {
            fetch('/api/admin/users', {
                headers: {
                    'Authorization': `Bearer ${auth.user.access_token}`
                }
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => setUsers(data))
            .catch(error => setError(error.message));
        }
    }, [auth.user]);

    if (auth.isLoading) {
        return <div>Loading...</div>;
    }

    if (!auth.isAuthenticated) {
        auth.signinRedirect();
        return null;
    }

    const isAdmin = auth.user?.profile.realm_access.roles.includes('APPX-Admin');

    if (!isAdmin) {
        return (
            <div>
                <h1>Admin Page</h1>
                <p>You are not authorized to view this page.</p>
            </div>
        );
    }

    return (
        <div>
            <h1>Admin Page</h1>
            <p>Welcome, Admin {auth.user?.profile.preferred_username}.</p>
            <h2>User List</h2>
            {error && <p>Error fetching users: {error}</p>}
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Username</th>
                        <th>Email</th>
                    </tr>
                </thead>
                <tbody>
                    {users.map(user => (
                        <tr key={user.id}>
                            <td>{user.id}</td>
                            <td>{user.username}</td>
                            <td>{user.email}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}

function Callback() {
    const auth = useAuth();
    auth.signinCallback();
    return <div>Loading...</div>;
}

export default App;