import { useEffect, useState } from "react";
import axios from "axios";

export default function Home() {
    const [user, setUser] = useState(null);

    useEffect(() => {
        axios.get('http://localhost:8080/user-info', {withCredentials: true})
        .then(response => {
            setUser(response.data);
        })
        .catch(error => {
            console.error('Error occured moron: ', error);
        })
    }, [])
    

    async function logout() {
        try{
            await axios.post('http://localhost:8080/Logout', {}, {withCredentials: true});
            window.location.replace('http://localhost:5173/Authentication');
        } catch (error){
            console.error('Failed to logout, check ya cors', error);
            if (error.response && error.response.status === 401) {
                window.location.href = 'http://localhost:5173/Authentication';
            }
        }
    }


    return (
        <div>
        <h1>Well done moron, your login finally works</h1>
        
        {user ? (
            <div>
                <p><strong>Name: </strong>{user.name}</p>
                <p><strong>Email: </strong>{user.email}</p>
            </div>
        ) : (
            <p>Loading user data...</p>
        )}

        <button onClick={logout}>
            Logout
        </button>

        </div>
    );
}