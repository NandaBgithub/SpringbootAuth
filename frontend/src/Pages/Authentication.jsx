import "./PageStyles/Authentication.css";

import axios from 'axios';

export default function Authentication() {
    function githubLogin() {
        window.location.href = 'http://localhost:8080/oauth2/authorization/github';
    }

    function googleLogin() {
        window.location.href = 'http://localhost:8080/oauth2/authorization/google';
    }

    async function handleSubmit(e) {
        e.preventDefault();

        const username = document.getElementById('username-input').value;
        const password = document.getElementById('password-input').value;

        try {
            const response = await axios.post(
                'http://localhost:8080/CustomLogin',
                { username, password },
                { withCredentials: true }
            );

            if (response.status === 200) {
                window.location.href = '/Home';
            }
        } catch (err) {
            console.error('Login failed:', err);
        }
    }

    return (
        <div>
            <h3>Login with us</h3>
            <form onSubmit={handleSubmit}>
                <input
                    type="text"
                    id="username-input"
                    name="username"
                    placeholder="Username"
                />

                <input
                    type="password"
                    id="password-input"
                    name="password"
                    placeholder="Password"
                />

                <button type="submit">Submit</button>
            </form>

            <h3>Login with third party</h3>
            <button onClick={githubLogin}>Login With Github</button>
            <button onClick={googleLogin}>Login With Google</button>
        </div>
    );
}
