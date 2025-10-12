import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import ApiService from '../../services/ApiService';

const ProductsPage = () => {
    const [allProducts, setAllProducts] = useState([]);
    const [filteredProducts, setFilteredProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [searchInput, setSearchInput] = useState('');
    const [activeFilter, setActiveFilter] = useState('all');
    const navigate = useNavigate();

    useEffect(() => {
        loadAllProducts();
    }, []);

    const loadAllProducts = async () => {
        try {
            setLoading(true);
            setError('');
            const data = await ApiService.getAllProducts();
            setAllProducts(data);
            setFilteredProducts(data);
        } catch (err) {
            setError('Failed to load products');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleSearch = (e) => {
        e.preventDefault();
        if (!searchInput.trim()) {
            setFilteredProducts(allProducts);
            setActiveFilter('all');
            return;
        }

        const filtered = allProducts.filter(product =>
            product.businessUsername.toLowerCase().includes(searchInput.toLowerCase()) ||
            product.name.toLowerCase().includes(searchInput.toLowerCase()) ||
            product.description.toLowerCase().includes(searchInput.toLowerCase())
        );
        setFilteredProducts(filtered);
        setActiveFilter('search');
    };

    const handleClearSearch = () => {
        setSearchInput('');
        setFilteredProducts(allProducts);
        setActiveFilter('all');
    };

    const handleAddToCart = async (productId, productName) => {
        try {
            await ApiService.addToCart(productId, 1);
            alert(`${productName} added to cart!`);
        } catch (err) {
            setError('Failed to add to cart: ' + err.message);
        }
    };

    const getStockBadgeClass = (stock) => {
        if (stock === 0) return 'bg-red-100 text-red-800';
        if (stock <= 10) return 'bg-yellow-100 text-yellow-800';
        return 'bg-green-100 text-green-800';
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center min-h-[400px]">
                <div className="w-8 h-8 border-4 border-yellow-400 border-t-transparent rounded-full animate-spin"></div>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-3xl font-bold text-gray-900">Shop Products</h1>
                    <p className="text-gray-600 mt-1">
                        {activeFilter === 'all'
                            ? `Browse all ${allProducts.length} products`
                            : `Found ${filteredProducts.length} products`}
                    </p>
                </div>
                <button
                    onClick={() => navigate('/cart')}
                    className="px-6 py-3 bg-yellow-400 text-black font-semibold rounded hover:bg-yellow-500 transition-colors flex items-center gap-2"
                >
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
                    </svg>
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
                            placeholder="Search by product name, description, or business..."
                            className="w-full px-4 py-3 border border-gray-300 rounded focus:outline-none focus:border-yellow-400"
                        />
                    </div>
                    <button
                        type="submit"
                        className="px-8 py-3 bg-black text-white font-semibold rounded hover:bg-gray-900 transition-colors"
                    >
                        Search
                    </button>
                    {activeFilter === 'search' && (
                        <button
                            type="button"
                            onClick={handleClearSearch}
                            className="px-6 py-3 bg-gray-200 text-gray-700 font-semibold rounded hover:bg-gray-300 transition-colors"
                        >
                            Clear
                        </button>
                    )}
                </form>
            </div>

            {/* Error Message */}
            {error && (
                <div className="bg-red-50 border-l-4 border-red-500 p-4">
                    <p className="text-red-700 text-sm">{error}</p>
                </div>
            )}

            {/* Products Grid */}
            {filteredProducts.length > 0 ? (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {filteredProducts.map((product) => (
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
                                <div className="flex items-start justify-between mb-2">
                                    <h3 className="text-lg font-bold text-gray-900 flex-1">
                                        {product.name}
                                    </h3>
                                </div>

                                <p className="text-xs text-gray-500 mb-2">
                                    by {product.businessUsername}
                                </p>

                                <p className="text-sm text-gray-600 mb-4 line-clamp-2">
                                    {product.description}
                                </p>

                                <div className="flex items-center justify-between mb-4">
                                    <span className="text-2xl font-bold text-gray-900">
                                        ${product.price.toFixed(2)}
                                    </span>
                                    <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStockBadgeClass(product.stock)}`}>
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
            ) : (
                <div className="bg-white border border-gray-200 rounded-lg p-12 text-center">
                    <div className="text-6xl mb-4">🔍</div>
                    <h3 className="text-lg font-semibold text-gray-900 mb-2">
                        No products found
                    </h3>
                    <p className="text-gray-600">
                        {activeFilter === 'search'
                            ? 'Try a different search term'
                            : 'No products available yet'}
                    </p>
                </div>
            )}
        </div>
    );
};

export default ProductsPage;