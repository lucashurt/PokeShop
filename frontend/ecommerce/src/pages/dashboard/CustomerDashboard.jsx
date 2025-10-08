import { useState, useEffect } from 'react';
import ApiService from '../../services/ApiService';

const CustomerDashboard = () => {
    const [cart, setCart] = useState(null);
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        loadData();
    }, []);

    const loadData = async () => {
        try {
            setLoading(true);
            const [cartData, ordersData] = await Promise.all([
                ApiService.getCart(),
                ApiService.getAllOrders()
            ]);
            setCart(cartData);
            setOrders(ordersData);
        } catch (err) {
            setError('Failed to load dashboard data');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center min-h-[400px]">
                <div className="w-8 h-8 border-4 border-yellow-400 border-t-transparent rounded-full animate-spin"></div>
            </div>
        );
    }

    const getStatusColor = (status) => {
        const colors = {
            'COMPLETED': 'bg-green-100 text-green-800',
            'PENDING': 'bg-yellow-100 text-yellow-800',
            'PROCESSING': 'bg-blue-100 text-blue-800',
            'CANCELLED': 'bg-red-100 text-red-800',
            'PAID': 'bg-green-100 text-green-800'
        };
        return colors[status] || 'bg-gray-100 text-gray-800';
    };

    return (
        <div className="space-y-6">
            {/* Page Header */}
            <div>
                <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
                <p className="text-gray-600 mt-1">Track your orders and manage your cart</p>
            </div>

            {error && (
                <div className="bg-red-50 border-l-4 border-red-500 p-4">
                    <p className="text-red-700 text-sm">{error}</p>
                </div>
            )}

            {/* Stats Grid */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                {/* Cart Summary */}
                <div className="bg-white border border-gray-200 rounded-lg p-6">
                    <div className="flex justify-between items-start mb-4">
                        <div>
                            <p className="text-sm font-medium text-gray-600">Cart Items</p>
                            <p className="text-3xl font-bold text-gray-900 mt-1">
                                {cart?.cartItems?.length || 0}
                            </p>
                        </div>
                        <div className="w-12 h-12 bg-yellow-400 rounded-lg flex items-center justify-center">
                            <svg className="w-6 h-6 text-black" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
                            </svg>
                        </div>
                    </div>
                    <div className="border-t border-gray-200 pt-4">
                        <p className="text-sm text-gray-600">Subtotal</p>
                        <p className="text-xl font-bold text-gray-900">${cart?.subtotal?.toFixed(2) || '0.00'}</p>
                    </div>
                </div>

                {/* Orders Summary */}
                <div className="bg-white border border-gray-200 rounded-lg p-6">
                    <div className="flex justify-between items-start mb-4">
                        <div>
                            <p className="text-sm font-medium text-gray-600">Total Orders</p>
                            <p className="text-3xl font-bold text-gray-900 mt-1">
                                {orders?.length || 0}
                            </p>
                        </div>
                        <div className="w-12 h-12 bg-gray-900 rounded-lg flex items-center justify-center">
                            <svg className="w-6 h-6 text-yellow-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
                            </svg>
                        </div>
                    </div>
                    <div className="border-t border-gray-200 pt-4">
                        <p className="text-sm text-gray-600">Lifetime spending</p>
                        <p className="text-xl font-bold text-gray-900">
                            ${orders.reduce((sum, order) => sum + (order.total || 0), 0).toFixed(2)}
                        </p>
                    </div>
                </div>

                {/* Quick Action */}
                <div className="bg-yellow-400 border border-yellow-500 rounded-lg p-6">
                    <div className="mb-4">
                        <p className="text-sm font-medium text-black">Quick Actions</p>
                        <p className="text-2xl font-bold text-black mt-1">Browse</p>
                    </div>
                    <button className="w-full py-3 bg-black text-white font-medium rounded hover:bg-gray-900 transition-colors">
                        View Products
                    </button>
                </div>
            </div>

            {/* Recent Orders Table */}
            <div className="bg-white border border-gray-200 rounded-lg overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-200">
                    <h2 className="text-lg font-bold text-gray-900">Recent Orders</h2>
                </div>
                <div className="overflow-x-auto">
                    {orders && orders.length > 0 ? (
                        <table className="w-full">
                            <thead className="bg-gray-50 border-b border-gray-200">
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
                                    Order ID
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
                                    Total
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
                                    Status
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
                                    Date
                                </th>
                            </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-200">
                            {orders.slice(0, 5).map((order) => (
                                <tr key={order.orderId} className="hover:bg-gray-50">
                                    <td className="px-6 py-4 whitespace-nowrap">
                                        <span className="text-sm font-medium text-gray-900">#{order.orderId}</span>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap">
                                            <span className="text-sm font-semibold text-gray-900">
                                                ${order.total?.toFixed(2)}
                                            </span>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap">
                                            <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(order.orderStatus)}`}>
                                                {order.orderStatus}
                                            </span>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                                        {new Date(order.createdAt).toLocaleDateString()}
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    ) : (
                        <div className="text-center py-12">
                            <p className="text-gray-600">No orders yet</p>
                            <p className="text-sm text-gray-400 mt-1">Your orders will appear here</p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default CustomerDashboard;