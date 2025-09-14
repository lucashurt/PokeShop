import React, { createContext, useContext, useState, useEffect } from 'react';
import ApiService from "./services/ApiService.js"

export const AuthContext = createContext();

export const useAuth = () =>{
    const context = useContext(AuthContext);
    if(!context){
        throw new Error('UseAuth must be used in AuthProvider');
    }
    return context;
}

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() =>  {
        if(ApiService.isAuthenticated()){
            const userInfo = ApiService.getUserInfo();
            setUser(userInfo);
        }
        setLoading(false);
    },[]);

    const login = async(username,password) => {
        try{
            const response = await ApiService.login(username,password);

            if(response.token) {
                const userInfo = ApiService.getUserInfo();
                setUser(userInfo);
                return {success:true, message:response.message}
            }
            else{
                return {success:false, message: response.message}
            }
        }
        catch (error){
            return { success: false, message: error.message };
        }
    }

    const register = async (username,password,fullName,role) => {
       try {
           const response = await ApiService.register(username, password, fullName, role);
           if (response.token) {
               const userInfo = ApiService.getUserInfo();
               setUser(userInfo);
               return {success: true, message: response.message};
           } else {
               return {success: false, message: response.message};
           }
       }
       catch (error){
           return {success: false, message: error.message};
       }
    }

    const logout = () => {
        ApiService.logout();
        setUser(null);
    }

    const isAuthenticated = () => {
        return ApiService.isAuthenticated();
    }

    const hasRole = (role) => {
        return user && user.role === role;
    }

    const value = {
        user,
        login,
        register,
        logout,
        isAuthenticated,
        hasRole,
        loading
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );

};
