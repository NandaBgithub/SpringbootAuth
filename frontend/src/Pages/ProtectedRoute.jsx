import { Navigate } from 'react-router';
import { useEffect, useState } from 'react';
import axios from 'axios';

export default function ProtectedRoute({ children }) {
    const [isAuthenticated, setIsAuthenticated] = useState(null);

    useEffect(() => {
        axios.get('http://localhost:8080/check-auth', { withCredentials: true })
            .then(() => setIsAuthenticated(true))
            .catch(() => setIsAuthenticated(false));
    }, []);

    if (isAuthenticated === null) return <div>Loading...</div>; // or a spinner

    return isAuthenticated ? children : <Navigate to="/Authentication" />;
}