import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMapEvents } from 'react-leaflet';
import L from 'leaflet';
import axios from 'axios';
import 'leaflet/dist/leaflet.css';

delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

const MapEvents = ({ onClick }) => {
  useMapEvents({
    click(e) {
      onClick(e.latlng);
    },
  });
  return null;
};

const MapComponent = () => {
  const [events, setEvents] = useState([]);
  const [tempMarker, setTempMarker] = useState(null);
  const [formData, setFormData] = useState({ title: '', description: '' });

  useEffect(() => {
    fetchEvents();
  }, []);

  const fetchEvents = async () => {
    try {
      const res = await axios.get('http://localhost:5000/events');
      setEvents(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  const handleAddEvent = async (e) => {
    e.preventDefault();
    try {
      const newEvent = {
        ...formData,
        lat: tempMarker.lat,
        lng: tempMarker.lng,
        creator: localStorage.getItem('username')
      };
      const res = await axios.post('http://localhost:5000/events', newEvent);
      setEvents([...events, res.data]);
      setTempMarker(null);
      setFormData({ title: '', description: '' });
    } catch (err) {
      alert("Error saving event");
    }
  };

  return (
    <section className="map-wrapper" aria-labelledby="map-heading">
      <h2 id="map-heading" className="sr-only">Interactive Event Map</h2>
      
      <div role="application" aria-label="Interactive map showing local events">
        <MapContainer 
          center={[50.45, 30.52]} 
          zoom={13} 
          style={{ height: '600px', width: '100%' }}
          placeholder={<p>Loading map...</p>}
        >
          <TileLayer 
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" 
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          />
          
          {events.map((event, idx) => (
            <Marker 
              key={event._id || idx} 
              position={[event.lat, event.lng]}
              alt={`Event: ${event.title}`}
            >
              <Popup>
                <article>
                  <h3 style={{ margin: '0 0 5px 0' }}>{event.title}</h3>
                  <p style={{ margin: '0 0 10px 0', color: '#475569' }}>{event.description}</p>
                  <footer style={{ color: '#94a3b8', fontSize: '0.8rem' }}>
                    Added by: <span className="author">{event.creator}</span>
                  </footer>
                </article>
              </Popup>
            </Marker>
          ))}

          <MapEvents onClick={setTempMarker} />

          {tempMarker && (
            <Popup position={tempMarker} onClose={() => setTempMarker(null)}>
              <form onSubmit={handleAddEvent} className="map-form">
                <legend style={{ fontWeight: 'bold', marginBottom: '10px' }}>Create New Event</legend>
                
                <div className="field-group">
                  <label htmlFor="event-title">Event Title</label>
                  <input 
                    id="event-title"
                    placeholder="e.g. Community Cleanup" 
                    value={formData.title}
                    onChange={e => setFormData({...formData, title: e.target.value})}
                    required 
                    aria-required="true"
                  />
                </div>

                <div className="field-group">
                  <label htmlFor="event-desc">Description</label>
                  <textarea 
                    id="event-desc"
                    placeholder="Tell us more about it" 
                    value={formData.description}
                    onChange={e => setFormData({...formData, description: e.target.value})}
                    required 
                    aria-required="true"
                  />
                </div>

                <button type="submit" aria-label="Save this event to the map">
                  Add to Map
                </button>
              </form>
            </Popup>
          )}
        </MapContainer>
      </div>
      
      <div className="sr-only" aria-live="polite">
        {tempMarker ? "New event marker placed. Fill out the form in the popup to save." : ""}
      </div>
    </section>
  );
};

export default MapComponent;