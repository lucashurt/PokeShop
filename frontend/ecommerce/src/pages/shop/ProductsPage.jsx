import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import ApiService from '../../services/ApiService';

const ProductsPage = () => {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [businessName, setBusinessName] = useState('');
    const [searchInput, setSearchInput] = useState('');
    const navigate = useNavigate();

    const loadProducts = async (business) => {
        if (!business.trim()) return;

        try {
            setLoading(true);
            setError('');
            const data = await ApiService.getBusinessInventory(business);
            setProducts(data);
            setBusinessName(business);
        } catch (err) {
            setError('Business not found or has no products');
            setProducts([]);
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleSearch = (e) => {
        e.preventDefault();
        loadProducts(searchInput);
    };

    const handleAddToCart = async (productId, productName) => {
        try {
            await ApiService.addToCart(productId, 1);
            alert(`${productName} added to cart!`);
        } catch (err) {
            setError('Failed to add to cart: ' + err.message);
        }
    };

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-3xl font-bold text-gray-900">Browse Products</h1>
                    <p className="text-gray-600 mt-1">Search for businesses and shop their products</p>
                </div>
                <button
                    onClick={() => navigate('/cart')}
                    className="px-6 py-3 bg-yellow-400 text-black font-semibold rounded hover:bg-yellow-500 transition-colors"
                >
                    View Cart
                </button>
            </div>

            {/* Search Bar */}
            <div className="bg-white border border-gray-200 rounded-lg p-6">
                <form onSubmit={handleSearch} className="flex gap-4">
                    <div className="flex-1">
                        <input
                            type="text"
                            value={searchInput}
                            onChange={(e) => setSearchInput(e.target.value)}
                            placeholder="Enter business username (e.g., 'business')"
                            className="w-full px-4 py-3 border border-gray-300 rounded focus:outline-none focus:border-yellow-400"
                        />
                    </div>
                    <button
                        type="submit"
                        disabled={loading}
                        className="px-8 py-3 bg-black text-white font-semibold rounded hover:bg-gray-900 transition-colors disabled:bg-gray-400"
                    >
                        {loading ? 'Searching...' : 'Search'}
                    </button>
                </form>
            </div>

            {/* Error Message */}
            {error && (
                <div className="bg-red-50 border-l-4 border-red-500 p-4">
                    <p className="text-red-700 text-sm">{error}</p>
                </div>
            )}

            {/* Products Grid */}
            {businessName && (
                <div>
                    <div className="mb-4">
                        <h2 className="text-xl font-bold text-gray-900">
                            Products from {businessName}
                        </h2>
                    </div>

                    {products.length > 0 ? (
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                            {products.map((product) => (
                                <div
                                    key={product.id}
                                    className="bg-white border border-gray-200 rounded-lg overflow-hidden hover:shadow-lg transition-shadow"
                                >
                                    {/* Product Image Placeholder */}
                                    <div className="h-48 bg-gradient-to-br from-yellow-400 to-yellow-500 flex items-center justify-center">
                                        <span className="text-6xl">📦</span>
                                    </div>

                                    {/* Product Details */}
                                    <div className="p-6">
                                        <h3 className="text-lg font-bold text-gray-900 mb-2">
                                            {product.name}
                                        </h3>
                                        <p className="text-sm text-gray-600 mb-4 line-clamp-2">
                                            {product.description}
                                        </p>

                                        <div className="flex items-center justify-between mb-4">
                                            <span className="text-2xl font-bold text-gray-900">
                                                ${product.price.toFixed(2)}
                                            </span>
                                            <span className={`px-3 py-1 rounded-full text-xs font-medium ${
                                                product.stock > 10
                                                    ? 'bg-green-100 text-green-800'
                                                    : product.stock > 0
                                                        ? 'bg-yellow-100 text-yellow-800'
                                                        : 'bg-red-100 text-red-800'
                                            }`}>
                                                {product.stock > 0 ? `${product.stock} in stock` : 'Out of stock'}
                                            </span>
                                        </div>

                                        <button
                                            onClick={() => handleAddToCart(product.id, product.name)}
                                            disabled={product.stock === 0}
                                            className="w-full py-3 bg-black text-white font-semibold rounded hover:bg-gray-900 transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed"
                                        >
                                            {product.stock === 0 ? 'Out of Stock' : 'Add to Cart'}
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : !loading && (
                        <div className="bg-white border border-gray-200 rounded-lg p-12 text-center">
                            <p className="text-gray-600">No products found for this business</p>
                        </div>
                    )}
                </div>
            )}

            {/* Initial State */}
            {!businessName && !loading && (
                <div className="bg-white border border-gray-200 rounded-lg p-12 text-center">
                    <div className="text-6xl mb-4">🔍</div>
                    <h3 className="text-lg font-semibold text-gray-900 mb-2">
                        Start Shopping
                    </h3>
                    <p className="text-gray-600">
                        Enter a business username above to browse their products
                    </p>
                </div>
            )}
        </div>
    );
};

export default ProductsPage;