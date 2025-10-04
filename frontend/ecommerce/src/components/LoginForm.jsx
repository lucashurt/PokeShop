import React, { useState } from "react";
import { useAuth } from '../AuthContext';

export default function LoginForm() {
    const { login } = useAuth();
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [message, setMessage] = useState("");

    const handleSubmit = async (e) => {
        e.preventDefault();
        const res = await login(username, password);
        setMessage(res.message);
    };

    return (
        <div className="bg-gray-100 rounded-[10px] p-6 border-black border-2 flex flex-col gap-3" >
            <h2 className="text-center text-2xl">Login</h2>
            <form onSubmit={handleSubmit}>
                <div className="h-full flex flex-col gap-4">
                <input
                    className="border-black border-2 rounded-[6px] p-1"
                    type="text"
                    placeholder="Username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                />
                <input
                    className="border-black border-2 rounded-[6px] p-1"
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                />
                <button  className=" rounded-[8px] bg-blue-300 text-grey" type="submit">Login</button>
                </div>
            </form>
            {message && <p>{message}</p>}
        </div>
    );
}
