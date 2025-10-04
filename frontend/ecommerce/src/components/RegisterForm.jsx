import React, { useState } from "react";
import { useAuth } from '../AuthContext';

export default function RegisterForm() {
    const { register } = useAuth();
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [fullName, setFullName] = useState("");
    const [role, setRole] = useState("CUSTOMER"); // default
    const [message, setMessage] = useState("");

    const handleSubmit = async (e) => {
        e.preventDefault();
        const res = await register(username, password, fullName, role);
        setMessage(res.message);
    };

    return (
        <div className="border-black border-2 flex flex-col justify-between rounded-[10px] p-6">
            <h2 className="text-center text-2xl">Register</h2>
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
                <input
                    className="border-black border-2 rounded-[6px] p-1"
                    type="text"
                    placeholder="Full Name"
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    required
                />
                <select className="rounded-[8px] w-40 bg-gray-400" value={role} onChange={(e) => setRole(e.target.value)}>
                    <option value="CUSTOMER">Customer</option>
                    <option value="BUSINESS">Business</option>
                </select>
                    <button  className=" rounded-[8px] bg-blue-300 text-grey" type="submit">Register</button>
                </div>
                </form>
            {message && <p>{message}</p>}
        </div>
    );
}
