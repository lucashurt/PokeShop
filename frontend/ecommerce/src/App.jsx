import React from "react";
import { AuthProvider, useAuth } from "./AuthContext.jsx";
import LoginForm from "./components/LoginForm.jsx";
import RegisterForm from "./components/RegisterForm.jsx";

function Dashboard() {
    const { user, logout } = useAuth();

    return (
        <div>
            <h2>Welcome {user?.username || "Guest"}</h2>
            {user ? (
                <>
                    <p>Role: {user.role}</p>
                    <button onClick={logout}>Logout</button>
                </>
            ) : (
                <>
                    <LoginForm />
                    <RegisterForm />
                </>
            )}
        </div>
    );
}

export default function App() {
    return (
        <AuthProvider>
            <div>
                <h1>JWT Test App</h1>
                <Dashboard />
            </div>
        </AuthProvider>
    );
}
