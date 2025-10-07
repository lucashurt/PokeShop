import { useAuth } from '../../AuthContext';
import { useNavigate } from 'react-router-dom';

const DashboardLayout = ({ children }) => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <div className="min-h-screen bg-gray-50">
            {/* Top Navigation Bar */}
            <nav className="bg-black border-b-4 border-yellow-400 sticky top-0 z-50">
                <div className="max-w-7xl mx-auto px-6">
                    <div className="flex justify-between items-center h-16">
                        {/* Logo */}
                        <div className="flex items-center gap-3">
                            <div className="w-8 h-8 bg-yellow-400 rounded"></div>
                            <span className="text-xl font-bold text-white">PokeShop</span>
                        </div>

                        {/* User Info & Actions */}
                        <div className="flex items-center gap-6">
                            <div className="text-right">
                                <div className="text-sm font-semibold text-white">{user?.username}</div>
                                <div className="text-xs text-gray-400">
                                    {user?.role === 'ROLE_BUSINESS' ? 'Business' : 'Customer'}
                                </div>
                            </div>
                            <button
                                onClick={handleLogout}
                                className="px-4 py-2 bg-gray-800 text-white text-sm font-medium rounded hover:bg-gray-700 transition-colors"
                            >
                                Logout
                            </button>
                        </div>
                    </div>
                </div>
            </nav>

            {/* Main Content */}
            <main className="max-w-7xl mx-auto px-6 py-8">
                {children}
            </main>
        </div>
    );
};

export default DashboardLayout;