import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import Navbar from './components/Navbar.jsx';
import Footer from './components/Footer.jsx';
import HomePage from './pages/HomePage';
import SignupPage from './pages/SignupPage';
import LoginPage from './pages/LoginPage'
import LocationAndSports from './pages/LocationAndSports';
import {AuthProvider} from "./utils/AuthContext.jsx";
import ProtectedRoute from "./utils/ProtectedRoute.jsx";
import TurfDetails from "./Pages/TurfDetails.jsx";
import SlotDetails from "./Pages/SlotDetails.jsx";
import ConfirmPayment from "./Pages/ConfirmPayment.jsx";
import UserProfile from "./Pages/UserProfile.jsx";
import Wishlist from "./Pages/Wishlist.jsx";
import EditProfile from "./Pages/EditProfile.jsx";
import AdminLogin from "./Pages/AdminLogin.jsx";
import AdminSignup from "./Pages/AdminSignup.jsx";
import AddTurfForm from "./Pages/AddTurfForm.jsx";
import UpdateTurf from "./Pages/UpdateTurf.jsx";
import EditTurf from "./Pages/EditTurf.jsx";
import SetSlot from "./Pages/SetSlot.jsx";
import BookingDetails from "./Pages/BookingDetails.jsx";
import PaymentPage from "./Pages/FinalPay.jsx";
import EditBooking from "./Pages/EditBooking.jsx";
const App = () => {
  return (

      <Router>
          <div className="app">
              <AuthProvider>
                  <Navbar />
                  <Routes>
                      <Route path="/" element={<HomePage />} />
                      <Route path="/signup" element={<SignupPage />} />
                      <Route path="/login" element={<LoginPage />} />
                      <Route path="/location" element={
                          <ProtectedRoute>
                              <LocationAndSports/>

                          </ProtectedRoute> }/>
                      <Route
                          path="/turfs" element={
                          <ProtectedRoute>
                              <TurfDetails />
                          </ProtectedRoute>}/>
                      <Route
                          path="/:turfId" element={
                          <ProtectedRoute>
                              <SlotDetails />
                          </ProtectedRoute>}/>

                      <Route
                          path="/dashboard"
                          element={
                              <ProtectedRoute>
                                  <UserProfile />
                              </ProtectedRoute>
                          }
                      />
                      <Route
                          path="/wishlist"
                          element={
                              <ProtectedRoute>
                                  <Wishlist />
                              </ProtectedRoute>
                          }
                      />


                      <Route path="/editprofile" element={<ProtectedRoute><EditProfile/></ProtectedRoute>}/>
                      <Route path="/locationandsports" element={<ProtectedRoute><LocationAndSports/></ProtectedRoute>}></Route>
                      <Route path="/adminlogin" element={<AdminLogin/>}></Route>
                      <Route path="/adminsignup" element={<AdminSignup/>}></Route>
                      <Route path="/turfregister" element={<AddTurfForm/>}></Route>
                      <Route path="/updateturf" element={<UpdateTurf/>}></Route>
                      <Route path="/edit-turf/:turfid" element = {<EditTurf/>}></Route>
                      <Route path="/set-slot/:turfid" element = {<SetSlot/>}></Route>
                      <Route path="/bookingdetails" element={<BookingDetails/>} />
                      <Route path="/payment" element={<PaymentPage/>} />
                      <Route path="/confirmpayment" element={<ConfirmPayment/>} />
                      <Route path="/edit-booking/:bookingId" element={<EditBooking />}></Route>
                  </Routes>
                  <Footer />
              </AuthProvider>
          </div>
      </Router>
  );
};

export default App;