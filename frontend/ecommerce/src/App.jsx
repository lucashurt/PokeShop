import React from "react";
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./AuthContext.jsx";
import Login from "./pages/authentication/Login.jsx";
import Register from "./pages/authentication/Registration.jsx";
import DashboardLayout from "./components/layout/DashboardLayout.jsx";
import BusinessDashboard from "./pages/dashboard/BusinessDashboard.jsx";

// Protected Route wrapper
function ProtectedRoute({ children }) {
    const { isAuthenticated, loading } = useAuth();

    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center" style={{ background: '#0f172a' }}>
                <div className="text-4xl">⚡</div>
            </div>
        );
    }

    return isAuthenticated() ? children : <Navigate to="/login" replace />;
}

// Dashboard Router - determines which dashboard to show based on role
function DashboardRouter() {
    const { user } = useAuth();

    return (
        <DashboardLayout>
            <BusinessDashboard />
        </DashboardLayout>
    );
}

export default function App() {
    return (
        <AuthProvider>
            <Router>
                <Routes>
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                    <Route
                        path="/dashboard"
                        element={
                            <ProtectedRoute>
                                <DashboardRouter />
                            </ProtectedRoute>
                        }
                    />
                    <Route path="/" element={<Navigate to="/dashboard" replace />} />
                </Routes>
            </Router>
        </AuthProvider>
    );
}