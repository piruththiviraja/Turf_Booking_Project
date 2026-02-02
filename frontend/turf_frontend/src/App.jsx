import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import Navbar from './Components/Navbar';
import Footer from './Components/Footer';
import HomePage from './Pages/HomePage';
import SignupPage from './Pages/SignupPage';
import LoginPage from './Pages/LoginPage'
import LocationAndSports from './Pages/LocationAndSports';
import {AuthProvider} from "./utils/AuthContext";
import ProtectedRoute from "./utils/ProtectedRoute";
import TurfDetails from "./Pages/TurfDetails";
import SlotDetails from "./Pages/SlotDetails";
import ConfirmPayment from "./Pages/ConfirmPayment";
import UserProfile from "./Pages/UserProfile";
import Wishlist from "./Pages/Wishlist";
import EditProfile from "./Pages/EditProfile";
import AdminLogin from "./Pages/AdminLogin";
import AdminSignup from "./Pages/AdminSignup";
import AddTurfForm from "./Pages/AddTurfForm";
import UpdateTurf from "./Pages/UpdateTurf";
import EditTurf from "./Pages/EditTurf";
import SetSlot from "./Pages/SetSlot";
import BookingDetails from "./Pages/BookingDetails";
import PaymentPage from "./Pages/FinalPay";
import EditBooking from "./Pages/EditBooking";
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