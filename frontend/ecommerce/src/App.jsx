import React from "react";
import { AuthProvider, useAuth } from "./AuthContext.jsx";
import LoginForm from "./components/LoginForm.jsx";
import RegisterForm from "./components/RegisterForm.jsx";

function Dashboard() {
    const { user, logout } = useAuth();

    return (
        <div>
            <h2 className="text-4xl text-center">Welcome {user?.username || "Guest"}</h2>
            {user ? (
                <>
                    <p>Role: {user.role}</p>
                    <button onClick={logout}>Logout</button>
                </>
            ) : (
                <div className="flex flex-col gap-6">
                    <LoginForm />
                    <RegisterForm />
                </div>
            )}
        </div>
    );
}

export default function App() {
    return (
        <AuthProvider>
            <div className="m-10">
                <h1 className="text-6xl text-center">JWT Test App</h1>
                <Dashboard />
            </div>
        </AuthProvider>
    );
}
