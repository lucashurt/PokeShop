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
        <div>
            <h2>Register</h2>
            <form onSubmit={handleSubmit}>
                <input
                    type="text"
                    placeholder="Username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                />
                <br />
                <input
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                />
                <br />
                <input
                    type="text"
                    placeholder="Full Name"
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    required
                />
                <br />
                <select value={role} onChange={(e) => setRole(e.target.value)}>
                    <option value="CUSTOMER">Customer</option>
                    <option value="BUSINESS">Business</option>
                </select>
                <br />
                <button type="submit">Register</button>
            </form>
            {message && <p>{message}</p>}
        </div>
    );
}
