import { useState, useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import axios from "axios";
import BG from '../../public/images/sports_11zon.jpg';
function TurfDetails() {
    const [searchParams] = useSearchParams();
    const [turfs, setTurfs] = useState([]);
    const [wishlist, setWishlist] = useState([]);
    const [ratings, setRatings] = useState({});
    const navigate = useNavigate();
    const [hoveredCard, setHoveredCard] = useState(null);
    const userEmail = localStorage.getItem("email");

    useEffect(() => {
        const location = searchParams.get("location");
        const sport = searchParams.get("sport");

        axios
            .get(`${import.meta.env.VITE_API_URL}/home/turfs?location=${location}&sport=${sport}`)
            .then((response) => {
                setTurfs(response.data);

                const initialRatings = {};
                response.data.forEach((turf) => {
                    initialRatings[turf.turfid] = turf.rating || 0;
                });
                setRatings(initialRatings);
            });
    }, [searchParams]);

    const handleSelectSlot = (turfId) => {
        navigate(`/${turfId}`);
    };

    // Fetch user's wishlist on component mount
    useEffect(() => {
        if (userEmail) {
            axios
                .get(`${import.meta.env.VITE_API_URL}/home/wishlist`, { params: { email: userEmail } })
                .then((response) => {
                    setWishlist(response.data); // Response should be an array of turf IDs
                })
                .catch((error) => console.error("Error fetching wishlist:", error));
        }
    }, [userEmail]);

    const handleWishlistToggle = (turfId) => {
        console.log("Toggling wishlist for Turf ID:", turfId);
        axios
            .post(`${import.meta.env.VITE_API_URL}/home/toggle`, null, {
                params: { email: userEmail, turfId: turfId },
            })
            .then(() => {
                // Update wishlist state
                setWishlist((prevWishlist) =>
                    prevWishlist.includes(turfId)
                        ? prevWishlist.filter((id) => id !== turfId)
                        : [...prevWishlist, turfId]
                );
            })
            .catch((error) => console.error("Error toggling wishlist:", error));
    };

    const handleRatingClick = (turfId, newRating) => {
        console.log(newRating);
        axios.post(`${import.meta.env.VITE_API_URL}/home/ratings`,{
            turfId: turfId,
            userRating: newRating,
            userEmail: userEmail
            }).then(()=>{
                setRatings((prevRatings) => ({
                            ...prevRatings,
                            [turfId]: newRating,
                        }));
                } ).catch((error) => console.log(error));

    };

    const containerStyle = {
        color: "white",
        backgroundImage: `url(${BG})`,
        backgroundSize: "cover",
        display: "flex",
        flexDirection: "column",
        justifyContent: "flex-start",
        paddingTop: "40px",
        minHeight: "100vh",
        backgroundPosition: "center",
        position: "relative", // To allow overlay positioning
        alignItems: "center",
        backgroundColor: "rgba(0, 0, 0, 0.7)",
    };

    // Darker overlay style
    const overlayStyle = {
        position: "absolute",
        top: "0",
        left: "0",
        right: "0",
        bottom: "0",
        backgroundColor: "rgba(0, 0, 0, 0.7)", // Dark overlay with higher opacity (more darkness)
        zIndex: "-1", // Behind the content
    };

    const gridStyle = {
        display: "grid",
        gridTemplateColumns: "repeat(auto-fit, minmax(300px, 1fr))",
        gap: "50px",
        width: "90%",
        margin: "0 auto",
        marginBottom: "50px",
    };

    const cardStyle = (isHovered) => ({
        display: "flex",
        flexDirection: "column",
        justifyContent: "space-between",
        alignItems: "center",
        border: "1px solid #ccc",
        borderRadius: "8px",
        padding: "15px",
        backgroundColor: "rgba(0,0,0,0.8)",
        boxShadow: isHovered
            ? "6px 12px 12px rgba(0, 188, 212, 1)"
            : "0 2px 5px rgba(0, 0, 0, 0.1)",
        transform: isHovered ? "scale(1.05)" : "scale(1)",
        transition: "transform 0.3s ease, box-shadow 0.3s ease, border-color 0.3s ease",
        borderColor: isHovered ? "rgb(0, 188, 212)" : "#ccc",
        width: "100%",
        maxWidth: "400px",
        margin: "auto",
    });

    const imgContainerStyle = (image) => ({
        height: "180px",
        width: "100%",
        borderRadius: "8px",
        backgroundColor: "#ddd",
        backgroundImage: image ? `url(data:image/jpeg;base64,${image})` : "url('../assets/turf.jpg')",
        backgroundSize: "cover",
        backgroundPosition: "center",
        marginBottom: "10px",
    });

    const buttonStyle = {
        backgroundColor: "#00bcd4",
        color: "white",
        padding: "8px 12px",
        border: "none",
        borderRadius: "5px",
        cursor: "pointer",
        fontSize: "16px",
        transition: "background-color 0.3s ease",
        marginTop: "10px",
        width: "100%",
    };

    const buttonHoverStyle = {
        backgroundColor: "#008ba3",
    };

    const starStyle = {
        fontSize: "20px",
        cursor: "pointer",
        color: "#f39c12",
        transition: "color 0.2s ease",
    };

    const heartStyle = {
        cursor: "pointer",
        color: "#ccc",
        fontSize: "30px",
        transition: "color 0.3s ease",
    };

    const heartActiveStyle = {
        color: "red",
    };

    const h1Style = {
        padding:"15px",
        color:"rgb(0,188,212)",
        backgroundColor:"rgba(0,0,0,0.6)",
        borderRadius:"8px",
        marginBottom:"20px",
    };
    const h2Style={
        display:"flex",
        flexDirection:"column",
        alignItems:"center",
    }

    return (
        <div style={containerStyle}>
            <div style={overlayStyle}></div> {/* Dark overlay */}
            <h1 style={h1Style}>Turf Details</h1>
            {turfs.length > 0 ? (
                <div style={gridStyle}>
                    {turfs.map((turf) => {

                        const turfRating = ratings[turf.turfid] || 0;

                        return (
                            <div
                                key={turf.turfid}
                                style={cardStyle(hoveredCard === turf.turfid)}
                                onMouseEnter={() => setHoveredCard(turf.turfid)}
                                onMouseLeave={() => setHoveredCard(null)}
                            >
                                <div style={imgContainerStyle(turf.image)}></div>
                                <div style={h2Style}>
                                    <h2 >{turf.turfname}</h2>
                                    <p>Location: {turf.location}</p>
                                    <p>Price: ₹ {turf.price}</p>
                                    <p>Contact: {turf.mobilenumber}</p>
                                    <p>Length: {turf.length}</p>
                                    <p>Breadth: {turf.breadth}</p>
                                </div>
                                <div>
                                    {[...Array(5)].map((_, index) => (
                                        <span
                                            key={index}
                                            style={{
                                                ...starStyle,
                                                color: index < turfRating ? "#f39c12" : "#ccc",
                                            }}
                                            onClick={() => handleRatingClick(turf.turfid, index + 1)}
                                        >
                                            &#9733;
                                        </span>
                                    ))}
                                </div>
                                <button
                                    style={buttonStyle}
                                    onMouseEnter={(e) =>
                                        Object.assign(e.currentTarget.style, buttonHoverStyle)
                                    }
                                    onMouseLeave={(e) =>
                                        Object.assign(e.currentTarget.style, buttonStyle)
                                    }

                                    onClick={() =>{

                                        handleSelectSlot(turf.turfid)}}
                                >
                                    Select Slot
                                </button>
                                <div
                                    style={heartStyle}
                                    onClick={() => {
                                        console.log("Heart Clicked for Turf ID:", turf.turfid);
                                        handleWishlistToggle(turf.turfid);
                                    }}
                                >
                                    <span
                                        style={wishlist.includes(turf.turfid)
                                            ? heartActiveStyle
                                            : {}}
                                    >
                                        &#9829;
                                    </span>
                                </div>
                            </div>
                        );
                    })}
                </div>
            ) : (
                <p style={{ color: "white" }}>No turfs available for the selected location and sport.</p>
            )}
        </div>
    );
}
export default TurfDetails;