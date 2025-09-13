const API_BASE_URL = "/api";

class ApiService{
    async request(endpoint,options={}){
        const url = `${API_BASE_URL}${endpoint}`;
        const config = {
            headers:{
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options,
        };

        if(config.body && typeof config.body !== 'string'){
            config.body = JSON.stringify(config.body);
        }

        try{
            const response = await fetch(url,config);

            if(!response.ok){
                const errorText = await response.text();
                throw new Error(`HTTP ${response.status}: ${errorText}`)
            }

            const contentType = response.headers.get('content-type');
            if(contentType && contentType.includes('application/json')){
                return await response.json;
            }
            return await response.text();
        }
        catch (error){
            console.error(`API request failed for ${endpoint}: ${error}`);
            throw error;
        }
    }

    async login(username,password){
        return await this.request('/auth/login', {
            method: 'POST',
            body: {username, password}
        });
    }

    async register(username,password,fullName,role){
        return await this.request('/auth/register', {
            method: 'POST',
            body: {username, password, fullName, role}
        });
    }

    async getProduct(productId){
        return this.request(`/products/${productId}`)
    }

    async getBusinessInventory(businessName){
        return this.request(`/products/${businessName}/inventory`)
    }

    async createProduct(name,description,price,stock){
        return this.request(`/products`,{
            method:'POST',
            body: {name,description,price,stock}
        });
    }

    async getCart(){
        return this.request('/cart')
    }

    async addToCart(productId,quantity){
        return this.request('/cart/add',{
            method:'POST',
            body:{productId,quantity}
        });
    }

    async removeFromCart(productId,quantity = 0){
        return this.request('/cart/add',{
            method:'POST',
            body:{productId,quantity}
        });
    }

    async getAllOrders(){
        return this.request('/orders')
    }

    async getOrder(orderId){
        return this.request(`/orders/${orderId}`)
    }

    async updateOrderStatus(orderId,status){
        return this.request(`/orders/${orderId}/status`,{
            method:'POST',
            body:{status}
        });
    }

    async cancelOrder(orderId){
        return this.request(`/orders/${orderId}/cancel`,{
            method:'DELETE',
        });
    }

    async createPaymentIntent(paymentMethodId = null, currency = 'usd'){
        return this.request('/payment/create-intent',{
            method:'POST',
            body:{paymentMethodId,currency}
        });
    }

    async createCheckoutSession(successURL,cancelURL,metadata = {}){
        return this.request('/payment/create-checkout-session',{
            method:'POST',
            body:{successURL,cancelURL,metadata}
        });
    }
}
export default new ApiService();