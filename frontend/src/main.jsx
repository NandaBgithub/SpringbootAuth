import {createRoot} from 'react-dom/client';
import {BrowserRouter, Routes, Route} from "react-router";
import Authentication from "./Pages/Authentication";
import ProtectedRoute from "./Pages/ProtectedRoute";
import Home from "./Pages/Home"

createRoot(document.getElementById('root')).render(
  <BrowserRouter>
    <Routes>
      <Route path="/Authentication" element={<Authentication />}/>
      <Route path="/Home" element={
        <ProtectedRoute>
          <Home/>
        </ProtectedRoute>
      } />
      <Route path="/" element={<Authentication/>}/>

    </Routes>
  </BrowserRouter>
)
