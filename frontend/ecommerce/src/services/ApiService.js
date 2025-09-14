const API_BASE_URL = "/api";

class ApiService{

    constructor(){
        this.token = this.getStoredToken();
    }

    getStoredToken(){
        return localStorage.getItem('jwt_token')
    }

    setToken(token){
        this.token = token;
        if(token){
            localStorage.setItem('jwt_token',token);
        }
        else{
            localStorage.removeItem('jwt_token');
        }
    }

    getAuthHeaders(){
        const headers = {
            'Content-Type': 'application/json'
        };
        if(this.token){
            headers['Authorization'] = `Bearer ${this.token}`
        }
        return headers;
    }

    isAuthenticated(){
        return !!this.token && !this.isTokenExpired();
    }

    isTokenExpired(){
        if(!this.token) return true;
        try{
            const payload = JSON.parse(atob(this.token.split('.')[1]));
            const currentTime = Date.now() / 1000
            return payload.exp < currentTime;
        }
        catch(error){
            return true;
        }
    }

    getUserInfo(){
        if(!this.token) return null;

        try{
            const payload = JSON.parse(atob(this.token.split('.')[1]));
            return{
                username: payload.sub,
                role: payload.role,
                userId: payload.userId,
                exp: payload.exp,
                iat: payload.iat
            };
        }
        catch (error) {
            return null;
        }
    }

    logout(){
        this.setToken(null)
    }
    async request(endpoint,options={}){
        const url = `${API_BASE_URL}${endpoint}`;
        const config = {
            headers:{
                ...this.getAuthHeaders(),
                ...options.headers
            },
            ...options,
        };

        if(config.body && typeof config.body !== 'string'){
            config.body = JSON.stringify(config.body);
        }

        try{
            const response = await fetch(url,config);

            if(response.status === 401){
                this.logout();
                throw new Error('Unauthorized must login')
            }

            if(!response.ok){
                const errorText = await response.text();
                throw new Error(`HTTP ${response.status}: ${errorText}`)
            }

            const contentType = response.headers.get('content-type');
            if(contentType && contentType.includes('application/json')){
                return await response.json();
            }
            return await response.text();
        }
        catch (error){
            console.error(`API request failed for ${endpoint}: ${error}`);
            throw error;
        }
    }

    async login(username,password){
        const response = await this.request('/auth/login', {
            method: 'POST',
            body: {username, password}
        });

        if(response.token){
            this.setToken(response.token);
        }

        return response;
    }

    async register(username,password,fullName,role){
        const response =  await this.request('/auth/register', {
            method: 'POST',
            body: {username, password, fullName, role}
        });

        if(response.token){
            this.setToken(response.token)
        }

        return response;
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
        return this.request('/cart/remove',{
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
