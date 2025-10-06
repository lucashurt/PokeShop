import React from "react";
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./AuthContext.jsx";
import Login from "./pages/authentication/Login.jsx";
import Register from "./pages/authentication/Registration.jsx";

// Temporary dashboard placeholder
function Dashboard() {
    return (
        <div className="min-h-screen flex items-center justify-center" style={{ background: '#0f172a' }}>
            <div className="text-center">
                <h1 className="text-4xl font-bold mb-4" style={{ color: '#fbbf24' }}>
                    Dashboard Coming Soon
                </h1>
                <p style={{ color: '#cbd5e1' }}>You're logged in! Dashboard will be built in Day 2.</p>
            </div>
        </div>
    );
}

export default function App() {
    return (
        <AuthProvider>
            <Router>
                <Routes>
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                    <Route path="/dashboard" element={<Dashboard />} />
                    <Route path="/" element={<Navigate to="/login" replace />} />
                </Routes>
            </Router>
        </AuthProvider>
    );
}