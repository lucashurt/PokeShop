import { useState, useEffect } from 'react';
import { useAuth } from '../../AuthContext';
import ApiService from '../../services/ApiService';

const BusinessDashboard = () => {
    const { user } = useAuth();
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showAddProduct, setShowAddProduct] = useState(false);

    const [newProduct, setNewProduct] = useState({
        name: '',
        description: '',
        price: '',
        stock: ''
    });

    useEffect(() => {
        loadProducts();
    }, []);

    const loadProducts = async () => {
        try {
            setLoading(true);
            const data = await ApiService.getBusinessInventory(user.username);
            setProducts(data);
        } catch (err) {
            setError('Failed to load products');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleAddProduct = async (e) => {
        e.preventDefault();
        try {
            await ApiService.createProduct(
                newProduct.name,
                newProduct.description,
                parseFloat(newProduct.price),
                parseInt(newProduct.stock)
            );
            setNewProduct({ name: '', description: '', price: '', stock: '' });
            setShowAddProduct(false);
            loadProducts();
        } catch (err) {
            setError('Failed to add product');
            console.error(err);
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center min-h-[400px]">
                <div className="w-8 h-8 border-4 border-yellow-400 border-t-transparent rounded-full animate-spin"></div>
            </div>
        );
    }

    const getStockStatus = (stock) => {
        if (stock === 0) return { color: 'bg-red-100 text-red-800', label: 'Out of Stock' };
        if (stock <= 10) return { color: 'bg-yellow-100 text-yellow-800', label: 'Low Stock' };
        return { color: 'bg-green-100 text-green-800', label: 'In Stock' };
    };

    return (
        <div className="space-y-6">
            {/* Page Header */}
            <div className="flex justify-between items-start">
                <div>
                    <h1 className="text-3xl font-bold text-gray-900">Business Dashboard</h1>
                    <p className="text-gray-600 mt-1">Manage your inventory and products</p>
                </div>
                <button
                    onClick={() => setShowAddProduct(!showAddProduct)}
                    className="px-6 py-3 bg-black text-yellow-400 font-semibold rounded hover:bg-gray-900 transition-colors"
                >
                    {showAddProduct ? 'Cancel' : 'Add Product'}
                </button>
            </div>

            {error && (
                <div className="bg-red-50 border-l-4 border-red-500 p-4">
                    <p className="text-red-700 text-sm">{error}</p>
                </div>
            )}

            {/* Add Product Form */}
            {showAddProduct && (
                <div className="bg-white border border-gray-200 rounded-lg p-6">
                    <h2 className="text-lg font-bold text-gray-900 mb-4">Add New Product</h2>
                    <form onSubmit={handleAddProduct} className="space-y-4">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Product Name
                                </label>
                                <input
                                    type="text"
                                    value={newProduct.name}
                                    onChange={(e) => setNewProduct({ ...newProduct, name: e.target.value })}
                                    required
                                    className="w-full px-4 py-2 border border-gray-300 rounded focus:outline-none focus:border-yellow-400"
                                    placeholder="Enter product name"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Price
                                </label>
                                <input
                                    type="number"
                                    step="0.01"
                                    value={newProduct.price}
                                    onChange={(e) => setNewProduct({ ...newProduct, price: e.target.value })}
                                    required
                                    className="w-full px-4 py-2 border border-gray-300 rounded focus:outline-none focus:border-yellow-400"
                                    placeholder="0.00"
                                />
                            </div>
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Stock Quantity
                            </label>
                            <input
                                type="number"
                                value={newProduct.stock}
                                onChange={(e) => setNewProduct({ ...newProduct, stock: e.target.value })}
                                required
                                className="w-full px-4 py-2 border border-gray-300 rounded focus:outline-none focus:border-yellow-400"
                                placeholder="0"
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Description
                            </label>
                            <textarea
                                value={newProduct.description}
                                onChange={(e) => setNewProduct({ ...newProduct, description: e.target.value })}
                                required
                                rows="3"
                                className="w-full px-4 py-2 border border-gray-300 rounded focus:outline-none focus:border-yellow-400"
                                placeholder="Product description"
                            />
                        </div>
                        <button
                            type="submit"
                            className="w-full py-3 bg-yellow-400 text-black font-semibold rounded hover:bg-yellow-500 transition-colors"
                        >
                            Add Product
                        </button>
                    </form>
                </div>
            )}

            {/* Stats Grid */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div className="bg-white border border-gray-200 rounded-lg p-6">
                    <p className="text-sm font-medium text-gray-600">Total Products</p>
                    <p className="text-3xl font-bold text-gray-900 mt-2">{products.length}</p>
                </div>

                <div className="bg-white border border-gray-200 rounded-lg p-6">
                    <p className="text-sm font-medium text-gray-600">Total Stock</p>
                    <p className="text-3xl font-bold text-gray-900 mt-2">
                        {products.reduce((sum, p) => sum + p.stock, 0)}
                    </p>
                </div>

                <div className="bg-white border border-gray-200 rounded-lg p-6">
                    <p className="text-sm font-medium text-gray-600">Inventory Value</p>
                    <p className="text-3xl font-bold text-gray-900 mt-2">
                        ${products.reduce((sum, p) => sum + (p.price * p.stock), 0).toFixed(2)}
                    </p>
                </div>
            </div>

            {/* Products Table */}
            <div className="bg-white border border-gray-200 rounded-lg overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-200">
                    <h2 className="text-lg font-bold text-gray-900">Inventory</h2>
                </div>
                <div className="overflow-x-auto">
                    {products.length > 0 ? (
                        <table className="w-full">
                            <thead className="bg-gray-50 border-b border-gray-200">
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
                                    Product
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
                                    Description
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
                                    Price
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">
                                    Stock
                                </th>
                            </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-200">
                            {products.map((product) => {
                                const stockStatus = getStockStatus(product.stock);
                                return (
                                    <tr key={product.id} className="hover:bg-gray-50">
                                        <td className="px-6 py-4">
                                            <span className="text-sm font-medium text-gray-900">{product.name}</span>
                                        </td>
                                        <td className="px-6 py-4">
                                            <span className="text-sm text-gray-600">{product.description}</span>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <span className="text-sm font-semibold text-gray-900">${product.price.toFixed(2)}</span>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                                <span className={`px-3 py-1 rounded-full text-xs font-medium ${stockStatus.color}`}>
                                                    {product.stock} units
                                                </span>
                                        </td>
                                    </tr>
                                );
                            })}
                            </tbody>
                        </table>
                    ) : (
                        <div className="text-center py-12">
                            <p className="text-gray-600">No products yet</p>
                            <p className="text-sm text-gray-400 mt-1">Add your first product to get started</p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default BusinessDashboard;