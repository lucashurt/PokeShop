import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../AuthContext';

const Login = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        const result = await login(username, password);

        if (result.success) {
            navigate('/dashboard');
        } else {
            setError(result.message || 'Login failed');
        }
        setLoading(false);
    };

    return (
        <div
            className="min-h-screen flex items-center justify-center p-4"
            style={{
                background: 'linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #334155 100%)'
            }}
        >
            {/* Main container */}
            <div className="w-full max-w-md">
                {/* Card */}
                <div
                    className="rounded-xl overflow-hidden shadow-2xl"
                    style={{
                        background: '#ffffff',
                        border: '3px solid #fbbf24'
                    }}
                >
                    {/* Header */}
                    <div
                        className="p-8 pb-6"
                        style={{
                            background: 'linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%)',
                            borderBottom: '2px solid #b45309'
                        }}
                    >
                        <div className="flex items-center justify-center gap-3 mb-2">
                            <div
                                className="w-10 h-10 rounded-full flex items-center justify-center text-2xl"
                                style={{ background: 'rgba(0,0,0,0.15)' }}
                            >
                                ⚡
                            </div>
                            <h1
                                className="text-4xl font-black"
                                style={{
                                    color: '#1e293b',
                                    letterSpacing: '0.5px'
                                }}
                            >
                                PokeShop
                            </h1>
                        </div>
                        <p
                            className="text-center text-sm font-semibold"
                            style={{ color: '#1e293b' }}
                        >
                            Trainer Login Portal
                        </p>
                    </div>

                    {/* Form section */}
                    <div className="p-8">
                        <form onSubmit={handleSubmit} className="flex flex-col gap-5">
                            {/* Error banner */}
                            {error && (
                                <div
                                    className="rounded-lg p-3 text-sm"
                                    style={{
                                        background: 'rgba(239, 68, 68, 0.1)',
                                        border: '2px solid #ef4444',
                                        color: '#dc2626'
                                    }}
                                >
                                    <div className="flex items-center gap-2">
                                        <span className="text-lg">⚠️</span>
                                        <span className="font-medium">{error}</span>
                                    </div>
                                </div>
                            )}

                            {/* Username input */}
                            <div>
                                <label
                                    htmlFor="username"
                                    className="block text-sm font-bold mb-2"
                                    style={{ color: '#1e293b' }}
                                >
                                    Trainer Username
                                </label>
                                <input
                                    id="username"
                                    type="text"
                                    value={username}
                                    onChange={(e) => setUsername(e.target.value)}
                                    required
                                    disabled={loading}
                                    placeholder="Enter your username"
                                    className="w-full px-4 py-3 rounded-lg transition-all duration-200 outline-none font-medium"
                                    style={{
                                        background: '#f8fafc',
                                        border: '2px solid #cbd5e1',
                                        color: '#1e293b',
                                    }}
                                    onFocus={(e) => {
                                        e.target.style.borderColor = '#fbbf24';
                                        e.target.style.background = '#ffffff';
                                    }}
                                    onBlur={(e) => {
                                        e.target.style.borderColor = '#cbd5e1';
                                        e.target.style.background = '#f8fafc';
                                    }}
                                />
                            </div>

                            {/* Password input */}
                            <div>
                                <label
                                    htmlFor="password"
                                    className="block text-sm font-bold mb-2"
                                    style={{ color: '#1e293b' }}
                                >
                                    Password
                                </label>
                                <input
                                    id="password"
                                    type="password"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    required
                                    disabled={loading}
                                    placeholder="Enter your password"
                                    className="w-full px-4 py-3 rounded-lg transition-all duration-200 outline-none font-medium"
                                    style={{
                                        background: '#f8fafc',
                                        border: '2px solid #cbd5e1',
                                        color: '#1e293b',
                                    }}
                                    onFocus={(e) => {
                                        e.target.style.borderColor = '#fbbf24';
                                        e.target.style.background = '#ffffff';
                                    }}
                                    onBlur={(e) => {
                                        e.target.style.borderColor = '#cbd5e1';
                                        e.target.style.background = '#f8fafc';
                                    }}
                                />
                            </div>

                            {/* Submit button */}
                            <button
                                type="submit"
                                disabled={loading}
                                className="w-full py-3.5 rounded-lg font-bold text-base tracking-wide transition-all duration-200 mt-2"
                                style={{
                                    background: loading ? '#9ca3af' : 'linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%)',
                                    color: '#1e293b',
                                    boxShadow: loading ? 'none' : '0 4px 12px rgba(251, 191, 36, 0.4)',
                                    cursor: loading ? 'not-allowed' : 'pointer'
                                }}
                                onMouseEnter={(e) => {
                                    if (!loading) {
                                        e.target.style.transform = 'translateY(-2px)';
                                        e.target.style.boxShadow = '0 6px 16px rgba(251, 191, 36, 0.5)';
                                    }
                                }}
                                onMouseLeave={(e) => {
                                    if (!loading) {
                                        e.target.style.transform = 'translateY(0)';
                                        e.target.style.boxShadow = '0 4px 12px rgba(251, 191, 36, 0.4)';
                                    }
                                }}
                            >
                                {loading ? 'Logging in...' : 'Enter Shop'}
                            </button>
                        </form>

                        {/* Footer */}
                        <div className="mt-6 text-center text-sm">
                            <span style={{ color: '#64748b' }}>New trainer? </span>
                            <Link
                                to="/register"
                                className="font-bold transition-colors duration-200"
                                style={{ color: '#f59e0b' }}
                                onMouseEnter={(e) => e.target.style.color = '#fbbf24'}
                                onMouseLeave={(e) => e.target.style.color = '#f59e0b'}
                            >
                                Create an account
                            </Link>
                        </div>
                    </div>
                </div>

                {/* Footer decoration */}
                <div className="mt-4 text-center text-xs" style={{ color: '#94a3b8' }}>
                    Powered by PokeShop © 2025
                </div>
            </div>
        </div>
    );
};

export default Login;