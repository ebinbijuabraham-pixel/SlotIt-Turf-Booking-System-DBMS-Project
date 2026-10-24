-- Haversine formula query to find venues within a given radius
-- The Haversine formula calculates the great-circle distance between two points on a sphere
-- given their longitudes and latitudes.
-- 
-- Constants:
-- 6371 : Earth's radius in kilometers
-- 3959 : Earth's radius in miles (use this instead if you need distance in miles)
-- 
-- Inputs needed for this query:
-- @user_lat : The latitude of the user
-- @user_lng : The longitude of the user
-- @radius_km : The maximum distance in kilometers

SET @user_lat = 8.885000;
SET @user_lng = 76.590000;
SET @radius_km = 10.0;

SELECT 
    venue_id, 
    name, 
    address, 
    sport_type,
    latitude, 
    longitude,
    (
        6371 * acos(
            cos(radians(@user_lat)) 
            * cos(radians(latitude)) 
            * cos(radians(longitude) - radians(@user_lng)) 
            + sin(radians(@user_lat)) 
            * sin(radians(latitude))
        )
    ) AS distance_km
FROM 
    venues
HAVING 
    distance_km <= @radius_km
ORDER BY 
    distance_km ASC;
